/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */

package org.praxislive.ide.project.ui;

import org.netbeans.api.queries.VisibilityQuery;
import org.praxislive.ide.project.api.PraxisProject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 */
class PraxisFolderChildren extends FilterNode.Children {
    
    private final static String CONFIG_FOLDER_NAME = "config";
    private final static String PXP_EXT = "pxp";
    private final static Node[] EMPTY_NODES = new Node[0];

    private PraxisProject project;

    public PraxisFolderChildren(PraxisProject project, Node original) {
        super(original);
        this.project = project;
    }

    @Override
    protected Node[] createNodes(Node original) {
        FileObject fob = original.getLookup().lookup(FileObject.class);
        
        if (fob != null) {
            
            if (!VisibilityQuery.getDefault().isVisible(fob)) {
                return EMPTY_NODES;
            }
            
            if (fob.isFolder()) {
                // hide config
                if (CONFIG_FOLDER_NAME.equals(fob.getName()) &&
                        fob.getParent().equals(project.getProjectDirectory())) {
                    return EMPTY_NODES;
                }
                return new Node[]{new PraxisFolderNode(project, original)};
            } else {
                if (fob.hasExt(PXP_EXT)) {
                    return EMPTY_NODES;
                }
                return new Node[]{new PraxisFileNode(project, original)};
            }
        }
        return EMPTY_NODES;
    }








}
