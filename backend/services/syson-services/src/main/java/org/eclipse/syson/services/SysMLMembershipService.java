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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.services.api.ISysMLMembershipService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Default implementation for {@link ISysMLMembershipService}.
 *
 * @author flatombe
 */
@Service
public class SysMLMembershipService implements ISysMLMembershipService {

    @Override
    public Set<EClass> getPossibleMemberships(Element fromElement, Element toElement) {
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);

        final Set<EClass> membershipTypes = new HashSet<>();

        final GetIntermediateContainerCreationSwitch membershipTypeSwitch = new GetIntermediateContainerCreationSwitch(fromElement);
        membershipTypeSwitch
                .doSwitch(toElement)
                .filter(SysmlPackage.eINSTANCE.getMembership()::isSuperTypeOf)
                .filter(Predicate.not(EClass::isAbstract))
                .filter(Predicate.not(EClass::isInterface))
                .ifPresent(membershipTypes::add);

        // Sanity check
        if (membershipTypes.stream().anyMatch(eClass -> !SysmlPackage.eINSTANCE.getMembership().isSuperTypeOf(eClass) || eClass.isAbstract() || eClass.isInterface())) {
            throw new RuntimeException("Only concrete sub-types of '%s' should be possible.".formatted(SysmlPackage.eINSTANCE.getMembership().getName()));
        }
        return membershipTypes;
    }

}
