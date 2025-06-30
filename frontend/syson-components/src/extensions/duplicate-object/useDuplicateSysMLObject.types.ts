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

import { GQLErrorPayload, GQLMessage } from '@eclipse-sirius/sirius-components-core';

export interface UseDuplicateSysMLObjectValue {
  duplicateSysMLObject: (
    editingContextId: string,
    objectId: string,
    ownerId: string,
    membershipTypeName: string,
    duplicateContents: boolean,
    copyOutgoingReferences: boolean,
    updateIncomingReferences: boolean
  ) => void;
  duplicatedObject: GQLObject | null;
  createdMembership: GQLObject | null;
  loading: boolean;
}

export interface GQLDuplicateSysMLObjectVariables {
  input: GQLDuplicateSysMLObjectInput;
}

export interface GQLDuplicateSysMLObjectInput {
  id: string;
  editingContextId: string;
  objectId: string;
  ownerId: string;
  membershipTypeName: string;
  duplicateContents: boolean;
  copyOutgoingReferences: boolean;
  updateIncomingReferences: boolean;
}

export interface GQLDuplicateSysMLObjectData {
  duplicateSysMLObject: GQLDuplicateSysMLObjectPayload;
}

export interface GQLDuplicateSysMLObjectSuccessPayload {
  __typename: 'DuplicateSysMLObjectSuccessPayload';
  id: string | null;
  messages: GQLMessage[] | null;
  duplicatedObject: GQLObject;
  createdMembership: GQLObject | null;
}

export interface GQLObject {
  id: string;
}

export type GQLDuplicateSysMLObjectPayload = GQLErrorPayload | GQLDuplicateSysMLObjectSuccessPayload;
