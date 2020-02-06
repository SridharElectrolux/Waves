package com.swing;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.apache.log4j.Logger;

public class IBMComparatorTool extends JFrame {
	static Logger log = Logger.getLogger(IBMComparatorTool.class.getName());
	public static void main(String args[]) {
		log.info(" = = = = = = = Inside SoapTester main method = = = = = = = ");
		new IBMComparatorTool().setVisible(true);
	}

	public IBMComparatorTool() {
		super("IBM Comparator Tool");
		log.info(" = = = = = = = Inside SoapTester() method = = = = = = = ");
		JTabbedPane tabs = new JTabbedPane();
		tabs.setUI(new BasicTabbedPaneUI() {
			@Override
			protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 25; // manipulate this number however you please.
			}
		});
		tabs.addTab("Run All TCs", new IBMAllTCRunner(this).getContentPane());
		tabs.addTab("Organize Test Files By Receiver",new OrganizeTestFilesRcv().getContentPane() );
		tabs.addTab("Organize Test Files By MsgType",new OrganizeTestFilesMsgTyp().getContentPane() );
		tabs.addTab("Organize Test Files By MapName",new OrganizeTestFilesMapName().getContentPane() );
		tabs.addTab("Organize Test Files By FlowName",new OrganizeTestFilesFlowName().getContentPane() );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 700);
		//setLocationRelativeTo(null); // center on screen
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		//setUndecorated(true);
		add(tabs);
		log.info(" = = = = = = = End of SoapTester() method = = = = = = = ");
	}

}
