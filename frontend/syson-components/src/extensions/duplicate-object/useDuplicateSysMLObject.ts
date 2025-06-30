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
import { gql, useMutation } from '@apollo/client';
import { useReporting } from '@eclipse-sirius/sirius-components-core';
import {
  GQLDuplicateSysMLObjectData,
  GQLDuplicateSysMLObjectInput,
  GQLDuplicateSysMLObjectVariables,
  GQLObject,
  UseDuplicateSysMLObjectValue,
} from './useDuplicateSysMLObject.types';

const duplicateSysMLObjectMutation = gql`
  mutation duplicateSysMLObject($input: DuplicateSysMLObjectInput!) {
    duplicateSysMLObject(input: $input) {
      __typename
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
      ... on DuplicateSysMLObjectSuccessPayload {
        duplicatedObject {
          id
        }
        createdMembership {
          id
        }
        messages {
          body
          level
        }
      }
    }
  }
`;

export const useDuplicateSysMLObject = (): UseDuplicateSysMLObjectValue => {
  const [mutationDuplicateSysMLObject, mutationDuplicateSysMLObjectResult] = useMutation<
    GQLDuplicateSysMLObjectData,
    GQLDuplicateSysMLObjectVariables
  >(duplicateSysMLObjectMutation);

  useReporting(mutationDuplicateSysMLObjectResult, (data: GQLDuplicateSysMLObjectData) => {
    const payload = data.duplicateSysMLObject;
    if (payload.__typename === 'DuplicateSysMLObjectSuccessPayload') {
      return { __typename: 'SuccessPayload', id: payload.id, messages: payload.messages };
    } else {
      return { __typename: 'ErrorPayload', id: payload.id, messages: payload.messages };
    }
  });

  const duplicateSysMLObject = (
    editingContextId: string,
    objectId: string,
    ownerId: string,
    membershipTypeName: string,
    duplicateContents: boolean,
    copyOutgoingReferences: boolean,
    updateIncomingReferences: boolean
  ) => {
    const input: GQLDuplicateSysMLObjectInput = {
      id: crypto.randomUUID(),
      editingContextId,
      objectId,
      ownerId,
      membershipTypeName,
      duplicateContents,
      copyOutgoingReferences,
      updateIncomingReferences,
    };
    mutationDuplicateSysMLObject({ variables: { input } });
  };

  let duplicatedSysMLObject: GQLObject | null = null;
  let createdMembership: GQLObject | null = null;
  if (
    mutationDuplicateSysMLObjectResult?.data?.duplicateSysMLObject?.__typename === 'DuplicateSysMLObjectSuccessPayload'
  ) {
    duplicatedSysMLObject = mutationDuplicateSysMLObjectResult.data.duplicateSysMLObject.duplicatedObject;
    createdMembership = mutationDuplicateSysMLObjectResult.data.duplicateSysMLObject.createdMembership;
  }

  return {
    duplicateSysMLObject,
    duplicatedObject: duplicatedSysMLObject,
    createdMembership,
    loading: mutationDuplicateSysMLObjectResult?.loading,
  };
};
