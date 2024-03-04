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
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.ide.pxr;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.praxislive.core.ComponentAddress;
import org.praxislive.ide.core.api.Callback;
import org.praxislive.ide.core.api.Task;
import org.praxislive.ide.model.ContainerProxy;
import org.praxislive.ide.core.api.AbstractTask;
import org.praxislive.ide.core.api.SerialTasks;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.praxislive.ide.pxr.wizard.PXGExportWizard;

/**
 *
 */
public class ActionBridge {

    private final static ActionBridge INSTANCE = new ActionBridge();

    private final static RequestProcessor RP = new RequestProcessor(ActionBridge.class);

    private ActionBridge() {
        // non instantiable
    }

    @Deprecated
    public void copyToClipboard(ContainerProxy container, Set<String> children) {
        StringBuilder sb = new StringBuilder();
        try {
            PXRWriter.writeSubGraph((PXRContainerProxy) container, children, sb);
            SubGraphTransferable tf = new SubGraphTransferable(sb.toString());
            getClipboard().setContents(tf, tf);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Task createCopyTask(ContainerProxy container,
            Set<String> children,
            Runnable preWriteTask, Runnable postWriteTask) {
        SubGraphTransferable empty = new SubGraphTransferable("");
        getClipboard().setContents(empty, empty);
        SyncTask sync = new SyncTask(
                children.stream()
                        .map(container::getChild)
                        .filter(cmp -> cmp != null)
                        .collect(Collectors.toSet()), 250);
        WriteClipboardTask write
                = new WriteClipboardTask(container, children, preWriteTask, postWriteTask);
        return new SerialTasks(sync, write);
    }

    public Task createExportTask(ContainerProxy container,
            Set<String> children,
            Runnable preWriteTask, Runnable postWriteTask) {
        SyncTask sync = new SyncTask(
                children.stream()
                        .map(container::getChild)
                        .filter(cmp -> cmp != null)
                        .collect(Collectors.toSet()), 250);
        ExportTask write
                = new ExportTask(container, children, preWriteTask, postWriteTask);
        return new SerialTasks(sync, write);
    }

    private static class WriteClipboardTask extends AbstractTask {

        private final ContainerProxy container;
        private final Set<String> children;
        private final Runnable preWriteTask;
        private final Runnable postWriteTask;

        WriteClipboardTask(ContainerProxy container,
                Set<String> children,
                Runnable preWriteTask,
                Runnable postWriteTask) {
            this.container = container;
            this.children = children;
            this.preWriteTask = preWriteTask;
            this.postWriteTask = postWriteTask;
        }

        @Override
        protected void handleExecute() throws Exception {
            try {
                if (preWriteTask != null) {
                    preWriteTask.run();
                }
                StringBuilder sb = new StringBuilder();
                PXRWriter.writeSubGraph((PXRContainerProxy) container, children, sb);
                SubGraphTransferable tf = new SubGraphTransferable(sb.toString());
                getClipboard().setContents(tf, tf);
            } finally {
                if (postWriteTask != null) {
                    postWriteTask.run();
                }
            }
            updateState(State.COMPLETED);
        }

    }

    private static class ExportTask extends AbstractTask {

        private final ContainerProxy container;
        private final Set<String> children;
        private final Runnable preWriteTask;
        private final Runnable postWriteTask;

        ExportTask(ContainerProxy container,
                Set<String> children,
                Runnable preWriteTask,
                Runnable postWriteTask) {
            this.container = container;
            this.children = children;
            this.preWriteTask = preWriteTask;
            this.postWriteTask = postWriteTask;
        }

        @Override
        protected void handleExecute() throws Exception {
            if (preWriteTask != null) {
                preWriteTask.run();
            }
            StringBuilder sb = new StringBuilder();
            PXRWriter.writeSubGraph((PXRContainerProxy) container, children, sb);

            if (postWriteTask != null) {
                postWriteTask.run();
            }

            String export = sb.toString();
            PXGExportWizard wizard = new PXGExportWizard();
            PXRParser.RootElement root = PXRParser.parseInContext(container.getAddress(), export);
            wizard.setSuggestedFileName(findSuggestedName(root));
            wizard.setSuggestedPaletteCategory(findPaletteCategory(root));
            
            if (wizard.display() != WizardDescriptor.FINISH_OPTION) {
                updateState(State.CANCELLED);
                return;
            }

            File file = wizard.getExportFile();
            String paletteCategory = wizard.getPaletteCategory().replace(":", "_");

            RP.post(() -> {
                if (file.exists()) {
                    EventQueue.invokeLater(() -> {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("File already exists.", NotifyDescriptor.ERROR_MESSAGE));
                        updateState(State.ERROR);
                    });
                } else {
                    try {
                        Files.write(file.toPath(), export.getBytes(StandardCharsets.UTF_8));
                        FileUtil.toFileObject(file.getParentFile()).refresh();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        EventQueue.invokeLater(() -> {
                            updateState(State.ERROR);
                        });
                        return;
                    }
                    try {
                        if (!paletteCategory.isEmpty()) {
                            FileObject src = FileUtil.toFileObject(file);
                            FileObject dst = FileUtil.createFolder(
                                    FileUtil.getConfigRoot(), "PXR/Palette/" + paletteCategory);
                            FileUtil.copyFile(src, dst, src.getName());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        EventQueue.invokeLater(() -> {
                            updateState(State.ERROR);
                        });
                        return;
                    }

                    EventQueue.invokeLater(() -> {
                        updateState(State.COMPLETED);
                    });

                }

            });

        }

        private String findSuggestedName(PXRParser.RootElement root) {
            if (root.children.length == 1) {
                return root.children[0].address.componentID();
            } else {
                return "";
            }
        }

        private String findPaletteCategory(PXRParser.RootElement root) {
            String ret = "core:custom";
            for (PXRParser.ComponentElement cmp : root.children) {
                if (cmp.children.length > 0) {
                    // container ??
                    return "";
                }
                String type = cmp.type.toString();
                if (type.startsWith("video:gl:")) {
                    // short circuit for GL
                    return "video:gl:custom";
                } else if (!type.startsWith("core")) {
                    String base = type.substring(0, type.indexOf(":"));
                    ret = base + ":custom";
                }
            }
            return ret;
        }

    }

    public boolean pasteFromClipboard(ContainerProxy container, Callback callback) {
        Clipboard c = getClipboard();
        if (c.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                String script = (String) c.getData(DataFlavor.stringFlavor);
                if (script.trim().isEmpty()) {
                    return false;
                }
                PXRParser.RootElement fakeRoot = PXRParser.parseInContext(container.getAddress(), script);
                if (ImportRenameSupport.prepareForPaste(container, fakeRoot)) {
                    PXRBuilder builder = new PXRBuilder(findRootProxy(container), fakeRoot, null);
                    builder.process(callback);
                    return true;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    private static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);
        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return c;
    }

    public boolean importSubgraph(final ContainerProxy container,
            final FileObject file,
            final List<String> warnings,
            final Callback callback) {
        if (!file.hasExt("pxg")) {
            return false;
        }
        final ComponentAddress context = container.getAddress();
        RP.execute(new Runnable() {
            @Override
            public void run() {
                PXRParser.RootElement r = null;
                try {
                    r = PXRParser.parseInContext(context, file.asText());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                final PXRParser.RootElement root = r;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (root != null) {
                                if (ImportRenameSupport.prepareForImport(container, root)) {
                                    PXRBuilder builder = new PXRBuilder(findRootProxy(container), root, warnings);
                                    builder.process(callback);
                                } else {
                                    callback.onReturn(List.of());
                                }
                            } else {
                                callback.onError(List.of());
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        });
        return true;
    }

    private PXRRootProxy findRootProxy(ContainerProxy container) {
        while (container != null) {
            if (container instanceof PXRRootProxy) {
                return (PXRRootProxy) container;
            }
            container = container.getParent();
        }
        throw new IllegalStateException("No root proxy found");
    }

    public static ActionBridge getDefault() {
        return INSTANCE;
    }

    private static class SubGraphTransferable implements Transferable, ClipboardOwner {

        private String data;

        private SubGraphTransferable(String data) {
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{
                DataFlavor.stringFlavor
            };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(DataFlavor.stringFlavor)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return data;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // no op
        }
    }

}
