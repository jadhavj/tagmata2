package com.gauronit.tagmata.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class MigrationHelp extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextPane textPane;

	/**
	 * Create the dialog.
	 */
	public MigrationHelp(JFrame parent, boolean modal) {
		super(parent, modal);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			textPane = new JTextPane();
			textPane.setText("To migrate to a different machine and still have all" +
					" the Tagmata cards with you, just follow these steps:\n" +
					"    1. Install Tagmata on the new machine.\n" +
					"    2. From the older machine, copy folder 'indexes' found in the\n" +
					"       installation folder. Typically on a Windows machine it could\n" +
					"       be found at the location, 'C:\\Program File\\Tagmata'\n" +
					"    3. On the destination machine, at the same location mentioned\n" +
					"       above, delete the 'indexes' folder and paste 'indexes' folder\n" +
					"       you copied from the older machine.\n" +
					"\n\n" +
					"That's all you need to do. Now your Tagmata installation on the new\n" +
					"machine will just work like your previous one with all of your cards\n" +
					"that you have saved till now.  ");
			textPane.setEditable(false);
		}
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(textPane, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(textPane, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
