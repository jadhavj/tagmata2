package com.gauronit.tagmata.views;

import java.awt.Color;
import org.jdesktop.application.Action;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import java.awt.Dimension;

public class About extends javax.swing.JDialog {

    public About(java.awt.Frame parent) {

        super(parent);

        initComponents();

        // getRootPane().setDefaultButton(closeButton);
    }

    @Action
    public void closeAboutBox() {
        dispose();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aboutPanel = new javax.swing.JPanel();
        javax.swing.JLabel imageLabel = new javax.swing.JLabel();
        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();
        javax.swing.JLabel appHomepageLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.gauronit.tagmata.Main.class).getContext().getResourceMap(About.class);
        setTitle("About Tagmata"); // NOI18N
        setBackground(resourceMap.getColor("aboutBox.background")); // NOI18N
        setMinimumSize(new Dimension(355, 240));
        setModal(true);
        setName("aboutBox"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(null);

        aboutPanel.setBackground(Color.WHITE); // NOI18N
        aboutPanel.setMaximumSize(new java.awt.Dimension(196, 177));
        aboutPanel.setMinimumSize(new java.awt.Dimension(196, 177));
        aboutPanel.setName("aboutPanel"); // NOI18N
        aboutPanel.setPreferredSize(new java.awt.Dimension(196, 177));

        imageLabel.setIcon(new ImageIcon(About.class.getResource("/com/gauronit/tagmata/resources/gauronit_logo.png"))); // NOI18N
        imageLabel.setName("imageLabel"); // NOI18N

        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));
        appTitleLabel.setText(resourceMap.getString("Application.title")); // NOI18N
        appTitleLabel.setName("appTitleLabel"); // NOI18N

        appVendorLabel.setText(resourceMap.getString("Application.vendor")); // NOI18N
        appVendorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        appVendorLabel.setName("appVendorLabel"); // NOI18N

        appHomepageLabel.setText("<html>&nbsp;&nbsp;<a href='http://www.gauronit.com'>http://www.gauronit.com</a>"); // NOI18N
        appHomepageLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        appHomepageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        appHomepageLabel.setName("appHomepageLabel"); // NOI18N
        appHomepageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                appHomepageLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanelLayout.setHorizontalGroup(
        	aboutPanelLayout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(aboutPanelLayout.createSequentialGroup()
        			.addGroup(aboutPanelLayout.createParallelGroup(Alignment.LEADING)
        				.addGroup(aboutPanelLayout.createSequentialGroup()
        					.addGap(70)
        					.addGroup(aboutPanelLayout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(imageLabel)
        						.addGroup(aboutPanelLayout.createSequentialGroup()
        							.addPreferredGap(ComponentPlacement.RELATED, 35, GroupLayout.PREFERRED_SIZE)
        							.addGroup(aboutPanelLayout.createParallelGroup(Alignment.LEADING)
        								.addComponent(appHomepageLabel)
        								.addComponent(appVendorLabel))
        							.addGap(34))))
        				.addGroup(aboutPanelLayout.createSequentialGroup()
        					.addGap(120)
        					.addComponent(appTitleLabel)))
        			.addContainerGap(83, Short.MAX_VALUE))
        );
        aboutPanelLayout.setVerticalGroup(
        	aboutPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(aboutPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(appTitleLabel)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(imageLabel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(appVendorLabel)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(appHomepageLabel)
        			.addContainerGap(19, Short.MAX_VALUE))
        );
        aboutPanel.setLayout(aboutPanelLayout);

        getContentPane().add(aboutPanel);
        aboutPanel.setBounds(0, 0, 349, 212);
        aboutPanel.getAccessibleContext().setAccessibleParent(aboutPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void appHomepageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_appHomepageLabelMouseClicked
// TODO add your handling code here:
    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
    try {
        java.net.URI uri = new java.net.URI("http://www.gauronit.com");
        desktop.browse(uri);
    } catch (Exception e) {
        e.printStackTrace();
    }
}//GEN-LAST:event_appHomepageLabelMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutPanel;
    // End of variables declaration//GEN-END:variables
}
