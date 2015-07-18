package edu.isi.trainlucene;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import edu.isi.driver.Driver;
import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;



/**
 * 
 * @author pranav
 * @date 15th June 2015
 * @aim : This class tries to do it memory wise.
 * 
 * @algorithm
 * Step1: Read Line 1. This is the semantic label
 * Step2: Read Line. This is a literal.
 * Step3: Check if more literals can be accommodated in the memory. 
 * Step4: If yes, goto step 2
 * Step5: Now call addType()
 * Step6: Go to step2
 *
 */

public class TrainLuceneIndex{
	
	private  String TRAINING_SET_INPUT_DIR ;
	
	private FileReader fr = null;
	private BufferedReader br = null;
	private static final int MAX_ALLOWED_LITERALS = Driver.MAX_TRAINING_TUPLES;
	private boolean isTrainingLimited = Driver.isTrainingLimited;
	private int count = 0;
	
	public  void setTrainingDirectoryName( String dirName){
		this.TRAINING_SET_INPUT_DIR = dirName;
	}
	public TrainLuceneIndex(){
		
	}
	public TrainLuceneIndex(String filename) {
		// 
		String file = filename;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			// 
			System.out.println(file);
			e.printStackTrace();
		}
		br = new BufferedReader(fr);
	}
	
	private boolean areMoreLiteralsAllowed(String currLine){
		//I wanted to do it memory wise, but I could not find sizeof() method in java
		if(count<MAX_ALLOWED_LITERALS){
			count++;
			return true;
		}
		return false;
	}
	
	public void runOneFile(){		
		try {
			trainOnFile();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
	}
	
	public  void runOneFolder(){
		File dirName = new File(this.TRAINING_SET_INPUT_DIR);
		//System.out.println("dir: "+dirName);
		String[] childList = dirName.list();
		
		
		for(String inputFileName:childList){
			//System.out.println(inputFileName);
			String fileName = dirName + "/" + inputFileName;
			
			TrainLuceneIndex tr = new TrainLuceneIndex(fileName);
			tr.runOneFile();
			try {
				tr.fr.close();
				tr.br.close();
				tr.count=0;
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
			
		}
		
	
		
	}
	
	
	public void trainOnFile() throws IOException{
		String semanticLabel = null;
		semanticLabel = br.readLine();
		LuceneBasedSTModelHandler modelHandler = new LuceneBasedSTModelHandler("semantic-type-files");
		
		LinkedList<String> examples = new LinkedList<String>();
		String currLine = null;
		
		if(isTrainingLimited){
			
			do{
				currLine = br.readLine();
				if(currLine == null)break;
				
				if(areMoreLiteralsAllowed(currLine)){
					examples.add(currLine);
				}else{
					examples.add(currLine);
					break;
				}	
				
			}while(currLine!=null);
			
		}else{
			
			do{
				currLine = br.readLine();
				if(currLine == null)break;						
				
				//The following code, does TRAIN on entire data set
				//It just trains them in batches so that there is no
				//out of memory error
				if(areMoreLiteralsAllowed(currLine)){
					examples.add(currLine);
				}else{
					examples.add(currLine); //This is very important. DO NOT REMOVE THIS
					//IF you remove this, then one of the examples wont be added into the system, 
					//on which the condition was found to be false
					modelHandler.addType(semanticLabel, examples);
					//System.out.println("\n SEMANTIC LABEL:" + semanticLabel + "\n EXAMPLES:" + examples);
					count = 0;
					examples.clear();
				}			
				
			}while(currLine!=null);
			
		}
		
		if(!examples.isEmpty()){
			modelHandler.addType(semanticLabel, examples);
			//System.out.println("\n SEMANTIC LABEL:" + semanticLabel + "\n EXAMPLES:" + examples);
		}
		
		
	}
	/*
	public static void main(String[] args) {
		//String inputFileName = "GeoCoordinates_latitude.txt";
		//TrainLuceneIndex tr = new TrainLuceneIndex(inputFileName);
		//tr.runOneFolder();
		long startTime = System.currentTimeMillis();
		
		TrainLuceneIndex.runOneFolder();
		long endTime = System.currentTimeMillis();
		long timeDuration = startTime - endTime ;
		System.out.println(" Executed in : "+ timeDuration + "\n");
		//tr.runOneFile();
	}
	
	*/

}
