/*******************************************************************************
 * Copyright (c) 2024 Obeo.
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
package org.eclipse.syson.tree.explorer.view.services.api;

import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Interface of the delegation service interacting with the SysON explorer.
 *
 * @author gdaniel
 */
public interface ISysONExplorerServiceDelegate {

    boolean canHandle(Object self);

    String getTreeItemId(Object self);

    String getKind(Object self);

    String getLabel(Object self);

    boolean isEditable(Object self);

    boolean isDeletable(Object self);

    boolean isSelectable(Object self);

    List<String> getImageURL(Object self);

    Object getTreeItemObject(String treeItemId, IEditingContext editingContext);

    Object getParent(Object self, String treeItemId, IEditingContext editingContext);

    boolean hasChildren(Object self, IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds);

    List<Object> getChildren(Object self, IEditingContext editingContext, List<String> expandedIds, List<String> activeFilterIds);

    List<Object> getElements(IEditingContext editingContext, List<String> activeFilterIds);

}