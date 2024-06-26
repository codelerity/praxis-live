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
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "STEP_LauncherJDK=Include Java"
})
final class EmbedRuntimeJDKVisual extends JPanel {
    


    EmbedRuntimeJDKVisual() {
        initComponents();
    }

    @Override
    public String getName() {
        return Bundle.STEP_LauncherJDK();
    }
    
    boolean includeJDK() {
        return embedJDKCheckbox.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        launcherJDKLabel = new javax.swing.JLabel();
        embedJDKCheckbox = new javax.swing.JCheckBox();
        launcherJDKHelp = new javax.swing.JTextPane();

        org.openide.awt.Mnemonics.setLocalizedText(launcherJDKLabel, org.openide.util.NbBundle.getMessage(EmbedRuntimeJDKVisual.class, "LBL_LauncherJDK")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(embedJDKCheckbox, org.openide.util.NbBundle.getMessage(EmbedRuntimeJDKVisual.class, "LBL_IncludeJDKCheckbox")); // NOI18N

        launcherJDKHelp.setEditable(false);
        launcherJDKHelp.setBorder(null);
        launcherJDKHelp.setFont(launcherJDKHelp.getFont().deriveFont(launcherJDKHelp.getFont().getSize()-2f));
        launcherJDKHelp.setForeground(java.awt.SystemColor.inactiveCaption);
        launcherJDKHelp.setText(org.openide.util.NbBundle.getMessage(EmbedRuntimeJDKVisual.class, "HELP_LauncherJDK")); // NOI18N
        launcherJDKHelp.setFocusable(false);
        launcherJDKHelp.setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(embedJDKCheckbox))
                            .addComponent(launcherJDKLabel))
                        .addGap(0, 406, Short.MAX_VALUE))
                    .addComponent(launcherJDKHelp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(launcherJDKLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(embedJDKCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launcherJDKHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(358, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox embedJDKCheckbox;
    private javax.swing.JTextPane launcherJDKHelp;
    private javax.swing.JLabel launcherJDKLabel;
    // End of variables declaration//GEN-END:variables
}
