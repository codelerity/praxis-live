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
package org.praxislive.ide.pxr.editors;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.syntax.Token;
import org.praxislive.core.syntax.Tokenizer;
import org.praxislive.core.types.*;
import org.praxislive.ide.properties.EditorSupport;
import org.praxislive.ide.properties.PraxisProperty;
import org.praxislive.ide.properties.SyntaxUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.praxislive.ide.properties.PraxisProperty.SubCommandEditor;

/**
 *
 */
public class ResourceEditor extends EditorSupport
        implements SubCommandEditor, ExPropertyEditor {

    private final static Logger LOG = Logger.getLogger(ResourceEditor.class.getName());
    private PropertyEnv env;
    private FileObject workingDir;
    private boolean allowEmpty;
    private List<String> suggested;

    public ResourceEditor(PraxisProperty property, ArgumentInfo info) {
        Object dir = property.getValue("workingDir");
        if (dir instanceof File) {
            workingDir = FileUtil.toFileObject((File)dir);
        }
        PMap props = info.properties();
        allowEmpty = true;
//        property.setValue("canEditAsText", Boolean.FALSE);
//        Value arg = props.get(ArgumentInfo.KEY_SUGGESTED_VALUES);
//        if (arg != null) {
//            try {
//                PArray arr = PArray.coerce(arg);
//                suggested = new ArrayList<String>(arr.getSize());
//                for (Value val : arr) {
//                    suggested.add(val.toString());
//                }
//                 property.setValue("canEditAsText", Boolean.TRUE);
//            } catch (ArgumentFormatException ex) {
//                // no op
//            }
//        }
    }


    @Override
    public String getPraxisInitializationString() {

        URI uri = getURI();
        if (uri == null) {
            return "{}";
        } else {
            if (workingDir != null) {
                uri = workingDir.toURI().relativize(uri);
            }
//            uri = base.relativize(uri);
            if (uri.isAbsolute()) {
                return uri.toString();
            } else {
                return "[file " + SyntaxUtils.escapeQuoted(uri.getPath()) + "]";
            }
        }

    }

    private URI getURI() {
        try {
            Value arg = (Value) getValue();
            if (arg.isEmpty()) {
                return null;
            } else {
                return PResource.from(arg).orElseThrow().value();
            }
        } catch (Exception ex) {
            return null;
        }
    }


    public String getDisplayName() {
        return "Resource Editor";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        // ignore?  Property sheet is calling with empty String on cancel???
        
        // Exceptions.printStackTrace(new Exception());

    }

    @Override
    public String getAsText() {
        return null;
    }
    
        @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        FontMetrics fm = gfx.getFontMetrics();
        gfx.drawString(getValue().toString(), box.x, box.y
                + (box.height - fm.getHeight()) / 2 + fm.getAscent());
    }
    
    
    @Override
    public void setFromCommand(String command) throws Exception {
        Iterator<Token> toks = new Tokenizer(command).iterator();
        Token cmd = toks.next();
        Token file = toks.next();
        if (cmd.getType() != Token.Type.PLAIN || !"file".equals(cmd.getText())) {
            throw new IllegalArgumentException("Not file command");
        }
        switch (file.getType()) {
            case PLAIN:
            case QUOTED:
            case BRACED:
                URI path = workingDir.toURI().resolve(new URI(null, null, file.getText(), null));
                LOG.log(Level.FINE, "Setting path to {0}", path);
                setValue(PResource.of(path));
                break;
            default:
                throw new IllegalArgumentException("Couldn't parse file");
        }
    }
    
    

    @Override
    public String[] getSupportedCommands() {
        return new String[]{"file"};
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

    @Override
    public boolean supportsCustomEditor() {
        return env != null;
    }

    @Override
    public Component getCustomEditor() {
        return new ResourceCustomEditor(this, workingDir, getURI(), env);
    }
}
