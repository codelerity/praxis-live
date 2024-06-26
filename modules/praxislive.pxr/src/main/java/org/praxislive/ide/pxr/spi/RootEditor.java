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

package org.praxislive.ide.pxr.spi;

//import javax.swing.Action;
import java.util.Optional;
import javax.swing.Action;
import javax.swing.JComponent;
import org.praxislive.ide.model.RootProxy;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.praxislive.ide.project.api.PraxisProject;

/**
 *
 */
public abstract class RootEditor {

    public void componentShowing() {
        // no op hook
    }

    public void componentActivated() {
        // no op hook
    }

    public void componentDeactivated() {
        // no op hook
    }

    public void componentHidden() {
        // no op hook
    }

    public void sync() {
        //no op hook - called to sync data with model prior to saving
    }

    public void dispose() {
        // no op hook
    }

    public Action[] getActions() {
        return new Action[0];
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    public abstract JComponent getEditorComponent();

 
    public static interface Provider {

        public Optional<RootEditor> createEditor(PraxisProject project,
                FileObject file, RootProxy model);
    
    }

    
}
