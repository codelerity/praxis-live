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
package org.praxislive.ide.midi;

import java.awt.Image;
import java.util.Optional;
import org.praxislive.core.ComponentType;
import org.praxislive.ide.components.api.ComponentIconProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ComponentIconProvider.class)
public class MidiIconProvider implements ComponentIconProvider {

    private final static Image MIDI_ICON = ImageUtilities.loadImage(
            "org/praxislive/ide/midi/resources/midi.png", true);

    @Override
    public Optional<Image> getIcon(ComponentType type) {
        if ("root:midi".equals(type.toString())
                || type.toString().startsWith("midi:")) {
            return Optional.of(MIDI_ICON);
        }
        return Optional.empty();
    }
}
