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
package org.eclipse.syson.sysml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.syson.sysml.upload.SysMLExternalResourceLoaderService;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SysMLExternalResourceLoaderService}.
 *
 * @author Arthur Daussy
 */
public class SysMLExternalResourceLoaderServiceTests {

    @Test
    public void testCandHandle() {
        // Incorrect content
        assertFalse(new SysMLExternalResourceLoaderService(new SysmlToAst(null)).canHandle(new ByteArrayInputStream("{}".getBytes()), URI.createFileURI("/test/model.sysml"), new ResourceSetImpl()));
        // Incorrect extension
        assertFalse(new SysMLExternalResourceLoaderService(new SysmlToAst(null)).canHandle(new ByteArrayInputStream("package p1;".getBytes()), URI.createFileURI("/test/model.sysml2"),
                new ResourceSetImpl()));
        // Valid file
        assertTrue(new SysMLExternalResourceLoaderService(new SysmlToAst(null)).canHandle(new ByteArrayInputStream("package p1;".getBytes()), URI.createFileURI("/test/model.sysml"),
                new ResourceSetImpl()));
    }

}