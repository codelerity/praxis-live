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
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.ide.components;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class ComponentSettings {
    
    private final static Preferences PREFS = NbPreferences.forModule(ComponentSettings.class);
    
    private final static String KEY_SHOW_TEST = "showTest";
    private final static String KEY_SHOW_DEPRECATED = "showDeprecated";
    private final static String KEY_REWRITE_DEPRECATED = "rewriteDeprecated";
    
    private ComponentSettings() {}
    
    
    public static boolean getShowTest() {
        return PREFS.getBoolean(KEY_SHOW_TEST, false);
    }
    
    public static void setShowTest(boolean value) {
        PREFS.putBoolean(KEY_SHOW_TEST, value);
    }
    
    public static boolean getShowDeprecated() {
        return PREFS.getBoolean(KEY_SHOW_DEPRECATED, false);
    }
    
    public static void setShowDeprecated(boolean value) {
        PREFS.putBoolean(KEY_SHOW_DEPRECATED, value);
    }
    
    public static boolean getRewriteDeprecated() {
        return PREFS.getBoolean(KEY_REWRITE_DEPRECATED, true);
    }
    
    public static void setRewriteDeprecated(boolean value) {
        PREFS.putBoolean(KEY_REWRITE_DEPRECATED, value);
    }
    
}
