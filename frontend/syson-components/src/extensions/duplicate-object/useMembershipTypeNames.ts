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

import { gql, useQuery } from '@apollo/client';
import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import { useEffect } from 'react';
import {
  GQLGetMembershipTypeNamesQueryData,
  GQLGetMembershipTypeNamesQueryVariables,
  UseMembershipTypeNamesValue,
} from './useMembershipTypeNames.types';

const getMembershipTypeNamesQuery = gql`
  query getMembershipTypeNames($editingContextId: ID!, $containerId: ID!, $ownedObjectId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        membershipTypeNames(containerId: $containerId, ownedObjectId: $ownedObjectId)
      }
    }
  }
`;

export const useMembershipTypeNames = (
  editingContextId: string,
  containerId: string | null,
  ownedObjectId: string
): UseMembershipTypeNamesValue => {
  const variables: GQLGetMembershipTypeNamesQueryVariables = {
    editingContextId,
    containerId,
    ownedObjectId,
  };
  const { error, data } = useQuery<GQLGetMembershipTypeNamesQueryData, GQLGetMembershipTypeNamesQueryVariables>(
    getMembershipTypeNamesQuery,
    { variables, skip: containerId === null }
  );

  const { addErrorMessage } = useMultiToast();
  useEffect(() => {
    if (error) {
      addErrorMessage(error.message);
    }
  }, [error]);

  let membershipTypeNames: string[] = [];
  if (data) {
    membershipTypeNames = data.viewer.editingContext.membershipTypeNames;
  }

  return { membershipTypeNames };
};
