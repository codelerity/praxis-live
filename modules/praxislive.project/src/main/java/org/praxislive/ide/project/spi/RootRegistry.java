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
package org.praxislive.ide.project.spi;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import org.praxislive.ide.model.RootProxy;

/**
 * A provider of root proxies. Instances should be registered in the project
 * lookup.
 */
public interface RootRegistry {

    /**
     * Name of roots property. Used in property change events when the available
     * root proxies changes.
     */
    public static final String ROOTS = "roots";

    /**
     * Find a proxy for the given root ID, if available from this provider.
     *
     * @param id root ID
     * @return root proxy of available
     */
    public Optional<RootProxy> find(String id);

    /**
     * Find all root proxies available from this provider.
     *
     * @return all root proxies
     */
    public List<RootProxy> findAll();

    /**
     * Add a property change listener
     *
     * @param listener property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a property change listener
     *
     * @param listener property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
