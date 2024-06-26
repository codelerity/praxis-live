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

import java.util.Optional;
import java.util.Set;
import org.praxislive.ide.core.api.Task;

/**
 * A provider interface for handling tasks based on the lifecycle of roots.
 * Instances should be registered in the project lookup.
 */
public interface RootLifecycleHandler {

    /**
     * Provide a task to run when one or more roots are about to be deleted.
     *
     * @param description user readable description of event triggering deletion
     * @param rootIDs roots to be deleted
     * @return optional task to run
     */
    public Optional<Task> getDeletionTask(String description, Set<String> rootIDs);

}
