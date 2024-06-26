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
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.praxislive.ide.pxr.spi.RootEditor;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;

/**
 *
 */
public class RootEditorTopComponent extends CloneableTopComponent {

    private final static Action START_STOP_ACTION = new StartableRootAction();
    private final static Action ROOT_CONFIG_ACTION = new RootConfigAction();
    
    private final PXRDataObject dob;
    private final EditorLookup lookup;
    private final PropertyChangeListener registryListener;
    private final JToolBar toolBar;
    
    private JComponent editorPanel;
    private RootEditor editor;
    private PXRRootProxy root;

    public RootEditorTopComponent(PXRDataObject dob) {
        this.setDisplayName(dob.getName());
        this.setIcon(dob.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        this.dob = dob;
        lookup = new EditorLookup(Lookups.singleton(dob), dob.getLookup());
        associateLookup(lookup);
        setLayout(new BorderLayout());
        toolBar = new ToolBar();
        add(toolBar, BorderLayout.NORTH);
        registryListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkRoot();
            }
        };
    }

    @Override
    protected void componentOpened() {
        assert EventQueue.isDispatchThread();
        var registry = PXRRootRegistry.registryForFile(dob.getPrimaryFile());
        if (registry != null) {
            root = registry.getRootByFile(dob.getPrimaryFile());
            registry.addPropertyChangeListener(registryListener);
        }
        install(root);
    }

    @Override
    protected void componentShowing() {
        if (editor != null) {
            editor.componentShowing();
        }
    }

    @Override
    protected void componentActivated() {
        if (editor != null) {
            editor.componentActivated();
            requestFocusInWindow();
        }
    }

    @Override
    protected void componentDeactivated() {
        if (editor != null) {
            editor.componentDeactivated();
        }
    }

    @Override
    protected void componentHidden() {
        if (editor != null) {
            editor.componentHidden();
        }
    }

    @Override
    protected void componentClosed() {
        syncEditor();
        var registry = PXRRootRegistry.registryForFile(dob.getPrimaryFile());
        if (registry != null) {
            registry.removePropertyChangeListener(registryListener);
        }
        uninstall(root);
    }

//    @Override
//    public void requestFocus() {
//        super.requestFocus();
//        if (editorPanel != null) {
//            editorPanel.requestFocus();
//        }
//    }
//
//    @Override
//    public boolean requestFocusInWindow() {
//        super.requestFocusInWindow();
//        if (editorPanel != null) {
//            return editorPanel.requestFocusInWindow();
//        } else {
//            return false;
//        }
//    }

    @Override
    protected CloneableTopComponent createClonedObject() {
        return new RootEditorTopComponent(dob);
    }
    
    void syncEditor() {
        if (editor != null) {
            editor.sync();
        }
    }

    private void checkRoot() {
        PXRRootProxy root = PXRRootRegistry.findRootForFile(dob.getPrimaryFile());
        if (root == this.root) {
            return;
        }
        if (root == null) {
            close();
        } else {
            uninstall(this.root);
            this.root = root;
            install(root);
        }
    }

    private void install(PXRRootProxy root) {
        if (root == null) {
            editor = new BlankEditor();
            lookup.setAdditional(editor.getLookup());
            initToolbar(new Action[0]);
        } else {
            editor = findEditor(root);
            lookup.setAdditional(
                    Lookups.singleton(new PXRRootContext(root)),
                    editor.getLookup());
            initToolbar(buildActions(editor));
        }
        editorPanel = editor.getEditorComponent();
        add(editorPanel);
        if (isVisible()) {
            editor.componentShowing();
            editor.componentActivated();
        }

    }

    private Action[] buildActions(RootEditor editor) {
        Action[] editorActions = editor.getActions();
        if (editorActions == null || editorActions.length == 0) {
            return new Action[]{START_STOP_ACTION, ROOT_CONFIG_ACTION};
        }
        ArrayList<Action> actions = new ArrayList<Action>(editorActions.length + 2);
        actions.add(START_STOP_ACTION);
        actions.add(ROOT_CONFIG_ACTION);
        actions.add(null);
        actions.addAll(Arrays.asList(editorActions));
        return actions.toArray(new Action[actions.size()]);
    }

    private void uninstall(PXRRootProxy root) {
        remove(editorPanel);
        editorPanel = null;
        editor.dispose();
        editor = null;
    }

    private void initToolbar(Action[] actions) {
        toolBar.removeAll();
        Lookup context = getLookup();
        for (Action action : actions) {
            if (action instanceof ContextAwareAction) {
                action = ((ContextAwareAction) action).createContextAwareInstance(context);
            }
            Component c;
            if (action instanceof Presenter.Toolbar) {
                c = ((Presenter.Toolbar) action).getToolbarPresenter();
            } else if (action == null) {
                c = new JToolBar.Separator();
            } else {
                JButton button = new JButton();
                Actions.connect(button, action);
                c = button;
            }
            if (c instanceof AbstractButton) {
                c.setFocusable(false);
            }
            toolBar.add(c);
        }
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    private RootEditor findEditor(PXRRootProxy root) {
        
        return Lookup.getDefault().lookupAll(RootEditor.Provider.class).stream()
                .flatMap(p -> p.createEditor(root.getProject(), root.getSourceFile(), root).stream())
                .findFirst()
                .orElse(new DebugRootEditor(root));
        
    }

    private class BlankEditor extends RootEditor {

        @Override
        public Lookup getLookup() {
            return Lookups.singleton(dob.getNodeDelegate());
        }

        @Override
        public JComponent getEditorComponent() {
            JComponent editor = new JLabel("<Build " + dob.getName() + " to edit>", JLabel.CENTER);
            editor.setFocusable(true);
            return editor;
        }
    }

    private class EditorLookup extends ProxyLookup {

        private Lookup[] permanent;

        private EditorLookup(Lookup... lookups) {
            super(lookups);
            this.permanent = lookups;
        }

        private void setAdditional(Lookup... lookups) {
            if (lookups == null || lookups.length == 0) {
                setLookups(permanent);
            } else {
                List<Lookup> lst = new ArrayList<Lookup>();
                lst.addAll(Arrays.asList(permanent));
                lst.addAll(Arrays.asList(lookups));
                setLookups(lst.toArray(new Lookup[lst.size()]));
            }
        }
    }

    private class ToolBar extends JToolBar {

        ToolBar() {
            super("editorToolbar");
            setFocusable(false);
            setFloatable(false);
            setRollover(true);
            setBorder(BorderFactory.createEtchedBorder());
        }
    }
}
