package fileSearch;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Window.Type;
import java.awt.GridBagLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import com.jgoodies.forms.layout.FormSpecs;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


public class FileSearcher extends JFrame{
	//Define Variables
	private JTextField RootFolder;
	private JTextField Filecontain;
	private JProgressBar progressBar = new JProgressBar();
	private JLabel lblBaseFolder = new JLabel("Base Folder:");
	private JLabel lblContains = new JLabel("File Contains:");
	private JButton btnSearch = new JButton("Search");
	private JCheckBox chckbxCaseSensitive = new JCheckBox("Case sensitive");
	private JButton btnNewButton = new JButton("...");
	private JScrollPane scrollPane = new JScrollPane();
	
	private String folderpath, keywords;
	private boolean CaseSensitive;
	
	private static CopyOnWriteArrayList<MyFile> SearchResult=new CopyOnWriteArrayList<>();
	private static AtomicInteger FileSearched = new AtomicInteger(0);
	private Helper helper = new Helper();
	
	
	public FileSearcher() {
		
		init();
	}
	
	public static void main(String[] args)
	 {
	    FileSearcher tool= new FileSearcher();
	    tool.setVisible(true);
	 }

	public void init(){
		//File searcher
		this.setTitle("File Searcher 1.0");
		this.setSize(536,449);
		getContentPane().setLayout(null);
		
		//Label: Base Folder 
		lblBaseFolder.setBounds(36, 10, 99, 42);
		getContentPane().add(lblBaseFolder);
		
		//Label: WordsContains
		lblContains.setBounds(36, 47, 139, 42);
		getContentPane().add(lblContains);
		
		//TextField: RootFolder
		RootFolder = new JTextField();
		RootFolder.setEditable(false);
		RootFolder.setBounds(130, 21, 341, 21);
		getContentPane().add(RootFolder);
		RootFolder.setColumns(10);
		
		//TextField: Filecontain
		Filecontain = new JTextField();
		Filecontain.setBounds(130, 58, 341, 21);
		getContentPane().add(Filecontain);
		Filecontain.setColumns(10);
		
		//Checkbox for Case
		chckbxCaseSensitive.setBounds(236, 89, 109, 23);
		getContentPane().add(chckbxCaseSensitive);
		
		//ProgressBar
		
		progressBar.setBounds(10, 118, 500, 23);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		getContentPane().add(progressBar);

		//Search Button-----------------------------------------------------------------------
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				reset();
				actionSearch();
			}
		});
		btnSearch.setBounds(378, 89, 93, 23);
		getContentPane().add(btnSearch);
		
		//Button: Choose Folder---------------------------------------------------------------
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
		
		//Search result pane
		scrollPane.setBounds(10, 166, 500, 234);
		getContentPane().add(scrollPane);
	}
	
	private void actionSearch(){
		keywords = Filecontain.getText();
		CaseSensitive = chckbxCaseSensitive.isSelected();
		int TotalFiles = helper.FileCount(RootFolder.getText());
		ThreadS root = new ThreadS(RootFolder.getText(), Filecontain.getText(), SearchResult, FileSearched, CaseSensitive);
		new Thread(root).start();
		do{
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while(Thread.activeCount()>2);
	}
	
	private void reset(){
		FileSearched.set(0);
		SearchResult.clear();
		
	}
}

