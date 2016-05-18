package fileSearch;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI.SelectionModelPropertyChangeHandler;
import javax.swing.table.AbstractTableModel;

import java.awt.Window.Type;
import java.awt.GridBagLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

//import progressBarDemo.ProgressBarDemo.Task;

import com.jgoodies.forms.layout.FormSpecs;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Desktop;

public class FileSearcher extends JFrame implements ActionListener, PropertyChangeListener, ListSelectionListener {
	// Define Variables
	private JTextField RootFolder;
	private JTextField Filecontain;

	private JProgressBar progressBar = new JProgressBar();
	private JButton btnSearch = new JButton("Search");
	private Task task;

	private JList resultList;
	private DefaultListModel listModel_name;
	private JScrollPane listScrollPane;
	private JPanel resultPane;
	private String[] TableHeader = { "File Name", "Folder" };
	private JTable table = new JTable(new MyTableModel());
	// private Object[][] Result = {};

	private JLabel lblBaseFolder = new JLabel("Base Folder:");
	private JLabel lblContains = new JLabel("File Contains:");
	private JCheckBox chckbxCaseSensitive = new JCheckBox("Case sensitive");
	private JButton btnNewButton = new JButton("...");

	private String folderpath, keywords;
	private boolean CaseSensitive;

	private static CopyOnWriteArrayList<MyFile> SearchResult = new CopyOnWriteArrayList<>();
	private static AtomicInteger FileSearched = new AtomicInteger(0);
	private int TotalFiles;
	private Helper helper = new Helper();

	public FileSearcher() {

		init();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FileSearcher tool = new FileSearcher();
				tool.setVisible(true);
			}
		});
	}

	public void init() {
		// File searcher
		this.setTitle("File Searcher 1.0");
		this.setSize(536, 449);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);

		// Label: Base Folder
		lblBaseFolder.setBounds(36, 10, 99, 42);
		getContentPane().add(lblBaseFolder);

		// Label: WordsContains
		lblContains.setBounds(36, 47, 139, 42);
		getContentPane().add(lblContains);

		// TextField: RootFolder
		RootFolder = new JTextField();
		RootFolder.setEditable(false);
		RootFolder.setBounds(130, 21, 341, 21);
		getContentPane().add(RootFolder);
		RootFolder.setColumns(10);

		// TextField: Filecontain
		Filecontain = new JTextField();
		Filecontain.setBounds(130, 58, 341, 21);
		getContentPane().add(Filecontain);
		Filecontain.setColumns(10);

		// Checkbox for Case
		chckbxCaseSensitive.setBounds(236, 89, 109, 23);
		getContentPane().add(chckbxCaseSensitive);

		// ProgressBar

		progressBar.setBounds(10, 118, 500, 23);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		getContentPane().add(progressBar);

		// Search
		// Button-----------------------------------------------------------------------
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				reset();
				actionSearch();
			}
		});
		btnSearch.setBounds(378, 89, 93, 23);
		btnSearch.addActionListener(this);
		getContentPane().add(btnSearch);

		// Button: Choose
		// Folder---------------------------------------------------------------
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser Fchooser = new JFileChooser();
				Fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				Fchooser.showDialog(new JLabel(), "Ñ¡Ôñ");
				File selected = Fchooser.getSelectedFile();
				RootFolder.setText(selected.getPath());
			}
		});
		btnNewButton.setBounds(481, 20, 29, 23);
		getContentPane().add(btnNewButton);

		// Result List initialize
		listModel_name = new DefaultListModel();
		// listModel_name.addElement(new JButton("FileName"));
		resultList = new JList(listModel_name);
		resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultList.setVisibleRowCount(10);
		resultList.addListSelectionListener(this);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() == 2) {
					String Filepath = (String) table.getValueAt(table.getSelectedRow(), 1)
							+ "\\"+ (String)table.getValueAt(table.getSelectedRow(), 0);
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(Filepath));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		// listScrollPane = new JScrollPane(resultList);
		listScrollPane = new JScrollPane(table);
		listScrollPane.setBounds(10, 150, 500, 250);
		getContentPane().add(listScrollPane);
	}

	private void actionSearch() {
		keywords = Filecontain.getText();
		CaseSensitive = chckbxCaseSensitive.isSelected();
		TotalFiles = helper.FileCount(RootFolder.getText());
		ThreadS root = new ThreadS(RootFolder.getText(), Filecontain.getText(), SearchResult, FileSearched,
				CaseSensitive);
		new Thread(root).start();
		// do{
		// try {
		// Thread.sleep(10);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }while(Thread.activeCount()>3);

	}

	private void reset() {
		FileSearched.set(0);
		SearchResult.clear();
	}

	class Task extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {
			// Initialize progress property.
			setProgress(0);
			while (progressBar.getValue() < 100) {
				// Sleep
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) {
				}

				// Update progress.
				progressBar.setValue(100 * FileSearched.get() / TotalFiles);
				setProgress(progressBar.getValue());
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnSearch.setEnabled(true);
			setCursor(null); // turn off the wait cursor
		}
	}

	public void actionPerformed(ActionEvent evt) {
		btnSearch.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// Instances of javax.swing.SwingWorker are not reusuable, so
		// we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {

		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			MyTableModel model = (MyTableModel) table.getModel();
			for (int n = table.getRowCount(); n < SearchResult.size(); n++) {
				// listModel_name.addElement(SearchResult.get(n).getName());
				table.setValueAt(SearchResult.get(n).getName(), n, 0);
				table.setValueAt(SearchResult.get(n).getParent(), n, 1);
				model.fireTableDataChanged();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	class MyTableModel extends AbstractTableModel {
		private String[] columnNames = TableHeader;
		// private Object[][] data = new Object[][]{};
		private Vector<Object[]> data = new Vector<Object[]>();

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data.get(row)[col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			if (data.size() > row) {
				data.get(row)[col] = value;

			} else {
				Object[] newRow = new Object[getColumnCount()];
				newRow[col] = value;
				data.add(newRow);
			}
			fireTableCellUpdated(row, col);
		}

	}
}
