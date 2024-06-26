/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2021 Neil C Smith.
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
package org.praxislive.ide.project.wizard;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "STEP_LauncherName=Launcher name"
})
final class EmbedRuntimeNameVisual extends JPanel implements DocumentListener {

    private final EmbedRuntimeNamePanel wizardPanel;

    EmbedRuntimeNameVisual(EmbedRuntimeNamePanel wizardPanel, String projectName) {
        this.wizardPanel = wizardPanel;
        initComponents();
        launcherNameField.setText(projectName);
        launcherNameField.getDocument().addDocumentListener(this);
    }

    @Override
    public String getName() {
        return Bundle.STEP_LauncherName();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    String getLauncherName() {
        return launcherNameField.getText();
    }
    
    private void update() {
        wizardPanel.validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        launcherNameField = new javax.swing.JTextField();
        launcherNameLabel = new javax.swing.JLabel();
        launcherNameHelp = new javax.swing.JTextPane();

        org.openide.awt.Mnemonics.setLocalizedText(launcherNameLabel, org.openide.util.NbBundle.getMessage(EmbedRuntimeNameVisual.class, "LBL_LauncherName")); // NOI18N

        launcherNameHelp.setEditable(false);
        launcherNameHelp.setBorder(null);
        launcherNameHelp.setFont(launcherNameHelp.getFont().deriveFont(launcherNameHelp.getFont().getSize()-2f));
        launcherNameHelp.setForeground(java.awt.SystemColor.inactiveCaption);
        launcherNameHelp.setText(org.openide.util.NbBundle.getMessage(EmbedRuntimeNameVisual.class, "HELP_LauncherName")); // NOI18N
        launcherNameHelp.setFocusable(false);
        launcherNameHelp.setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(launcherNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(launcherNameHelp, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .addComponent(launcherNameField))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(launcherNameLabel)
                    .addComponent(launcherNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(launcherNameHelp, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField launcherNameField;
    private javax.swing.JTextPane launcherNameHelp;
    private javax.swing.JLabel launcherNameLabel;
    // End of variables declaration//GEN-END:variables
}
