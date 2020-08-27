/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.ide.core.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.praxislive.ide.core.Core;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;
import org.praxislive.ide.core.ui.spi.StartPagePanelProvider;

/**
 * Welcome Page
 */
@ConvertAsProperties(
        dtd = "-//org.praxislive.ide.core.ui//Start//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "StartTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "org.praxislive.ide.core.ui.StartTopComponent")
@ActionReference(path = "Menu/Window", position = 2000)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_StartAction",
        preferredID = "StartTopComponent")
@Messages({
    "CTL_StartAction=Start Page",
    "CTL_StartTopComponent=Praxis LIVE",
    "HINT_StartTopComponent=Welcome to Praxis LIVE",
})
public final class StartTopComponent extends TopComponent {

    private final List<StartPagePanelProvider> panelProviders;
    private final Font headerFont;

    public StartTopComponent() {
        initComponents();
        setFocusable(true);
        setName(Bundle.CTL_StartTopComponent());
        setToolTipText(Bundle.HINT_StartTopComponent());
        putClientProperty("activateAtStartup", Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, true);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, true);

        Font defFont = UIManager.getFont("controlFont");
        if (defFont != null) {
            headerFont = defFont.deriveFont(Font.PLAIN, defFont.getSize2D() * 1.6f);
        } else {
            headerFont = null;
        }
        
        panelProviders = new ArrayList<>();
        initPanels();
        
    }

    
    private void initPanels() {
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 2, 4, 2);
        
        panelProviders.add(new VersionCheckStartPanel.Provider());
        panelProviders.add(new InfoStartPanel.Provider());
        
        panelProviders.addAll(Lookup.getDefault().lookupAll(StartPagePanelProvider.class));
        
        panelProviders.forEach(pp -> {
            String title = pp.getTitle();
            if (title != null && !title.isEmpty()) {
                container.add(createTitleLabel(pp.getTitle()), gbc);
            }
            JPanel panel = pp.getPanel();
            panel.setBackground(Color.BLACK);
            panel.setAlignmentX(LEFT_ALIGNMENT);
            container.add(panel, gbc);
        });
        
        gbc.weighty = 1;
        container.add(Box.createVerticalGlue(), gbc);
    }
    
    private JLabel createTitleLabel(String title) {
        JLabel label = new JLabel(title);
        label.setFont(headerFont);
        label.setBackground(Color.BLACK);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        logo = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        container = new javax.swing.JPanel();
        logo1 = new javax.swing.JButton();

        setBackground(java.awt.Color.black);
        setOpaque(true);

        mainPanel.setBackground(new java.awt.Color(0, 0, 0));
        mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/praxislive/ide/core/ui/resources/praxislive-text.png"))); // NOI18N
        logo.setBorderPainted(false);
        logo.setContentAreaFilled(false);
        logo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoActionPerformed(evt);
            }
        });

        scrollPane.setBackground(new java.awt.Color(0, 0, 0));
        scrollPane.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)));

        container.setBackground(new java.awt.Color(0, 0, 0));
        container.setLayout(new java.awt.GridBagLayout());
        scrollPane.setViewportView(container);

        logo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/praxislive/ide/core/ui/resources/praxislive-bg.png"))); // NOI18N
        logo1.setBorderPainted(false);
        logo1.setContentAreaFilled(false);
        logo1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logo1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(logo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 354, Short.MAX_VALUE)
                        .addComponent(logo1)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(logo)
                    .addComponent(logo1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void logoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoActionPerformed
        Utils.openExternalLink(Utils.WEBSITE_LINK);
    }//GEN-LAST:event_logoActionPerformed

    private void logo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_logo1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel container;
    private javax.swing.JButton logo;
    private javax.swing.JButton logo1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        refresh();
        requestFocusInWindow();
    }

    private void refresh() {
        panelProviders.forEach(StartPagePanelProvider::refresh);
    }
    
//    void configureStartPage() {
//        String startPage = Core.getInstance().getPreferences().get("start-page", null);
//        if (startPage != null) {
//            try {
//                htmlPane.setPage(startPage);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private static void checkInfo() {
        StartTopComponent start = StartTopComponent.find();
        if (start != null && start.isVisible()) {
            start.refresh();
        }
//        if (isUpdateAvailable()) {
//            NotificationDisplayer.getDefault().notify(
//                    Bundle.LBL_NewVersion(),
//                    ImageUtilities.loadImageIcon(
//                            "org/praxislive/ide/core/ui/resources/info_icon.png", true),
//                    Bundle.LBL_NewVersionInfo(),
//                    null);
//        }
    }

//    private static boolean isUpdateAvailable() {
//        Core core = Core.getInstance();
//        String current = core.getVersion();
//        String latest = core.getLatestAvailableVersion();
//        return !Objects.equals(current, latest);
//    }

    static StartTopComponent find() {
        TopComponent tc = WindowManager.getDefault().findTopComponent("StartTopComponent");
        if (tc instanceof StartTopComponent) {
            return (StartTopComponent) tc;
        }
        assert false;
        return null;
    }

    @OnShowing
    public static class Installer implements Runnable {

        @Override
        public void run() {
            WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {
                @Override
                public void beforeLoad(WindowSystemEvent event) {
                }

                @Override
                public void afterLoad(WindowSystemEvent event) {
                }

                @Override
                public void beforeSave(WindowSystemEvent event) {
                    boolean show = Utils.isShowStart();
                    TopComponent start = StartTopComponent.find();
                    if (start != null) {
                        if (show) {
                            start.open();
                            start.requestActive();
                        } else {
                            start.close();
                        }
                    }

                }

                @Override
                public void afterSave(WindowSystemEvent event) {
                }
            });

            Core.getInstance().getPreferences().addPreferenceChangeListener(
                    new PreferenceChangeListener() {

                        @Override
                        public void preferenceChange(PreferenceChangeEvent evt) {
                            update();

                        }
                    });
            
            update();

        }

        private void update() {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    StartTopComponent.checkInfo();
                }

            });
        }

    }

}
