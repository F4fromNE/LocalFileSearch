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
	public ThreadS(String a, String b, CopyOnWriteArrayList<MyFile> c, AtomicInteger FileSearched){
		Path = a;
		KeyWords = b;
		Result = c;
		Count = FileSearched;
	}
	public void run(){
		MyFile root = new MyFile(Path);
		List<MyFile> Content = root.myListFiles();
		for(MyFile file:Content){

			if(file.isDirectory()){
				ThreadS nThread = new ThreadS(file.getPath(), KeyWords, Result, Count);
				new Thread(nThread).start();
			}
			else{
				String filename = file.getName();
				if(filename.contains(KeyWords)){
					Result.add(file);
					Count.addAndGet(1);
				}
			}
		}
	}
}
