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
package org.praxislive.ide.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.praxislive.core.Call;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.SystemManagerService;
import org.praxislive.ide.core.api.Task;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.praxislive.base.AbstractAsyncControl;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.Control;
import org.praxislive.core.Info;
import org.praxislive.core.RootHub;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PReference;
import org.praxislive.ide.core.api.AbstractIDERoot;
import org.praxislive.ide.project.api.PraxisProject;
import org.praxislive.ide.project.spi.RootLifecycleHandler;
import org.praxislive.ide.project.ui.ProjectDialogManager;

/**
 *
 */
class ServicesOverride extends AbstractIDERoot implements RootHub.ServiceProvider {

    private final static Logger LOG = Logger.getLogger(ServicesOverride.class.getName());
    private final static ComponentInfo INFO = Info.component()
            .merge(ComponentProtocol.API_INFO)
            .merge(RootManagerService.API_INFO)
            .control(RootFactoryService.NEW_ROOT_INSTANCE, RootFactoryService.NEW_ROOT_INSTANCE_INFO)
            .control(SystemManagerService.SYSTEM_EXIT, SystemManagerService.SYSTEM_EXIT_INFO)
            .build();
    
    private final DefaultPraxisProject project;
    private final Set<String> knownRoots;
    
    private ComponentAddress defaultRootManagerService;
    private ComponentAddress defaultRootFactoryService;

    ServicesOverride(DefaultPraxisProject project) {
        this.project = project;
        knownRoots = new LinkedHashSet<>();
        registerControl(RootManagerService.ADD_ROOT, new AddRootControl());
        registerControl(RootManagerService.REMOVE_ROOT, new RemoveRootControl());
        registerControl(RootManagerService.ROOTS, new RootsControl());
        registerControl(RootFactoryService.NEW_ROOT_INSTANCE, new NewRootInstanceControl());
        registerControl(SystemManagerService.SYSTEM_EXIT, new ExitControl());
    }

    @Override
    public ComponentInfo getInfo() {
        return INFO;
    }
    
    @Override
    public List<Class<? extends Service>> services() {
        return List.of(RootManagerService.class,
                RootFactoryService.class,
                SystemManagerService.class);
    }
    
    Set<String> getKnownUserRoots() {
        return knownRoots;
    }

    private ComponentAddress getDefaultRootManagerService() throws ServiceUnavailableException {
        if (defaultRootManagerService == null) {
            ComponentAddress[] services = getLookup().find(Services.class)
                    .map(s -> s.locateAll(RootManagerService.class).toArray(ComponentAddress[]::new))
                    .orElseThrow(ServiceUnavailableException::new);

            defaultRootManagerService = services[services.length - 1];
        }
        return defaultRootManagerService;
    }
    
    private ComponentAddress getDefaultRootFactoryService() throws ServiceUnavailableException {
        if (defaultRootFactoryService == null) {
            ComponentAddress[] services = getLookup().find(Services.class)
                    .map(s -> s.locateAll(RootFactoryService.class).toArray(ComponentAddress[]::new))
                    .orElseThrow(ServiceUnavailableException::new);

            defaultRootFactoryService = services[services.length - 1];
        }
        return defaultRootFactoryService;
    }

