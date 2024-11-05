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
package org.praxislive.ide.pxr.palette;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 */
@Messages({
    "TITLE_PXGWidget=Additional components",
    "TXT_PXGWidget=Custom components can be downloaded and added to the palette to provide a wider range of functionality.",
    "LBL_Install=Install components",
    "STATUS_Install=Download and install additional components",
    "LBL_Reinstall=Reinstall components",
    "STATUS_Reinstall=Reinstall additional components",
    "TXT_UpdateAvailable=New components available for download",
    "TXT_Installing=Installing...",
    "TXT_Installed=Installed",
    "TXT_Error=Installation error"
})
public class PXGWidget implements DashboardWidget {

    private static enum State {
        START, INSTALLING, INSTALLED, ERROR
    };

    private final List<WidgetElement> elements;
    private final Set<DashboardDisplayer.Panel> active;

    private State state;

    public PXGWidget() {
        elements = new ArrayList<>();
        active = new HashSet<>();
        state = State.START;
        refresh();
    }

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_PXGWidget();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.copyOf(elements);
    }

    @Override
    public void showing(DashboardDisplayer.Panel panel) {
        active.add(panel);
    }

    @Override
    public void hidden(DashboardDisplayer.Panel panel) {
        active.remove(panel);
    }

    private void refresh() {
        elements.clear();
        elements.add(WidgetElement.text(Bundle.TXT_PXGWidget()));
        switch (state) {
            case START -> {
                if (!Utils.isInstalled()) {
                    elements.add(WidgetElement.action(new InstallAction()));
                } else if (!Utils.isLatest()) {
                    elements.add(WidgetElement.aside(Bundle.TXT_UpdateAvailable()));
                    elements.add(WidgetElement.action(new InstallAction()));
                } else {
                    elements.add(WidgetElement.action(new InstallAction(true)));
                }
            }
            case INSTALLING -> {
                elements.add(WidgetElement.aside(Bundle.TXT_Installing()));
            }
            case INSTALLED -> {
                elements.add(WidgetElement.aside(Bundle.TXT_Installed()));
            }
            case ERROR -> {
                elements.add(WidgetElement.unavailable(Bundle.TXT_Error()));
                elements.add(WidgetElement.action(new InstallAction()));
            }
        }
        active.forEach(DashboardDisplayer.Panel::refresh);
    }

    private class InstallAction extends AbstractAction {

        private InstallAction() {
            this(false);
        }

        private InstallAction(boolean reinstall) {
            super(reinstall ? Bundle.LBL_Reinstall() : Bundle.LBL_Install());
            putValue(SHORT_DESCRIPTION, reinstall
                    ? Bundle.STATUS_Reinstall() : Bundle.STATUS_Install());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(() -> {
                state = State.INSTALLING;
                refresh();
            });
            Utils.RP.post(() -> {
                try {
                    Utils.install();
                    EventQueue.invokeLater(() -> {
                        state = State.INSTALLED;
                        refresh();
                    });
                } catch (Exception ex) {
                    EventQueue.invokeLater(() -> {
                        Exceptions.printStackTrace(ex);
                        state = State.ERROR;
                        refresh();
                    });
                }

            });
        }

    }

}
