package com.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.soapui.ActionHandler;
import com.soapui.Config;
import com.soapui.ResultBean;

public class IBMAllTCRunner extends JFrame {
	static Logger log = Logger.getLogger(IBMAllTCRunner.class.getName());
	JFrame parent ;
	
	private JLabel radioLabel;
	private JRadioButton simulateButton;
	private JLabel filePathlabel;
	private JFileChooser filePathChooser;
	private JTextField filePathTextField;
	private JButton browseButton;
	public static final int MODE_OPEN = 1;
	public static final int MODE_SAVE = 2;

	
	boolean cancel = false;
	// JPanel newPanel = new JPanel();
	private int mode = MODE_OPEN;
	

	public IBMAllTCRunner(JFrame parent) {
		super("Run All Testcases");
		log.info("Start of AllTCRunner() method");
		this.parent =parent;
		// newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
		// "Run All Testcases"));
		// add(newPanel);
		setLayout(null);

		radioLabel = new JLabel("Simulate");
		simulateButton = new JRadioButton();
		
		filePathlabel = new JLabel("Select filePath");

		filePathTextField = new JTextField(30);

		filePathChooser = new JFileChooser();
		filePathChooser.setCurrentDirectory(new File(Config.getInstance().getProperty("defaultdirectory")));

		browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});
		final JButton submitButton = new JButton("Execute");
		submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				
				 final JDialog loading = new JDialog(IBMAllTCRunner.this);
				    JPanel p1 = new JPanel();//
				    
				    BufferedImage myPicture = null;
					try {
						myPicture = ImageIO.read(this.getClass().getResource("avatar-black.gif"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.info(e.getMessage());
					}
					final JButton stopButton = new JButton("Stop"); 
			    p1.add(new JLabel(new ImageIcon(myPicture)), BorderLayout.CENTER);
			    p1.add(stopButton);
				    loading.setUndecorated(true);
				    loading.getContentPane().add(p1);
				    loading.pack();
				    loading.setLocationRelativeTo(IBMAllTCRunner.this);
				    loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				    loading.setModal(true);

				    final SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
				        @Override
				        protected String  doInBackground() throws InterruptedException{
				        		submitButtonActionPerformed();
				        		return null;
				        }
				        @Override
				        protected void done() {
				            loading.dispose();
				        }
				    };
				    stopButton.addActionListener(new ActionListener() {
					    @Override
					    public void actionPerformed(ActionEvent evt) {
					    	int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to cancel?","Warning",JOptionPane.YES_NO_OPTION);
					    	if(dialogResult==0)
					    		worker.cancel(true);
					    }

					    });
				    worker.execute();
				    loading.setVisible(true);
				    try {
				        worker.get();
				    } catch (Exception e1) {
				    	log.info(e1.getMessage());//e1.printStackTrace();
				    }
		
				
				
			
			}
		});


		radioLabel.setBounds(100,200,400,30);
		simulateButton.setBounds(200,200,100,30);
		filePathlabel.setBounds(100, 240, 400, 30);
		filePathTextField.setBounds(200, 240, 400, 30);
		browseButton.setBounds(620, 240, 100, 30);
		submitButton.setBounds(200, 290, 100, 30);

		
		add(radioLabel);
		add(simulateButton);
		add(filePathlabel);
		add(filePathTextField);
		add(browseButton);

		add(submitButton);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 680);
		setLocationRelativeTo(null); // center on screen
		/*if (endPointComboBox.getSelectedItem() != null) {
			String url = endPointComboBox.getSelectedItem().toString();
			if (url.startsWith("https://")) {
				String urlAuth = Config.getInstance().getProperty(url.substring(url.indexOf("https://") + "https://".length(), url.indexOf(".")));
				String[] list = urlAuth.split(",");
				userNameTextField.setText(list[0]);
				pwdTextField.setText(list[1]);
				userNameLabel.setVisible(true);
				pwdLabel.setVisible(true);
				userNameTextField.setVisible(true);
				pwdTextField.setVisible(true);
			} else {
				userNameLabel.setVisible(false);
				pwdLabel.setVisible(false);
				userNameTextField.setVisible(false);
				pwdTextField.setVisible(false);
			}
		}*/
		log.info("End of AllTCRunner() method");
	}

	

	private void submitButtonActionPerformed() {
		log.info("Start of submitButtonActionPerformed() method");
		String filePath = filePathTextField.getText();
		String ftpUrl = Config.getInstance().getProperty("SftpEndpoint");
		String ftpUser = Config.getInstance().getProperty("SftpUser");
		String ftpPassword = Config.getInstance().getProperty("SftpPassword");
		String ftpProxy = Config.getInstance().getProperty("Proxy");
		int ftpProxyPort = NumberUtils.toInt(Config.getInstance().getProperty("ProxyPort"));
		if ( "".equalsIgnoreCase(filePath)) {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, "EndPoint URL / File Path can't be blank", "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options,
					options[0]);
		} else {
			try {
					ArrayList<ResultBean> beanList = new ArrayList<ResultBean>();
					ActionHandler a = new  ActionHandler();
					String result = "";
					if(!Thread.currentThread().isInterrupted()){
						JSch jsch = new JSch();
				        Session session = jsch.getSession(ftpUser, ftpUrl, 22);
				        ChannelSftp sftpChannel = null ;
				        session.setPassword(ftpPassword);
				        session.setConfig("StrictHostKeyChecking", "no");
				        session.setConfig("PreferredAuthentications", "password");
				        if(!ftpProxy.equals(""))
				        	session.setProxy(new ProxySOCKS5(ftpProxy, ftpProxyPort));
				     
				        if(!simulateButton.isSelected()){
				        	log.info("Establishing Connection...");
				        	session.connect();
				        	log.info("Connection established.");
						    log.info("Creating SFTP Channel.");
					        sftpChannel = (ChannelSftp) session.openChannel("sftp");
					        sftpChannel.connect();
					        log.info("SFTP Channel created.");
				        }
				     
						
				        
				     
				        
				        result =a.runAllTestCases(beanList, filePath, sftpChannel,simulateButton.isSelected());
					//	result =a.runAllSoapRequest(beanList,url, filePath, authentication);
				        if(!simulateButton.isSelected()){
				        sftpChannel.disconnect();
				        session.disconnect();
				        }
					}
					log.info("result"+result);
					
					if(result.indexOf("#")!=-1){
						String[] array = result.split("#");
						Object[] options = { "OK","Cancel" };
				
					if(!Thread.currentThread().interrupted()){
					}
					}else{
						Object[] options = { "OK" };
						JOptionPane.showOptionDialog(null, "Error in execution > "+result, "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
					}
					
//				}
				
			} catch (Exception e) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, "Error in execution\n"+e.getMessage(), "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				log.info(e.getMessage());
			}
		}
		log.info("End of submitButtonActionPerformed() method");
	}

	private void browseButtonActionPerformed(ActionEvent evt) {
		log.info("Start of browseButtonActionPerformed() method");
		if (mode == MODE_OPEN) {
			if (filePathChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				filePathTextField.setText(filePathChooser.getSelectedFile().getAbsolutePath());
			}
		} else if (mode == MODE_SAVE) {
			if (filePathChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				filePathTextField.setText(filePathChooser.getSelectedFile().getAbsolutePath());
			}
		}
		log.info("EndS of browseButtonActionPerformed() method");
	}


	
	public static void main(String[] a){
		new IBMAllTCRunner(new JFrame()).setVisible(true);
	}

}
