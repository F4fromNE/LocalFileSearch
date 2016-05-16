package fileSearch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class ThreadS implements Runnable {
	private String Path, KeyWords;
	private CopyOnWriteArrayList<MyFile> Result;	
	private AtomicInteger Count;
	private boolean CaseS;
	public ThreadS(String a, String b, CopyOnWriteArrayList<MyFile> c, AtomicInteger FileSearched, boolean CaseSensitive){
		Path = a;
		KeyWords = b;
		Result = c;
		Count = FileSearched;
		CaseS = CaseSensitive;
	}
	public void run(){
		MyFile root = new MyFile(Path);
		List<MyFile> Content = root.myListFiles();
		for(MyFile file:Content){

			if(file.isDirectory()){
				ThreadS nThread = new ThreadS(file.getPath(), KeyWords, Result, Count, CaseS);
				new Thread(nThread).start();
			}
			else{
				String filename = file.getName();
				
				if(CaseS == false){
					filename = filename.toLowerCase();
					KeyWords = KeyWords.toLowerCase();
				}
				
				if(filename.contains(KeyWords)){
					Result.add(file);
					Count.addAndGet(1);
				}
			}
		}
	}
}
