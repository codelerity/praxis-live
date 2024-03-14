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
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.ide.pxr.palette;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.praxislive.core.ComponentType;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.ide.model.ContainerProxy;

/**
 *
 */
public final class DefaultComponentPalette {

    private final PaletteController controller;
    private final Filter filter;
    private final PropertyChangeListener listener;

    private ContainerProxy context;
    private List<ComponentType> types;

    private DefaultComponentPalette(PaletteController controller, Filter filter,
            ContainerProxy container) {
        this.controller = controller;
        this.filter = filter;
        this.listener = e -> {
            if (ContainerProtocol.SUPPORTED_TYPES.equals(e.getPropertyName())) {
                revalidate();
            }
        };
        this.types = List.of();
        context(container);
    }

    public void context(ContainerProxy context) {
        if (this.context == context) {
            return;
        }
        if (this.context != null) {
            this.context.removePropertyChangeListener(listener);
        }
        if (context != null) {
            context.addPropertyChangeListener(listener);
        }
        this.context = context;
        revalidate();
    }

    public ContainerProxy context() {
        return context;
    }

    public PaletteController controller() {
        return controller;
    }

    public void dispose() {
        context(null);
    }

    public Node root() {
        return controller().getRoot().lookup(Node.class);
    }

    private void revalidate() {
        List<ComponentType> newTypes = List.of();
        if (context != null) {
            newTypes = context.supportedTypes();
        }
        if (!types.equals(newTypes)) {
            types = newTypes;
            PaletteFiles.getDefault().addTypes(types);
            filter.types(types);
            controller.refresh();
        }

    }

    public static DefaultComponentPalette create(ContainerProxy container) {
        Filter filter = new Filter();
        DataFolder paletteFolder = PaletteFiles.getDefault().paletteFolder();
        Node rootNode = new PaletteFilterNode(paletteFolder.getNodeDelegate());
        PaletteController controller = PaletteFactory.createPalette(rootNode,
                new DefaultPaletteActions(), filter, null);
        return new DefaultComponentPalette(controller, filter, container);
    }

    private static class Filter extends PaletteFilter {

        private List<ComponentType> types;

        private Filter() {
            this.types = List.of();
        }

        @Override
        public boolean isValidCategory(Lookup lkp) {
            Node category = lkp.lookup(Node.class);
            for (Node item : category.getChildren().getNodes()) {
                if (isValidItem(item.getLookup())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isValidItem(Lookup lkp) {
            ComponentType type = lkp.lookup(ComponentType.class);
            if (type != null) {
                return types.contains(type);
            } else {
                return true;
            }
        }

        private void types(List<ComponentType> types) {
            this.types = types;
        }

    }

}
