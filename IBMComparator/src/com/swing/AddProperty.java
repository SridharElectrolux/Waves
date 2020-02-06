package com.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.soapui.ActionHandler;
import com.soapui.Config;
import com.soapui.Constants;
import com.soapui.ResultBean;

public class AddProperty extends JFrame {
	JFrame frame = null;
	private JLabel keylabel;
	private JTextField keyTextField;

	private JLabel valuelabel;
	private JTextField valueTextField;
	JFrame parentFrame;
	private JButton button;

	public AddProperty(JFrame frame, JFrame parentFrame) {
		super("Add Property");
		this.parentFrame = parentFrame;
		this.frame = frame;
		setLayout(null);

		keylabel = new JLabel("Enter Key");
		keyTextField = new JTextField(30);

		valuelabel = new JLabel("Enter Value");
		valueTextField = new JTextField(30);

		button = new JButton("Add Property");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});

		keylabel.setBounds(100, 80, 600, 30);
		keyTextField.setBounds(300, 80, 400, 30);

		valuelabel.setBounds(100, 130, 600, 30);
		valueTextField.setBounds(300, 130, 400, 30);

		button.setBounds(300, 180, 130, 30);

		add(keylabel);
		add(keyTextField);

		add(valuelabel);
		add(valueTextField);

		add(button);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 300);
		setLocationRelativeTo(null); // center on screen

	}

	private void browseButtonActionPerformed(ActionEvent evt) {
		String keyString = keyTextField.getText().trim();
		String valueString = valueTextField.getText().trim();
		String status = null;
		if ("".equalsIgnoreCase(keyString) || "".equalsIgnoreCase(valueString)) {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, "Key / Value can't be empty", "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		} else {
			Properties prop = Config.getInstance().getConfig();
			prop.setProperty(keyString, valueString);
			try {
				prop.store(new FileOutputStream(Constants.CONFIGPATH), "");
				status = "Success";
			} catch (Exception e) {
				status = "Error - >" + e.getMessage();
				e.printStackTrace();
			}
			Object[] options = { "OK", "Cancel" };
			if ("Success".equalsIgnoreCase(status)) {
				int option = JOptionPane.showOptionDialog(null, "Successfully Added", "Success Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, options,
						options[0]);
				if (option == 0) {
					this.dispose();
					parentFrame.dispose();
				}

			} else {
				int option = JOptionPane.showOptionDialog(null, status, "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (option == 0) {
					this.dispose();
				}
			}
		}
	}

}
