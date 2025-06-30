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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.sirius.components.annotations.spring.graphql.MutationDataFetcher;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.components.graphql.api.IEditingContextDispatcher;
import org.eclipse.syson.tree.explorer.view.dto.DuplicateSysMLObjectInput;

import graphql.schema.DataFetchingEnvironment;

/**
 * {@link MutationDataFetcher} for mutation {@code duplicateSysMLObject}.
 *
 * @author flatombe
 */
@MutationDataFetcher(type = "Mutation", field = "duplicateSysMLObject")
public class MutationDuplicateSysMLObjectDataFetcher implements IDataFetcherWithFieldCoordinates<CompletableFuture<IPayload>> {

    private static final String INPUT_ARGUMENT = "input";

    private final ObjectMapper objectMapper;

    private final IEditingContextDispatcher editingContextDispatcher;

    public MutationDuplicateSysMLObjectDataFetcher(final ObjectMapper objectMapper, final IEditingContextDispatcher editingContextDispatcher) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.editingContextDispatcher = Objects.requireNonNull(editingContextDispatcher);
    }

    @Override
    public CompletableFuture<IPayload> get(final DataFetchingEnvironment environment) throws Exception {
        System.out.println("MutationDuplicateSysMLObjectDataFetcher called");
        final Object argument = environment.getArgument(INPUT_ARGUMENT);
        final DuplicateSysMLObjectInput input = this.objectMapper.convertValue(argument, DuplicateSysMLObjectInput.class);

        return this.editingContextDispatcher.dispatchMutation(input.editingContextId(), input).toFuture();
    }
}
