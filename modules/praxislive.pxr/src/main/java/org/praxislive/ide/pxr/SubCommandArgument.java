/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Neil C Smith.
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

import org.praxislive.core.Value;

/**
 *
 */
public class SubCommandArgument extends Value {

    private String commandLine;

    public SubCommandArgument(String commandLine) {
        if (commandLine == null) {
            throw new NullPointerException();
        }
        this.commandLine = commandLine;
    }

    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public String toString() {
        return commandLine; // ? do we want to return this here ?
    }

    @Override
    public int hashCode() {
        return commandLine.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SubCommandArgument && commandLine.equals(obj.toString());
    }

}
