/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2020 Neil C Smith.
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

package org.praxislive.ide.pxr;

import java.awt.BorderLayout;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.praxislive.ide.model.RootProxy;
import org.praxislive.ide.pxr.spi.RootEditor;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;

/**
 *
 */
public class DebugRootEditor extends RootEditor {

    private final ExplorerManager manager;
    private final Panel panel;
    private final Lookup lookup;

    public DebugRootEditor(RootProxy root) {
        manager = new ExplorerManager();
        manager.setRootContext(root.getNodeDelegate());
        panel = new Panel();
        lookup = ExplorerUtils.createLookup(manager, panel.getActionMap());

    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public JComponent getEditorComponent() {
        return panel;
    }

    private class Panel extends JPanel implements ExplorerManager.Provider {

//        private JLabel label;

        private Panel() {
            setLayout(new BorderLayout());
            add(new BeanTreeView());
//            label = new JLabel("<DROP HERE>", JLabel.CENTER);
//            DropTargetListener dtl = new DropTargetAdapter() {
//
//                @Override
//                public void drop(DropTargetDropEvent dtde) {
//                    Node n = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.DND_COPY);
//                    if (n != null) {
//                        ComponentType t = n.getLookup().lookup(ComponentType.class);
//                        if (t != null) {
//                            label.setText(t.toString());
//                        }
//                    }
//                }
//            };
//            DropTarget dt = new DropTarget(label, dtl);
//            dt.setActive(true);
//            dt.setDefaultActions(DnDConstants.ACTION_COPY);
//            add(label, BorderLayout.SOUTH);
        }


        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

    }

    private class MyPaletteActions extends PaletteActions {

        @Override
        public Action[] getImportActions() {
            return new Action[0];
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return new Action[0];
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup category) {
            return new Action[0];
        }

        @Override
        public Action[] getCustomItemActions(Lookup item) {
            return new Action[0];
        }

        @Override
        public Action getPreferredAction(Lookup item) {
            return null;
        }

    }


}
