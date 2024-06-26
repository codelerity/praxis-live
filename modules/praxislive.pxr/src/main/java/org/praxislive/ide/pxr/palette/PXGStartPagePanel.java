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
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.ide.pxr.palette;

import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.praxislive.ide.core.ui.spi.StartPagePanelProvider;

/**
 *
 */
@Messages({
    "TITLE_PXGStartPanel=Additional components",
    "BTN_Install=Install",
    "BTN_Reinstall=Reinstall",
    "LBL_UpdateAvailable=New components download available",
    "LBL_Installing=Installing ...",
    "LBL_Installed=Installed",
    "LBL_Error=Error - please download manually from https://www.praxislive.org"
})
public class PXGStartPagePanel extends javax.swing.JPanel {

    /**
     * Creates new form ExamplesStartPagePanel
     */
    public PXGStartPagePanel() {
        initComponents();
        actionButton.setVisible(false);
        refresh();
    }

    private boolean refresh() {
        if (Utils.canInstall()) {
            actionButton.setVisible(true);
            if (!Utils.isInstalled()) {
                actionButton.setText(Bundle.BTN_Install());
                updateStatus.setText("");
            } else if (!Utils.isLatest()) {
                actionButton.setText(Bundle.BTN_Install());
                updateStatus.setText(Bundle.LBL_UpdateAvailable());
            } else {
                actionButton.setText(Bundle.BTN_Reinstall());
                updateStatus.setText("");
            }
        }
        return true;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        updateStatus = new javax.swing.JLabel();
        actionButton = new javax.swing.JButton();
        infoScrollPane = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(updateStatus, org.openide.util.NbBundle.getMessage(PXGStartPagePanel.class, "LBL_CheckingForUpdates")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(actionButton, org.openide.util.NbBundle.getMessage(PXGStartPagePanel.class, "BTN_DownloadPXG")); // NOI18N
        actionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionButtonActionPerformed(evt);
            }
        });

        infoScrollPane.setBorder(null);

        infoTextArea.setEditable(false);
        infoTextArea.setColumns(20);
        infoTextArea.setLineWrap(true);
        infoTextArea.setRows(3);
        infoTextArea.setText(org.openide.util.NbBundle.getMessage(PXGStartPagePanel.class, "INFO_PXGStartPanel")); // NOI18N
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setBorder(null);
        infoTextArea.setOpaque(false);
        infoScrollPane.setViewportView(infoTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(updateStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(actionButton))
                    .addComponent(infoScrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(infoScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(actionButton)
                    .addComponent(updateStatus))
                .addContainerGap())
        );

        infoScrollPane.setOpaque(false);
        infoScrollPane.getViewport().setOpaque(false);
    }// </editor-fold>//GEN-END:initComponents

    private void actionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionButtonActionPerformed
        updateStatus.setText(Bundle.LBL_Installing());
        Utils.RP.post(() -> {
            try {
                Utils.install();
                EventQueue.invokeLater(() -> {
                    refresh();
                    updateStatus.setText(Bundle.LBL_Installed());
                });
            } catch (IOException ex) {
                EventQueue.invokeLater(() -> {
                    updateStatus.setText(Bundle.LBL_Error());
                    actionButton.setVisible(false);
                    Exceptions.printStackTrace(ex);
                });
                
            }
        });
    }//GEN-LAST:event_actionButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionButton;
    private javax.swing.JScrollPane infoScrollPane;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JLabel updateStatus;
    // End of variables declaration//GEN-END:variables

    @ServiceProvider(service = StartPagePanelProvider.class, position = 200)
    public static class Provider implements StartPagePanelProvider {

        private final PXGStartPagePanel panel = new PXGStartPagePanel();
        
        @Override
        public String getTitle() {
            return Bundle.TITLE_PXGStartPanel();
        }

        @Override
        public JPanel getPanel() {
            return panel;
        }

        @Override
        public boolean refresh() {
            return panel.refresh();
        }

    }

}
