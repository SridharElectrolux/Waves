package com.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.soapui.Config;
import com.soapui.Constants;

public class Configuration extends JFrame {
	static Logger log = Logger.getLogger(Configuration.class.getName());
	public static void main(String s[]){
	new Configuration(new JFrame()).setVisible(true);
	}
	JTable table = null;
	static JButton sendButton;
	static JButton addButton;
	static Properties prop = Config.getInstance().getConfig();
JFrame parentFrame ;
	private void sendButtonActionPerformed(ActionEvent evt) {
		log.info(" Start of sendButtonActionPerformed");
		int row = table.getRowCount();
		String status = null;
		for (int j = 0; j < row; j++) {

			prop.setProperty(table.getValueAt(j, 0).toString().trim(), table.getValueAt(j, 1).toString().trim());
			try {
				prop.store(new FileOutputStream(Constants.CONFIGPATH), "");
				status = "Success";
			} catch (Exception e) {
				status = "Error - >" + e.getMessage();
				e.printStackTrace();
			}
		}
		Object[] options = { "OK", "Cancel" };
		if ("Success".equalsIgnoreCase(status)) {

			int option = JOptionPane.showOptionDialog(null, "File Successfully Updated", "Success Message", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null,
					options, options[0]);
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
		log.info(" End of sendButtonActionPerformed");

	}
	private void addButtonActionPerformed(ActionEvent evt) {
		new AddProperty(this,parentFrame).setVisible(true);
	
	}

	public Configuration(JFrame parentFrame) {
		
		super("Update Configuration Property");
		log.info(" Start of Update Configuration Property ");
		
		this.parentFrame=parentFrame;
		sendButton = new JButton("Update");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				sendButtonActionPerformed(evt);
			}
		});
		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		Object columns[] = { "Key", "Value" };
		Set<Object> s = (prop.keySet());

		Object rows[][] = new Object[prop.keySet().size()][2];

		int i = 0;
		for (Object object : s) {
			rows[i] = new Object[2];
			rows[i][0] = object;
			rows[i][1] = prop.getProperty(object.toString());
			i++;
		}

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
		};

		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table = new JTable(model);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setRowSorter(sorter);
		table.getColumnModel().getColumn(0).setMinWidth(180);		
		table.getColumnModel().getColumn(1).setMinWidth(1000);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(50);
		//table.setBackground(Color.decode("#F0F8FF"));
		table.getColumn("Value").setCellRenderer(new TextAreaRenderer());
		table.getColumn("Value").setCellEditor(new TextAreaEditor());
		JScrollPane pane = new JScrollPane(table);
		JTableHeader header = table.getTableHeader();
		Dimension d = new Dimension();
		d.height = 50;
		table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
		table.getTableHeader().setBackground(Color.decode(Constants.TABLEHEADERCOLOR));
		header.setPreferredSize(d);
		pane.setBounds(50, 100, 1000, 450);
		add(pane);
		sendButton.setBounds(50, 70, 100, 30);
		add(sendButton);
		addButton.setBounds(150, 70, 100, 30);
		add(addButton);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(1100, 700);
		setLocationRelativeTo(null); // center on screen
		setResizable(false);
		log.info(" End of Update Configuration Property ");
	}

	class TextAreaRenderer extends JScrollPane implements TableCellRenderer {
		JTextArea textarea;

		public TextAreaRenderer() {
			textarea = new JTextArea();
			textarea.setLineWrap(true);
			textarea.setWrapStyleWord(true);
			// textarea.setBorder(new TitledBorder("This is a JTextArea"));
			getViewport().add(textarea);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
				textarea.setForeground(table.getSelectionForeground());
				textarea.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
				textarea.setForeground(table.getForeground());
				textarea.setBackground(table.getBackground());
			}

			textarea.setText((String) value);
			textarea.setCaretPosition(0);
			return this;
		}
	}

	class TextAreaEditor extends DefaultCellEditor {
		protected JScrollPane scrollpane;
		protected JTextArea textarea;

		public TextAreaEditor() {
			super(new JCheckBox());
			scrollpane = new JScrollPane();
			textarea = new JTextArea();
			textarea.setLineWrap(true);
			textarea.setWrapStyleWord(true);
			// textarea.setBorder(new TitledBorder("This is a JTextArea"));
			scrollpane.getViewport().add(textarea);
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			textarea.setText((String) value);

			return scrollpane;
		}

		public Object getCellEditorValue() {
			return textarea.getText();
		}
	}
}
