package com.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.soapui.Constants;
import com.soapui.ResultBean;

public class HTMLDisplay_AllTCs extends JDialog {

	static ArrayList<ResultBean> beanList;
	static String filePath;
	static JButton sendButton;
	JTable table;

	private void sendButtonActionPerformed(ActionEvent evt) {
		StringBuffer buffer = new StringBuffer();
		boolean recordSelected = false;
		for (int i = 0; i < beanList.size(); i++) {
			Boolean isChecked = Boolean.valueOf(table.getValueAt(i, 0).toString());
		
			if (isChecked ) {
				String requestFileName = FilenameUtils.getName(table.getValueAt(i, 6).toString().replace("<HTML><U>", "").replace("</U></HTML>", ""));
				buffer.append(filePath);
				buffer.append(Constants.CONSOLIDATEDRESULTPATH);
				buffer.append("FAILED_");
				buffer.append(requestFileName);
				buffer.append(".txt");
				buffer.append(",");
				recordSelected = true;
			}
		}
		if (recordSelected) {
			if (buffer.length() > 0) {
				String fielName = buffer.toString();
				new SwingEmailSender(fielName.substring(0, fielName.length() - 1)).setVisible(true);
			}
		} else {
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, "Select alleast one checkbox", "Error Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

		}

	}

