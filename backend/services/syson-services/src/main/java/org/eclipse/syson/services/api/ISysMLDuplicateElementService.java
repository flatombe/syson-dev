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
package org.eclipse.syson.services.api;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Membership;

/**
 * Service about duplicating {@link Element SysML Elements}.
 *
 * @author flatombe
 */
public interface ISysMLDuplicateElementService {

    /**
     * Duplicates an {@link Element}.
     *
     * @param editingContext
     *            the (non-{@code null}) {@link IEditingContext} in which the duplication is performed.
     * @param elementToDuplicate
     *            the (non-{@code null}) {@link Element} to duplicate.
     * @param duplicationDestination
     *            the (non-{@code null}) duplication destination. Most likely another {@link Element} that needs a
     *            {@link Membership} to contain a copy of {@link code elementToDuplicate}.
     * @param membershipTypeName
     *            the (maybe-{@code null}) name of the {@link Membership} type to create.
     */
    DuplicateElementStatus duplicateElement(IEditingContext editingContext, Element elementToDuplicate, Element duplicationDestination, String membershipTypeName, boolean duplicateContent,
            boolean copyOutgoingReferences,
            boolean updateIncomingReferences);

    /**
     * Result type for {@link ISysMLDuplicateElementService#duplicateElement(Element, Element, String)}.
     *
     * @author flatombe
     */
    record DuplicateElementStatus(boolean isSuccess, Element duplicatedElement, Element duplicationDestination, Membership createdMembership, String message) {
        public static DuplicateElementStatus success(final Element duplicatedElement, final Element duplicationDestination, final Membership createdMembership) {
            return new DuplicateElementStatus(true, duplicatedElement, duplicationDestination, createdMembership, null);
        }

        public static DuplicateElementStatus failure(String failureMessage) {
            return new DuplicateElementStatus(false, null, null, null, failureMessage);
        }
    }
}
