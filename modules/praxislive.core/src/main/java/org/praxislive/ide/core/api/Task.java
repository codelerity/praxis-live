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
package org.praxislive.ide.core.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import org.openide.util.Cancellable;

/**
 * A task to be executed. The task may complete within a call to
 * {@link #execute()} or initiate asynchronous operation, such as making calls
 * into the PraxisCORE hub. If the caller needs to know when an asynchronous
 * task is completed, it may add a property change listener.
 * <p>
 * A task may only be executed once.
 * <p>
 * All methods should be called on, and all listeners are fired on, the Swing
 * event thread.
 */
public interface Task extends Cancellable {

    /**
     * Name of property used when firing state change events.
     */
    public final static String PROP_STATE = "state";

    /**
     * The possible states of a Task.
     */
    public static enum State {

        /**
         * The starting state of all tasks before execution has been started.
         */
        NEW,
        /**
         * The state of a task being executed, either inside the
         * {@link #execute()} method or during subsequent asynchronous
         * operations.
         */
        RUNNING,
        /**
         * The state of a task that has been cancelled by the user.
         */
        CANCELLED,
        /**
         * The state of a completed task.
         */
        COMPLETED,
        /**
         * The state of a task that was unable to complete due to error.
         */
        ERROR
    };

    /**
     * Initiate execution of the task. This method returns {@link State#RUNNING}
     * when the task is running asynchronous operations. The caller may add a
     * property listener if it needs to know when and how those operations
     * complete.
     * <p>
     * A task can only be executed once. This method will throw an
     * {@link IllegalStateException} if the task state is not {@link State#NEW}.
     *
     * @return task state
     * @throws IllegalStateException if the task state is not NEW.
     */
    public State execute();

    /**
     * Query the current task state.
     *
     * @return task state
     */
    public State getState();

    /**
     * Add a property change listener.
     *
     * @param listener property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a property change listener.
     *
     * @param listener property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Optional description of this task to be presented to the user.
     *
     * @return optional description
     */
    public default Optional<String> description() {
        return Optional.empty();
    }

    /**
     * Optional log of information generated by execution of this task, suitable
     * for presentation to the user.
     *
     * @return task log
     */
    public default List<String> log() {
        return List.of();
    }

}
