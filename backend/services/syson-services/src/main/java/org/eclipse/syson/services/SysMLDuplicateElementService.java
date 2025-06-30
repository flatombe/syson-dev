/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.services;

import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.utils.SiriusEMFCopier;
import org.eclipse.syson.services.api.ISysMLDuplicateElementService;
import org.eclipse.syson.services.api.ISysMLReadOnlyService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.OwningMembership;
import org.eclipse.syson.sysml.Relationship;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link ISysMLDuplicateElementService}.
 *
 * @author flatombe
 */
@Service
public class SysMLDuplicateElementService implements ISysMLDuplicateElementService {

    private final ISysMLReadOnlyService readOnlyService;

    public SysMLDuplicateElementService(final ISysMLReadOnlyService readOnlyService) {
        this.readOnlyService = Objects.requireNonNull(readOnlyService);
    }

    @Override
    public DuplicateElementStatus duplicateElement(final IEditingContext editingContext, final Element elementToDuplicate, final Element duplicationDestination, final String membershipTypeName,
            final boolean duplicateContent,
            final boolean copyOutgoingReferences,
            final boolean updateIncomingReferences) {
        Objects.requireNonNull(editingContext);
        Objects.requireNonNull(elementToDuplicate);
        Objects.requireNonNull(duplicationDestination);

        final DuplicateElementStatus result;

        if (duplicationDestination instanceof Relationship) {
            result = DuplicateElementStatus.failure("TODO: currently not supported");
        } else if (elementToDuplicate instanceof Relationship) {
            result = DuplicateElementStatus.failure("TODO: currently not supported");
        } else if (this.readOnlyService.isReadOnly(elementToDuplicate)) {
            result = DuplicateElementStatus.failure("Cannot duplicate into a read-only element");
        } else {
            result = this.doDuplicateElement(editingContext, elementToDuplicate, duplicationDestination, membershipTypeName, duplicateContent, copyOutgoingReferences,
                    updateIncomingReferences);
        }

        return result;
    }

    private DuplicateElementStatus doDuplicateElement(final IEditingContext editingContext, final Element elementToDuplicate, final Element duplicationDestination, final String membershipTypeName,
            final boolean duplicateContent,
            final boolean copyOutgoingReferences,
            final boolean updateIncomingReferences) {
        final DuplicateElementStatus result;

        if (membershipTypeName == null) {
            // TODO: I think when elementToDuplicate is a Relationship we will have no membershipTypeName.
            // In that case the duplicate is probably simpler, just have to do a regular EMF copy and place it at the
            // destination (if multiplicity/editability allows).

            // Currently we only support the happiest case so we should always have a membershipTypeName.
            result = DuplicateElementStatus.failure("Missing membership type name");
        } else {
            final EClassifier membershipEClassifier = SysmlPackage.eINSTANCE.getEClassifier(membershipTypeName);
            if (membershipEClassifier == null || !(membershipEClassifier instanceof EClass eClass && SysmlPackage.eINSTANCE.getOwningMembership().isSuperTypeOf(eClass))) {
                // By construction, at this point we should have an EClass that sub-types OwningMembership.
                result = DuplicateElementStatus.failure("Erroneous membership type name");
            } else {
                result = this.doDuplicateElementWithOwningMembershipCreation(elementToDuplicate, duplicationDestination, (EClass) membershipEClassifier, duplicateContent, copyOutgoingReferences,
                        updateIncomingReferences);
            }
        }
        return result;
    }

    private DuplicateElementStatus doDuplicateElementWithOwningMembershipCreation(final Element elementToDuplicate, final Element duplicationDestination, final EClass owningMembershipType,
            final boolean duplicateContent, final boolean copyOutgoingReferences, final boolean updateIncomingReferences) {
        final OwningMembership createdMembership = (OwningMembership) SysmlPackage.eINSTANCE.getSysmlFactory().create(owningMembershipType);
        // TODO: if there are any specific settings from the membership that contained elementToDuplicate,
        // I think we might want to try and copy them over to the newly-created membership?
        // For instance any eAnnotation, membership name/visibility, etc.

        final Element duplicatedElement = createObjectCopy(elementToDuplicate, duplicateContent, copyOutgoingReferences, updateIncomingReferences);
        createdMembership.getOwnedRelatedElement().add(duplicatedElement);
        duplicationDestination.getOwnedRelationship().add(createdMembership);

        if (updateIncomingReferences) {
            updateIncomingReferences(duplicatedElement, elementToDuplicate);
        }

        return DuplicateElementStatus.success(duplicatedElement, duplicationDestination, createdMembership);
    }

    // Copied from org.eclipse.sirius.web.application.views.explorer.services.DefaultObjectDuplicator
    private static void updateIncomingReferences(EObject duplicatedEObject, EObject objectToDuplicate) {
        duplicatedEObject.eAdapters().stream()
                .filter(ECrossReferenceAdapter.class::isInstance)
                .map(ECrossReferenceAdapter.class::cast)
                .flatMap(eCrossReferenceAdapter -> eCrossReferenceAdapter.getInverseReferences(objectToDuplicate).stream())
                .filter(setting -> {
                    boolean canFeatureBeSet = false;
                    if (setting.getEStructuralFeature() instanceof EReference eReference && eReference.isMany() && !eReference.isContainment()) {
                        List<?> list = (List<?>) setting.getEObject().eGet(eReference);
                        int upperBound = eReference.getUpperBound();
                        canFeatureBeSet = upperBound == -1 || (upperBound > 0 && list.size() < upperBound);
                    }
                    return canFeatureBeSet;
                })
                .forEach(setting -> {
                    if (setting.getEStructuralFeature() instanceof EReference eReference && eReference.isMany()) {
                        List<EObject> list = (List<EObject>) setting.getEObject().eGet(eReference);
                        list.add(duplicatedEObject);
                    }
                });
    }

    // Adapted from
    // org.eclipse.sirius.web.application.views.explorer.services.DefaultObjectDuplicator.duplicateObject(EObject,
    // DuplicationSettings)
    private static <T extends EObject> T createObjectCopy(T eObjectToCopy, final boolean mustCopyContent,
            final boolean mustCopyOutgoingReferences,
            final boolean mustUpdateIncomingReferences) {
        if (mustCopyContent) {
            EcoreUtil.Copier copier = new EcoreUtil.Copier();
            @SuppressWarnings("unchecked")
            T duplicatedObject = (T) copier.copy(eObjectToCopy);
            if (mustCopyOutgoingReferences) {
                copier.copyReferences();
            }
            return duplicatedObject;
        } else {
            SiriusEMFCopier copier = new SiriusEMFCopier();
            @SuppressWarnings("unchecked")
            T duplicatedObject = (T) copier.copyWithoutContent(eObjectToCopy);
            if (mustCopyOutgoingReferences) {
                copier.copyReferences();
            }
            return duplicatedObject;
        }
    }
}
