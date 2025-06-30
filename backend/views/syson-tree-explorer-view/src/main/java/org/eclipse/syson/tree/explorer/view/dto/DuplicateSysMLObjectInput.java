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
package org.eclipse.syson.tree.explorer.view.dto;

import java.util.UUID;

import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.syson.tree.explorer.view.handlers.DuplicateSysMLObjectEventHandler;

import jakarta.validation.constraints.NotNull;

/**
 * {@link IInput} implementation for {@link DuplicateSysMLObjectEventHandler}.
 * 
 * @author flatombe
 */
public record DuplicateSysMLObjectInput(@NotNull UUID id, @NotNull String editingContextId, @NotNull String objectId, @NotNull String ownerId, @NotNull String membershipTypeName,
        boolean duplicateContents, boolean copyOutgoingReferences, boolean updateIncomingReferences) implements IInput {

}
