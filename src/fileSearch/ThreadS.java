package fileSearch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
		Random random = new Random();
		MyFile root = new MyFile(Path);
		List<MyFile> Content = root.myListFiles();
		for(MyFile file:Content){
			try {
				Thread.sleep(random.nextInt(200));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(file.isDirectory()){
				ThreadS nThread = new ThreadS(file.getPath(), KeyWords, Result, Count, CaseS);
				new Thread(nThread).start();
			}
			else{
				String filename = file.getName();
				Count.addAndGet(1);
				if(CaseS == false){
					filename = filename.toLowerCase();
					KeyWords = KeyWords.toLowerCase();
				}
				
				if(filename.contains(KeyWords)){
					Result.add(file);
				}
			}
		}
	}
}
