/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2023 Neil C Smith.
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

import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;
import org.praxislive.base.AbstractRootContainer;
import org.praxislive.base.BindingContextControl;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;

/**
 *
 */
public abstract class AbstractIDERoot extends AbstractRootContainer {
    
    private final boolean startOnActivation;
    
    private BindingContextControl bindings;
    private Lookup lookup;

    public AbstractIDERoot() {
        this(true);
    }
    
    public AbstractIDERoot(boolean startOnActivation) {
        this.startOnActivation = startOnActivation;
    }

    
    @Override
    public Lookup getLookup() {
        return lookup == null ? super.getLookup() : lookup;
    }
    
    @Override
    protected final void activating() {
        bindings = new BindingContextControl(ControlAddress.of(getAddress(), "_bindings"),
                getExecutionContext(),
                getRouter());
        registerControl("_bindings", bindings);
        lookup = Lookup.of(super.getLookup(), bindings);
        var delegate = new SwingDelegate();
        attachDelegate(delegate);
        delegate.start();
    }

    @Override
    protected final void terminating() {}

    protected void setup() {}
    
    protected void dispose() {}

    private class SwingDelegate extends Delegate {
        
        private final AtomicBoolean pollQueued = new AtomicBoolean();

        private Timer timer;

        private void start() {
            EventQueue.invokeLater(() -> {
                setup();
                if (startOnActivation) {
                    setRunning();
                }
                timer = new Timer(50, e -> update());
                timer.start();
            });
        }

        private void update() {
            boolean ok = doUpdate(getRootHub().getClock().getTime());
            if (!ok) {
                timer.stop();
                dispose();
                detachDelegate(this);
            }
        }

        @Override
        protected void onQueueReceipt() {
            if (pollQueued.compareAndSet(false, true)) {
                EventQueue.invokeLater(() -> {
                    pollQueued.set(false);
                    doPollQueue();
                });
            }
        }

    }
    
}
