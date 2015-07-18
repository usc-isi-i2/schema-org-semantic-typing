package edu.isi.karma.semantictypes.indexreader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/*
 * @author Pranav and Aditi
 * @Date 16th June 2015
 * @AIM : To read the lucene indexes present in the semantic type files folder
 */

public class ReadIndex2 {
	
	@SuppressWarnings("deprecation")
	public static void readAllIndices(String file){
		IndexReader r = null;
		//File indexDirectory = new File("/home/pranav/karma-home/semantic-type-files");
		File indexDirectory = new File("/home/pranav/error_sem_type/semantic-type-files");
		try {
			r = IndexReader.open(FSDirectory.open(indexDirectory));		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		int num = r.numDocs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			fw.append(Integer.toString(num));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println(num);
		
		
		
		
		for ( int i = 0; i < num; i++){   
		    
		        Document d=null;
				try {
					d = r.document(i);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        //System.out.println( "d=" +d);
		        String docString = d.toString();
		        
		        String originalTabSpace = "     ";
		        String concatenatedTabSpace = "";
		        
		        String[] angleArr = docString.split("<");
		        
		        
		        for(String s:angleArr){
		        	concatenatedTabSpace+=originalTabSpace;
		        	//System.out.println(concatenatedTabSpace+"\n"+s);
		        	try {
						fw.append(concatenatedTabSpace+"\n"+s);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
		
		String dirName = ""; //"/home/v/karma-isi/schema.org/ReadIndex/";
		String fileName = "LuceneIndexes.txt";
		String file = dirName + fileName;
		
		ReadIndex2.readAllIndices(file);
		
	}
	 

}