	public HTMLDisplay_AllTCs(JFrame parent, ArrayList<ResultBean> beanList1, String filePath1, String url, int passCount, int failCount) {
		super(parent, "Result - Run All TCs");
		beanList = beanList1;
		filePath = filePath1;
		sendButton = new JButton("Send Mail");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				sendButtonActionPerformed(evt);
			}
		});
		String status = null;
		Object columns[] = { "Select", "Status", "Date/Signum Dif", "Env/DB/Code Error", "Request", "Expected Response", "Actual Response", "Time(Sec)" };
		Object rows[][] = new Object[beanList.size()][8];
		double totalTime = 0;
		double time = 0;
		for (int i = 0; i < beanList.size(); i++) {
			status = beanList.get(i).getResultStatus();
			rows[i] = new Object[8];
			rows[i][0] = new Boolean(false);
			if (Constants.FAILED.equalsIgnoreCase(status)) {

				rows[i][1] = "<HTML><U><font color=red>" + beanList.get(i).getResultStatus() + "</font></U></HTML>";
			} else {
				rows[i][1] = "<HTML><font color=green>" + beanList.get(i).getResultStatus() + "</font></HTML>";
			}

			rows[i][2] = beanList.get(i).getDateSignum_Error();
			rows[i][3] = beanList.get(i).getDb_Code_Error();
			rows[i][4] = "<HTML><U>"+beanList.get(i).getRequestFileName()+"</U></HTML>";
			rows[i][5] = "<HTML><U>"+beanList.get(i).getResponseFileName()+"</U></HTML>";
			rows[i][6] = "<HTML><U>"+beanList.get(i).getActualResponseFileName()+"</U></HTML>";
			rows[i][7] = beanList.get(i).getTime();
			if (!"".equalsIgnoreCase(beanList.get(i).getTime().trim())) {
				time = Double.parseDouble(beanList.get(i).getTime());
			}
			totalTime = totalTime + time;
		}
		DefaultTableModel model2 = new DefaultTableModel();
		JTable table2 = new JTable(model2);

		// Create a couple of columns
		model2.addColumn("Col1");
		model2.addColumn("Col2");

		// Append a row
		model2.addRow(new Object[] { "EndPoint URL", url });

		DefaultTableModel model3 = new DefaultTableModel();
		JTable table3 = new JTable(model3);

		// Create a couple of columns
		model3.addColumn("Col1");
		model3.addColumn("Col2");
		model3.addColumn("Col3");
		model3.addColumn("Col4");

		// Append a row
		model3.addRow(new Object[] { "Total Number of TCs", String.valueOf(passCount + failCount), "Total Time taken (Secs)", String.format("%.3f", totalTime) });
		model3.addRow(new Object[] { "Total Number of Passed TCs", String.valueOf(passCount), "Total Number of Failed TCs", String.valueOf(failCount) });

		String columns1[] = { "EndPoint URL", "Total Number of TCs" };
		String rows1[][] = new String[5][4];
		rows1[0][0] = "EndPoint URL";
		rows1[0][1] = url;
		rows1[1][0] = "Total Number of TCs";
		rows1[1][1] = String.valueOf(passCount + failCount);
		rows1[2][0] = "Total Number of Passed TCs";
		rows1[2][1] = String.valueOf(passCount);
		rows1[3][0] = "Total Number of Failed TCs";
		rows1[3][1] = String.valueOf(failCount);
		rows1[4][0] = "Total Time taken (Secs)";
		rows1[4][1] = String.format("%.3f", totalTime);

		this.setLayout(null);

		TableModel model = new DefaultTableModel(rows, columns) {
			public Class getColumnClass(int column) {
				Class returnValue;
				if ((column >= 0) && (column < getColumnCount())) {
					returnValue = getValueAt(0, column).getClass();
				} else {
					returnValue = Object.class;
				}
				return returnValue;
			}

			@Override
			public boolean isCellEditable(int row, int col) {
				if (col == 0) {
					String status = getValueAt(row, 1).toString();
					if (status.contains(Constants.FAILED)) {
						return true;
					}
					return false;
				}else if(col == 1){
					String status = getValueAt(row, 1).toString();
					if (status.contains(Constants.PASS)) {
						return false;
					}
				}
				return true;

			}
		};

		TableModel model1 = new DefaultTableModel(rows1, columns1) {
			public Class getColumnClass(int column) {
				Class returnValue;
				if ((column >= 0) && (column < getColumnCount())) {
					returnValue = getValueAt(0, column).getClass();
				} else {
					returnValue = Object.class;
				}
				return returnValue;
			}
		};

		table = new JTable(model);
		final JTable table1 = new JTable(model1);
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				final JTable target = (JTable) evt.getSource();
				int row = table.rowAtPoint(evt.getPoint());
				int col = table.columnAtPoint(evt.getPoint());
				int col1 = col+1;
				if (row >= 0 && col >= 0) {
					String cellValue1 = target.getValueAt(row, col).toString();
					String cellValue2 = target.getValueAt(row,col1).toString();
					String requestFileName = FilenameUtils.getBaseName(target.getValueAt(row, 4).toString().replace("<HTML><U>", "").replace("</U></HTML>", ""));
					String responseFileName = FilenameUtils.getBaseName(target.getValueAt(row, 6).toString().replace("<HTML><U>", "").replace("</U></HTML>", ""));
					String expectedFileName = FilenameUtils.getBaseName(target.getValueAt(row, 5).toString().replace("<HTML><U>", "").replace("</U></HTML>", ""));

					if (col == 1 && (cellValue1.contains(Constants.FAILED) || (cellValue1.contains(Constants.PASS) && cellValue2.contains("Y")))) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(filePath);
						buffer.append(Constants.COMPARISONRESULTPATH);
						buffer.append(requestFileName);
						buffer.append(".txt");
						String message = null;
						try {
							message = FileUtils.readFileToString(new File(buffer.toString()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JTextArea area1 = new JTextArea();
						JScrollPane sp1 = null;
						Object[] options = { "OK" };
						try {
							area1.setText(message);

							sp1 = new JScrollPane(area1);
							sp1.setPreferredSize(new Dimension(800, 500));

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JOptionPane.showOptionDialog(null, sp1, "Difference", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, options, null);

					} else if (col == 4) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(filePath);
						buffer.append(Constants.ACTUALREQRESPATH);
						buffer.append(requestFileName);
						buffer.append(".TXT");
						String message = null;
						try {
							
							message = FileUtils.readFileToString(new File(buffer.toString()));

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						setContent(message, "Request Message",buffer.toString());

					} else if (col == 5 && !Constants.NOEXPRESPONSEFILEFOUND.equalsIgnoreCase(cellValue1)) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(filePath);
						buffer.append(Constants.EXPECTEDREQRESPATH);
						buffer.append(expectedFileName);
						buffer.append(".TXT");
						String message = null;
						try {
							message = (FileUtils.readFileToString(new File(buffer.toString())));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						setContent(message, "Expected Response",buffer.toString());

					} else if (col == 6) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(filePath);
						buffer.append(Constants.ACTUALREQRESPATH);
						buffer.append(responseFileName);
						buffer.append(".TXT");
						String message = null;
						try {
							message = (FileUtils.readFileToString(new File(buffer.toString())));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						setContent(message, "Actual Response",buffer.toString());

					}
				}
			}
		});
		table.setRowSorter(sorter);
		table.setRowHeight(40);
		table.getColumn("Select").setMinWidth(10);
		table.getColumn("Request").setMinWidth(270);
		table.getColumn("Expected Response").setMinWidth(270);
		table.getColumnModel().getColumn(6).setMinWidth(270);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table2.setRowHeight(30);
		table3.setRowHeight(30);
		table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
		table.getTableHeader().setBackground(Color.decode(Constants.TABLEHEADERCOLOR));
		JTableHeader header = table.getTableHeader();
		Dimension d = new Dimension();
		d.height = 40;
		header.setPreferredSize(d);

		table1.setRowHeight(30);

		JScrollPane pane = new JScrollPane(table);
		table2.setBounds(200, 10, 900, 30);
		table3.setBounds(200, 40, 900, 60);

		// urlLabel.setBounds(100, 130, 100, 30);
		sendButton.setBounds(50, 150, 100, 30);
		pane.setBounds(50, 180, 1200, 450);
		add(pane);
		add(sendButton);
		// add(urlLabel);
		add(table2);
		add(table3);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(1300, 700);
		setLocationRelativeTo(null); // center on screen
		setResizable(false);
	}

	public static void setContent(String text, String content,String fileName) {

		XmlTextPane area1 = new XmlTextPane();
		JScrollPane sp1 = null;
		Object[] options = { "OK","Open in Browser" };
		try {
			area1.setText(text);

			sp1 = new JScrollPane(area1);
			sp1.setPreferredSize(new Dimension(800, 500));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int option=JOptionPane.showOptionDialog(null, sp1, content, JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, options, null);
		if(option==1){
			try {
				Desktop.getDesktop().open(new File(fileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
