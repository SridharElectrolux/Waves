package com.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.soapui.ActionHandler;
import com.soapui.Config;

public class OrganizeTestFilesMapName extends JFrame {

	static Logger log = Logger.getLogger(OrganizeTestFilesMapName.class.getName());
	private JLabel infilePathLabel;
	private JFileChooser infilePathChooser;
	private JTextField infilePathTextField;
	private JButton inbrowseButton;
	private JLabel destfilePathLabel;
	private JFileChooser destfilePathChooser;
	private JTextField destfilePathTextField;
	private JButton destbrowseButton;
	private JButton runButton;
	public static final int MODE_OPEN = 1;
	public static final int MODE_SAVE = 2;
	
	private int mode = MODE_OPEN;
	
	public OrganizeTestFilesMapName() {
		super("Run Organize Test files");
		log.info("Start of OrganizeTestFilesMapName() method");
		try{
		this.setLayout(null);
		log.info("About to load Config file");
		String testFilesDirIn = Config.getInstance().getProperty("testfilesdirectoryin");
		String testFilesDirOut = Config.getInstance().getProperty("testfilesdirectoryout");
		log.info("Loaded Config file");
		
		infilePathLabel = new JLabel("Select Test Files folder");

		infilePathTextField = new JTextField(30);
		infilePathTextField.setText(testFilesDirIn);

		infilePathChooser = new JFileChooser();
		infilePathChooser.setCurrentDirectory(new File(testFilesDirIn));
		//infilePathChooser.setSelectedFile(new File(testFilesDirIn));
		infilePathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		inbrowseButton = new JButton("Browse");
		inbrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				inbrowseButtonActionPerformed(evt);
			}
		});
		
		destfilePathLabel = new JLabel("Select Test Files Organized folder");

		destfilePathTextField = new JTextField(30);
		destfilePathTextField.setText(testFilesDirOut);

		destfilePathChooser = new JFileChooser();
		destfilePathChooser.setCurrentDirectory(new File(testFilesDirOut));
		//destfilePathChooser.setSelectedFile(new File(testFilesDirOut));
		destfilePathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		destbrowseButton = new JButton("Browse");
		destbrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				destbrowseButtonActionPerformed(evt);
			}
		});
		
		runButton = new JButton("Execute");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {

				final JDialog loading = new JDialog(OrganizeTestFilesMapName.this);
				JPanel p1 = new JPanel();//

				BufferedImage myPicture = null;
				try {
					myPicture = ImageIO.read(this.getClass().getResource("avatar-black.gif"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info(e.getMessage());
				}

				p1.add(new JLabel(new ImageIcon(myPicture)), BorderLayout.CENTER);
				loading.setUndecorated(true);
				loading.getContentPane().add(p1);
				loading.pack();
				loading.setLocationRelativeTo(OrganizeTestFilesMapName.this);
				loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				loading.setModal(true);

				SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
					@Override
					protected String doInBackground() throws InterruptedException {
						runButtonOrganizePerformed();
						return null;
					}

					@Override
					protected void done() {
						loading.dispose();
					}
				};
				worker.execute();
				loading.setVisible(true);
				try {
					worker.get();
				} catch (Exception e1) {
					log.info(e1.getMessage());
				}

			}
		});
		
		infilePathLabel.setBounds(100, 180, 400, 30);
		infilePathTextField.setBounds(300, 180, 400, 30);
		inbrowseButton.setBounds(900, 180, 100, 30);
		
		destfilePathLabel.setBounds(100, 240, 400, 30);
		destfilePathTextField.setBounds(300, 240, 400, 30);
		destbrowseButton.setBounds(900, 240, 100, 30);
		
		runButton.setBounds(100, 300, 100, 30);
		
		add(infilePathLabel);
		add(infilePathTextField);
		add(inbrowseButton);
		add(destfilePathLabel);
		add(destfilePathTextField);
		add(destbrowseButton);
		add(runButton);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(2000, 1040);
		setLocationRelativeTo(null); // center on screen
		}catch(Exception e)
		{
			//e.printStackTrace();
			log.info(e.getMessage());
		}
		log.info("End of OrganizeTestFilesMapName() method");
	}
	
	private void runButtonOrganizePerformed() {
		log.info("Start - OrganizeTestFilesMapName() - runButtonOrganizePerformed");
		String inpath;
		String outpath;
		inpath = infilePathChooser.getSelectedFile().getPath();
		outpath = destfilePathChooser.getSelectedFile().getPath();
		if ("".equalsIgnoreCase(inpath) || "".equalsIgnoreCase(outpath)) {
			Object[] options = { "OK" };
			log.info("runButtonOrganizePerformed - File Paths cant be blank");
			JOptionPane.showOptionDialog(null, "File Paths can't be blank", "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
		} else {
			try {
				ThreadGroup threadGroup = new ThreadGroup("OrgThreadGrp");
				ActionHandler.RunThreadedOrganizeTestFile(inpath, outpath,threadGroup);
				while(true){
					if(threadGroup.activeCount()==0)
						break;
					else
						Thread.sleep(10000);
				}
				//ActionHandler.OrganizeTestFiles(inpath, outpath);
				log.info("OrganizeTestFilesMapName - Finished - ");
			} catch (Exception e) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, "Error in execution" + e.getMessage(), "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options,
						options[0]);
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}
		log.info("End - OrganizeTestFilesMapName() - runButtonOrganizePerformed");
	}
	private void inbrowseButtonActionPerformed(ActionEvent evt) {
		log.info("Start of inbrowseButtonActionPerformed() method");
		if (mode == MODE_OPEN) {
			if (infilePathChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				infilePathTextField.setText(infilePathChooser.getSelectedFile().getAbsolutePath());
			}
		} else if (mode == MODE_SAVE) {
			if (infilePathChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				infilePathTextField.setText(infilePathChooser.getSelectedFile().getAbsolutePath());
			}
		}
		log.info("EndS of inbrowseButtonActionPerformed() method");
	}
	
	private void destbrowseButtonActionPerformed(ActionEvent evt) {
		log.info("Start of inbrowseButtonActionPerformed() method");
		if (mode == MODE_OPEN) {
			if (destfilePathChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				destfilePathTextField.setText(destfilePathChooser.getSelectedFile().getAbsolutePath());
			}
		} else if (mode == MODE_SAVE) {
			if (destfilePathChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				destfilePathTextField.setText(destfilePathChooser.getSelectedFile().getAbsolutePath());
			}
		}
		log.info("EndS of inbrowseButtonActionPerformed() method");
	}
}
