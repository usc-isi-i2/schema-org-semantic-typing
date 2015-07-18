package edu.isi.predict;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.isi.driver.Driver;

import edu.isi.quadNode.QuadNode;


public class GenerateSetsSortedBySources {
	
	
	
	
		
	
	public String sanitizefilePropertyLabel(String filePropertyLabel){
		
		String sanitizedLabel = null;
		if(filePropertyLabel.length() > 30){
		sanitizedLabel = filePropertyLabel.substring(0, 30);
		}
		else {
			sanitizedLabel = filePropertyLabel;
		}
		
		return sanitizedLabel;
	}
		
		
	public String getFileNameForSemanticLabel(String type){
			
			
			String fileName=null;
			String fileLabelTokens[] = type.split("/");
			
			String filePropertyTempLabel = fileLabelTokens[fileLabelTokens.length-1];
			String filePropertyLabel = sanitizefilePropertyLabel(filePropertyTempLabel);
			if(fileLabelTokens.length > 3){
			String fileClassLabel = fileLabelTokens[fileLabelTokens.length-2];
			
			fileName= fileClassLabel + "_" + filePropertyLabel + ".txt";
			}
			else {
				fileName= filePropertyLabel + ".txt";
			}
			
			return fileName;
		}
	
	
	
	private void addToSrcDir(String src, String currLine){
		
			String fName = getFileNameForSemanticLabel(src);
			try {
			FileWriter fw = new FileWriter (new File(Driver.WEBPAGE_SRC_DIR+"/"+fName),true);
			fw.append(currLine+"\n");
			fw.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		
	}
	/*
	private void addToTypeDir(String type, String currLine){
		
		String fName = getFileNameForSemanticLabel(type);
		try {
		FileWriter fw = new FileWriter (new File(TYPE_DIR+"/"+fName),true);
		fw.append(currLine+"\n");
		fw.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	
	}
	*/
	
	
	public void processFile(String inputFileName,String INPUT_DIR){
		String inputFile = INPUT_DIR + "/" + inputFileName;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String currLine = null;
			do{
				try {
					currLine = br.readLine();
				} catch (IOException e) {

					e.printStackTrace();
				}
				if(currLine==null){
					break;
				}
				QuadNode qn = new QuadNode(currLine);
				
				String src = qn.getQuadSource();
				String type = qn.getQuadPredicate();
				
				if(src!=null && type!=null){
					
					addToSrcDir(src,currLine);
					//addToTypeDir(type, currLine);
					
				}else{
					System.out.println(qn);
				}
				
				
				
			}while(currLine!=null);
			
			try {
				br.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	public void runFolder(String INPUT_DIR){
		
		File[] fileNames = new File(INPUT_DIR).listFiles();
		for(File f: fileNames){
			if(f.isFile()){
				processFile(f.getName(),INPUT_DIR);
			}
		}
	}
	
	

}
