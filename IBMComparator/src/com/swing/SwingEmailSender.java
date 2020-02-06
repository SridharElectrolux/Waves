package com.swing;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.soapui.Config;
import com.soapui.JavaMail;

/**
 * A Swing application that allows sending e-mail messages from a SMTP server.
 * 
 * @author www.codejava.net
 * 
 */
public class SwingEmailSender extends JFrame {

	private JLabel filePathlabel;
	private JFileChooser filePathChooser;
	private JTextField filePathTextField;
	private JButton browseButton;
	public static final int MODE_OPEN = 1;
	public static final int MODE_SAVE = 2;
	private int mode = MODE_OPEN;

	private JLabel labelTo = new JLabel("To: ");
	private JLabel labelSubject = new JLabel("Subject: ");

	private JTextField fieldTo = new JTextField(Config.getInstance().getProperty("mail_tolist"), 30);
	private JTextField fieldSubject = new JTextField(Config.getInstance().getProperty("mail_subject"), 30);
	private JLabel labelcc = new JLabel("cc: ");

	private JTextField fieldcc = new JTextField(Config.getInstance().getProperty("mail_cclist"), 30);

	private JButton buttonSend = new JButton("SEND");

	private JTextArea textAreaMessage = new JTextArea(Config.getInstance().getProperty("mail_content"), 10, 30);

	private GridBagConstraints constraints = new GridBagConstraints();
	String attachFileName;

	public SwingEmailSender(String attachFileName) {
		super("Swing E-mail Sender Program");
		this.attachFileName = attachFileName;
		// set up layout
		setLayout(new GridBagLayout());
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		setupForm();

		pack();
		setLocationRelativeTo(null); // center on screen
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void setupForm() {
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(labelTo, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(fieldTo, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		add(labelcc, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(fieldcc, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		add(labelSubject, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(fieldSubject, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		constraints.fill = GridBagConstraints.BOTH;
		buttonSend.setFont(new Font("Arial", Font.BOLD, 16));
		add(buttonSend, constraints);

		buttonSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				buttonSendActionPerformed(event);
			}
		});

		filePathlabel = new JLabel("Attached");

		filePathTextField = new JTextField(attachFileName, 30);

		filePathChooser = new JFileChooser();
		filePathChooser.setCurrentDirectory(new File(Config.getInstance().getProperty("defaultdirectory")));

		browseButton = new JButton("Attach File");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});

		constraints.gridx = 0;
		constraints.gridy = 3;
		add(filePathlabel, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(filePathTextField, constraints);

		constraints.gridx = 2;
		// constraints.fill = GridBagConstraints.HORIZONTAL;
		add(browseButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 6;
		constraints.gridwidth = 3;

		add(new JScrollPane(textAreaMessage), constraints);
	}

	private void buttonSendActionPerformed(ActionEvent event) {
		if (!validateFields()) {
			return;
		}
		String toAddress = fieldTo.getText();
		String subject = fieldSubject.getText();
		String message = textAreaMessage.getText();
		try {
			String status = JavaMail.sendMail(Config.getInstance().getProperty("mail_from"), Config.getInstance().getProperty("mail_password"), toAddress, fieldcc.getText(), subject, message,
					attachFileName);
			if(status.equalsIgnoreCase("Success")){
				JOptionPane.showMessageDialog(this, "The e-mail has been sent successfully!");
				this.dispose();
			}
			
			

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error while sending the e-mail: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean validateFields() {
		if (fieldTo.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "Please enter To address!", "Error", JOptionPane.ERROR_MESSAGE);
			fieldTo.requestFocus();
			return false;
		}

		if (fieldSubject.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "Please enter subject!", "Error", JOptionPane.ERROR_MESSAGE);
			fieldSubject.requestFocus();
			return false;
		}

		if (textAreaMessage.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "Please enter message!", "Error", JOptionPane.ERROR_MESSAGE);
			textAreaMessage.requestFocus();
			return false;
		}

		return true;
	}


	private void browseButtonActionPerformed(ActionEvent evt) {

		if (mode == MODE_OPEN) {
			if (filePathChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				filePathTextField.setText(filePathChooser.getSelectedFile().getAbsolutePath());
			}
		} else if (mode == MODE_SAVE) {
			if (filePathChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				filePathTextField.setText(filePathChooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
}