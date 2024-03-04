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
package org.praxislive.ide.components.api;

import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.praxislive.core.ComponentType;

/**
 *
 */
public final class Icons {

     private static final Image DEFAULT_ICON = ImageUtilities.loadImage(
            "org/praxislive/ide/components/resources/default-icon.png", true);
     
     private Icons() {}
     
     public static Image getIcon(ComponentType type) {
        return Lookup.getDefault().lookupAll(ComponentIconProvider.class).stream()
                .flatMap(p -> p.getIcon(type).stream())
                .findFirst()
                .orElse(DEFAULT_ICON);
    }
     
    
}
