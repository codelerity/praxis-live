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

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.praxislive.ide.project.api.PraxisProject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 */
class PraxisFolderNode extends FilterNode {

    public PraxisFolderNode(PraxisProject project, Node original) {
        super(original, new PraxisFolderChildren(project, original),
                new ProxyLookup(original.getLookup(), Lookups.fixed(new BaseTemplates(), project)));
    }

    private static class BaseTemplates implements PrivilegedTemplates {

        @Override
        public String[] getPrivilegedTemplates() {
            return new String[]{
                "Templates/Other/Folder",
                "Templates/Other/org-netbeans-modules-project-ui-NewFileIterator-folderIterator"
            };
        }
    }
    

}
