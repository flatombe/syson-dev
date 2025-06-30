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
package org.eclipse.syson.tree.explorer.view.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.annotations.spring.graphql.QueryDataFetcher;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.syson.services.api.ISysMLMembershipService;
import org.eclipse.syson.sysml.Element;

import graphql.schema.DataFetchingEnvironment;

/**
 * {@link QueryDataFetcher} implementation for field {@code EditingContext.membershipTypeNames}.
 *
 * @author flatombe
 */
@QueryDataFetcher(type = "EditingContext", field = "membershipTypeNames")
public class EditingContextMembershipTypeNamesDataFetcher implements IDataFetcherWithFieldCoordinates<List<String>> {

    private static final String CONTAINER_ID = "containerId";

    private static final String OWNED_OBJECT_ID = "ownedObjectId";

    private final IObjectSearchService objectSearchService;

    private final ISysMLMembershipService sysMLmembershipService;

    private final IEditingContextSearchService editingContextSearchService;

    public EditingContextMembershipTypeNamesDataFetcher(
            final IEditingContextSearchService editingContextSearchService, final IObjectSearchService objectSearchService, final ISysMLMembershipService sysMLmembershipService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.sysMLmembershipService = Objects.requireNonNull(sysMLmembershipService);
        this.editingContextSearchService = Objects.requireNonNull(editingContextSearchService);
    }

    @Override
    public List<String> get(final DataFetchingEnvironment environment) throws Exception {
        final String editingContextId = environment.getSource();
        final String containerId = environment.getArgument(CONTAINER_ID);
        final String containedObjectId = environment.getArgument(OWNED_OBJECT_ID);

        final Optional<IEditingContext> maybeEditingContext = editingContextSearchService.findById(editingContextId);
        if (maybeEditingContext.isPresent()) {
            final Optional<Element> maybeOwnedElement = this.objectSearchService.getObject(maybeEditingContext.get(), containedObjectId).filter(Element.class::isInstance)
                    .map(Element.class::cast);
            if (maybeOwnedElement.isPresent()) {
                final Optional<Element> maybeOwnerElement = this.objectSearchService.getObject(maybeEditingContext.get(), containerId).filter(Element.class::isInstance)
                        .map(Element.class::cast);
                if (maybeOwnerElement.isPresent()) {
                    return sysMLmembershipService.getPossibleMemberships(maybeOwnerElement.get(), maybeOwnedElement.get()).stream().map(EClass::getName).toList();
                }
            }
        }
        // TODO FLA: should we throw some exception instead?
        return new ArrayList<>();
    }
}
