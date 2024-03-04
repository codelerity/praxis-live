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
package org.praxislive.ide.core.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 *
 */
public class SerialTasks extends AbstractTask {
    
    private final Queue<Task> tasks;
    private final Listener listener;

    private Task activeTask;
    private Consumer<Task> before;
    private Consumer<Task> after;
    
    
    public SerialTasks(List<Task> tasks) {
        this.tasks = new ArrayDeque<>(tasks);
        listener = new Listener();
    }
    
    public SerialTasks(Task ... tasks) {
        this(Arrays.asList(tasks));
    }

    public int remaining() {
        return tasks.size();
    }
    
    protected void beforeExecute() {}
    
    protected void afterExecute() {}
    
    protected void beforeTask(Task task) {}
    
    protected void afterTask(Task task) {}
    
    @Override
    protected final void handleExecute() throws Exception {
        beforeExecute();
        if (tasks.isEmpty()) {
            updateState(State.COMPLETED);
            afterExecute();
            return;
        }
        handleTaskQueue();
    }
    
    private void handleTaskQueue() {
        while (!tasks.isEmpty()) {
            activeTask = tasks.poll();
            State state = activeTask.getState();
            if (state == State.NEW) {
                beforeTask(activeTask);
                state = activeTask.execute();
            }
            if (state == State.COMPLETED) {
                afterTask(activeTask);
            }
            if (state == State.RUNNING) {
                activeTask.addPropertyChangeListener(listener);
                return;
            }
            if (state == State.ERROR || state == State.CANCELLED) {
                afterTask(activeTask);
                updateState(state);
                afterExecute();
                return;
            }
        }
        updateState(State.COMPLETED);
        afterExecute();
    }

    @Override
    protected boolean handleCancel() {
        boolean cancelled = true;
        if (activeTask != null) {
            cancelled = activeTask.cancel();
        }
        if (cancelled) {
            for (Task task : tasks) {
                task.cancel();
            }
        }
        return cancelled;
    }
    
    
    
    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() == activeTask;
            if (getState() == State.RUNNING && 
                    evt.getSource() == activeTask) {
                State taskState = activeTask.getState();
                if (taskState == State.RUNNING) {
                    assert false;
                    return;
                }
                activeTask.removePropertyChangeListener(this);
                if (taskState == State.COMPLETED) {
                    afterTask(activeTask);
                    handleTaskQueue();
                } else if (taskState == State.CANCELLED ||
                        taskState == State.ERROR) {
                    afterTask(activeTask);
                    updateState(taskState);
                    afterExecute();
                }
            }
            
        }
        
    }
    
}
