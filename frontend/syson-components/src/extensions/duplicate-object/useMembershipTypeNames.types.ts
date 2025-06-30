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

export interface UseMembershipTypeNamesValue {
  membershipTypeNames: string[];
}

export interface GQLGetMembershipTypeNamesQueryVariables {
  editingContextId: string;
  containerId: string | null;
  ownedObjectId: string;
}

export interface GQLGetMembershipTypeNamesQueryData {
  viewer: GQLViewer;
}
export interface GQLViewer {
  editingContext: GQLEditingContext;
}
export interface GQLEditingContext {
  membershipTypeNames: string[];
}
