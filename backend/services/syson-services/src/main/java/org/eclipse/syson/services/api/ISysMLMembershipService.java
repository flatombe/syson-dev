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

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Membership;

/**
 * Services about {@link Membership} and its sub-types.
 *
 * @author flatombe
 */
public interface ISysMLMembershipService {

    /**
     * Provides the types of {@link Membership} that may exist between two {@link Element SysML Elements}.
     *
     * @param fromElement
     *            the (non-{@code null}) {@link Element} that would {@link Element#getOwnedRelationship() contain} the
     *            {@link Membership}.
     * @param toElement
     *            the (non-{@code null}) {@link Element} that would {@link Membership#getMemberElement() be contained}
     *            by the {@link Membership}.
     * @return the (non-{@code null}) {@link List} of all concrete types of {@link Membership} that may exist from
     *         {@code fromElement} to {@code toElement}.
     */
    Set<EClass> getPossibleMemberships(Element fromElement, Element toElement);
}