    private class AddRootControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            ControlAddress to = ControlAddress.of(getDefaultRootManagerService(), RootManagerService.ADD_ROOT);
            return Call.create(to, call.to(), call.time(), call.args());
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            Call active = getActiveCall();
            knownRoots.add(active.args().get(0).toString());
            return getActiveCall().reply(call.args());
        }

    }

    private class RootsControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            ControlAddress to = ControlAddress.of(getDefaultRootManagerService(), RootManagerService.ROOTS);
            return Call.create(to, call.to(), call.time(), call.args());
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            return getActiveCall().reply(call.args());
        }

    }

    private class RemoveRootControl implements Control {

        private final Map<String, List<Call>> pending;
        private final Map<Integer, String> forwarded;

        private RemoveRootControl() {

            pending = new HashMap<>();
            forwarded = new HashMap<>();
        }

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.isRequest()) {
                processInvoke(call, router);
            } else {
                processResponse(call, router, call.isError());
            }
        }

        private void processInvoke(Call call, PacketRouter router) throws Exception {
            String rootID = call.args().get(0).toString();
            List<Call> calls = pending.get(rootID);
            if (calls == null) {
                LOG.log(Level.FINE, "No pending calls found for root removal /{0}", rootID);
                var description = "Deleting /" + rootID;
                var rootSet = Set.of(rootID);
                var tasks = project.getLookup().lookupAll(RootLifecycleHandler.class).stream()
                        .flatMap(handler -> handler.getDeletionTask(description, rootSet).stream())
                        .collect(Collectors.toList());
                if (tasks.isEmpty()) {
                    LOG.log(Level.FINE, "No tasks found for root removal /{0}", rootID);
                    boolean remove = ProjectDialogManager.get(project)
                            .confirm("Remove root?", "Remove root " + call.args().get(0).toString());
                    if (remove) {
                        forwardCall(rootID, call, router);
                    } else {
                        router.route(call.error(List.of()));
                        return; // don't allow call to be added to pending
                    }

                } else {
                    if (tasks.size() > 1) {
                        LOG.log(Level.WARNING, "More than one deletion task for root /{0}\nOnly first task will be run", rootID);
                    }
                    LOG.log(Level.FINE, "Starting root deletion task");
                    Task task = tasks.get(0);
                    task.execute();
                    if (task.getState() == Task.State.RUNNING) {
                        LOG.log(Level.FINE, "Task still running - add PCL");
                        task.addPropertyChangeListener(new TaskListener(task, rootID));
                    } else if (task.getState() == Task.State.COMPLETED) {
                        LOG.log(Level.FINE, "Task completed synchronously - forwarding call");
                        forwardCall(rootID, call, router);
                    } else {
                        LOG.log(Level.FINE, "Synchronous task error");
                        router.route(call.error(List.of()));
                        return; // don't allow call to be added to pending
                    }
                }
                calls = new ArrayList<Call>(1);
                calls.add(call);
                pending.put(rootID, calls);
            } else {
                LOG.log(Level.FINE, "Pending call found for root removal /{0}", rootID);
                calls.add(call);
            }

        }

        private void forwardCallFromTask(String rootID) throws Exception {
            LOG.log(Level.FINE, "Task Completed OK - forwarding call to remove /{0}", rootID);
            List<Call> inbound = pending.get(rootID);
            PacketRouter router = getRouter();
            forwardCall(rootID, inbound.get(0), router);
        }

        private void forwardCall(String rootID, Call call, PacketRouter router) throws Exception {
            ControlAddress to = ControlAddress.of(getDefaultRootManagerService(), RootManagerService.REMOVE_ROOT);
            Call forward = Call.create(to, call.to(), call.time(), call.args());
            forwarded.put(forward.matchID(), rootID);
            router.route(forward);
        }

        private void processResponse(Call call, PacketRouter router, boolean error) {
            String rootID = forwarded.remove(call.matchID());
            List<Call> inbound = pending.remove(rootID);
            for (Call in : inbound) {
                Call response;
                if (error) {
                    response = in.error(call.args());
                } else {
                    response = in.reply(call.args());
                }
                router.route(response);
            }
        }

        private void taskError(String rootID) {
            List<Call> inbound = pending.remove(rootID);
            PacketRouter router = getRouter();
            for (Call in : inbound) {
                router.route(in.error(List.of()));
            }
        }

        private class TaskListener implements PropertyChangeListener {

            private String rootID;
            private Task task;

            private TaskListener(Task task, String rootID) {
                this.task = task;
                this.rootID = rootID;
            }

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (task.getState() == Task.State.COMPLETED) {
                    try {
                        forwardCallFromTask(rootID);
                        return;
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                taskError(rootID);

            }
        }
    }
    
    private class NewRootInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            if ("root:gui".equals(call.args().get(0).toString())) {
                try {
                    var loader = Lookup.getDefault().lookup(ClassLoader.class);
                    var guiClass = Class.forName(
                            "org.praxislive.ide.pxr.gui.DockableGuiRoot",
                            true, loader);
                    var instance = guiClass.getDeclaredConstructor(PraxisProject.class)
                            .newInstance(project);
                    return call.reply(PReference.of(instance));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    // fall through to default
                }
            }
            ControlAddress to = ControlAddress.of(getDefaultRootFactoryService(),
                    RootFactoryService.NEW_ROOT_INSTANCE);
            return Call.create(to, call.to(), call.time(), call.args());
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            return getActiveCall().reply(call.args());
        }
    }

    private class ExitControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.isRequest()) {
                for (String id : knownRoots) {
                    ControlAddress to = ControlAddress.of("/" + id + ".stop");
                    Call msg = Call.createQuiet(to, call.from(),
                            getExecutionContext().getTime());
                    router.route(msg);
                }
            } else {
                // no op?
            }

        }
    }
    
    
}
