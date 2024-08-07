/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2024 Neil C Smith.
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
package org.praxislive.ide.pxr.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import org.praxislive.ide.model.RootProxy;
import org.praxislive.ide.pxr.spi.RootEditor;

import static org.praxislive.ide.pxr.gui.LayoutAction.Type.*;

import org.openide.actions.DeleteAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.praxislive.ide.model.ContainerProxy;
import org.praxislive.ide.project.api.PraxisProject;
import org.praxislive.ide.pxr.api.ComponentPalette;

/**
 *
 */
public class GuiEditor implements RootEditor {

    private final PraxisProject project;
    private final FileObject file;
    private final RootProxy root;
    private final GuiHelper helper;
    private final JComponent editorComponent;
    private final JLayeredPane layeredPane;
    private final ExplorerManager em;
    private final Lookup lookup;
    private final InstanceContent content;
    private final ComponentPalette palette;

    private EditAction editAction;
    private EditLayer editLayer;
    private Action[] actions;

    GuiEditor(PraxisProject project, FileObject file, RootProxy root) {
        this.project = project;
        this.file = file;
        this.root = root;
        this.helper = project.getLookup().lookup(GuiHelper.class);
        em = new ExplorerManager();
        em.setRootContext(root.getNodeDelegate());
        // init components
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.setFocusable(true);
        editorComponent = new JScrollPane(layeredPane);

        initActions(layeredPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                layeredPane.getActionMap());
        content = new InstanceContent();
        palette = ComponentPalette.create(root instanceof ContainerProxy ? (ContainerProxy) root : null);
        lookup = new ProxyLookup(
                ExplorerUtils.createLookup(em, layeredPane.getActionMap()),
                Lookups.fixed(palette.controller()),
                new AbstractLookup(content));
    }

    private void initActions(InputMap inputMap, ActionMap actionMap) {
        actions = new Action[13];
        editAction = new EditAction();
        actions[0] = editAction;
        actions[1] = null;
        actions[2] = new LayoutAction(MoveLeft);
        actions[3] = new LayoutAction(MoveUp);
        actions[4] = new LayoutAction(MoveDown);
        actions[5] = new LayoutAction(MoveRight);
        actions[6] = null;
        actions[7] = new LayoutAction(IncreaseSpanX);
        actions[8] = new LayoutAction(DecreaseSpanX);
        actions[9] = new LayoutAction(IncreaseSpanY);
        actions[10] = new LayoutAction(DecreaseSpanY);
        actions[11] = null;
        actions[12] = DeleteAction.get(DeleteAction.class);
        inputMap.put(Utilities.stringToKey("D-e"), "edit");
        actionMap.put("edit", editAction);
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "move-left");
        actionMap.put("move-left", actions[2]);
        inputMap.put(KeyStroke.getKeyStroke("UP"), "move-up");
        actionMap.put("move-up", actions[3]);
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "move-down");
        actionMap.put("move-down", actions[4]);
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "move-right");
        actionMap.put("move-right", actions[5]);
        inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), "increase-span-x");
        actionMap.put("increase-span-x", actions[7]);
        inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), "decrease-span-x");
        actionMap.put("decrease-span-x", actions[8]);
        inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), "increase-span-y");
        actionMap.put("increase-span-y", actions[9]);
        inputMap.put(KeyStroke.getKeyStroke("shift UP"), "decrease-span-y");
        actionMap.put("decrease-span-y", actions[10]);

        actionMap.put(DeleteAction.get(DeleteAction.class).getActionMapKey(),
                ExplorerUtils.actionDelete(em, true));

    }

    @Override
    public JComponent getEditorComponent() {
        return editorComponent;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public List<Action> getActions() {
        return Stream.of(actions).toList();
    }

    RootProxy getRoot() {
        return root;
    }

    GuiHelper getHelper() {
        return helper;
    }

    void setSelected(Node[] nodes) throws Exception {
        em.setSelectedNodes(nodes);
    }

    void performPreferredAction() {
        Node[] nodes = em.getSelectedNodes();
        if (nodes.length == 1) {
            nodes[0].getPreferredAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    void addRootPanel(JPanel panel) {
        layeredPane.add(panel, JLayeredPane.DEFAULT_LAYER);
        editLayer = new EditLayer(this, panel);
        layeredPane.add(editLayer, JLayeredPane.PALETTE_LAYER);
        content.add(editLayer);
//        if (((ContainerProxy)root).getChildIDs().length == 0) {
//            editLayer.setVisible(true);
//        }
        editAction.setEnabled(true);
    }

    void removeRootPanel(JPanel panel) {
        layeredPane.remove(panel);
        layeredPane.remove(editLayer);
        content.remove(editLayer);
        editAction.setEnabled(false);
        editLayer = null;
        try {
            em.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(GuiEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void componentActivated() {
        DockableGuiRoot r = DockableGuiRoot.find(project, root.getAddress().rootID());
        if (r != null) {
            r.requestConnect(this);
        }
    }

    @Override
    public void dispose() {
        palette.dispose();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                DockableGuiRoot r = DockableGuiRoot.find(project, root.getAddress().rootID());
                if (r != null) {
                    r.requestDisconnect(GuiEditor.this);
                }
            }
        });

    }

    private class EditAction extends AbstractAction implements Presenter.Toolbar {

        private JToggleButton button;

        private EditAction() {
            super("Edit");
            putValue(SELECTED_KEY, Boolean.FALSE);
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editLayer.isVisible()) {
                editLayer.setVisible(false);
                putValue(SELECTED_KEY, Boolean.FALSE);
            } else {
                editLayer.setVisible(true);
                putValue(SELECTED_KEY, Boolean.TRUE);
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                putValue(SELECTED_KEY, editLayer.isVisible());
            } else {
                putValue(SELECTED_KEY, Boolean.FALSE);
            }
        }

        @Override
        public Component getToolbarPresenter() {
            if (button == null) {
                button = new JToggleButton(this);
            }
            return button;
        }
    }
}
