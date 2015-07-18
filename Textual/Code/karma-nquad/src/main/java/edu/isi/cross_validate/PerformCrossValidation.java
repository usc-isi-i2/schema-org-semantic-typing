package edu.isi.cross_validate;



/**
 * @author pranav
 * @date 17th June;-->This file shall perform cross validation on the given training data
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;


import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;
import edu.isi.predict.PredictSemanticType;
import edu.isi.trainlucene.TrainLuceneIndex;

public class PerformCrossValidation {
	
	private int numSets;
	private static final double NUMERIC_THRESHOLD = 0.7;


	private Scanner sc;
	
	public void setScanner(){
		sc = new Scanner(new InputStreamReader(System.in));
	}
	
	public void unsetScanner(){
		sc.close();
	}
	
	public int getNumSets(){return numSets;}
	public void setNumSets(int numSets){this.numSets=numSets;}
	public void askNumSets(){
		//Scanner sc = new Scanner(new InputStreamReader(System.in));
		System.out.println("\nEnter Number of Sets on which you want to perform cross Validation:");
		int numSets = sc.nextInt();
		setNumSets(numSets);
		//sc.close();
	}
	
		
	private String readFile(String inputFile){
		try {
			return new String(Files.readAllBytes(Paths.get(inputFile)));
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
		return null;
	}
	
	private void buildTrainingAndTestSets(String[] lines,int setNumber,

			LinkedList<String> trainData,LinkedList<String> testData,boolean invertTrainTest){
		//The setNumber will indicate the testSet and the rest of the data will be treated as 
		//training data
		
		//The setCount begins with "1" and not with "0"
		
		
		int numData = lines.length;
		
		int numSets = getNumSets();
		int numDataInEach = numData/numSets;
		
		int testStart = (setNumber-1)*(numDataInEach);
		int testEnd = (setNumber)*(numDataInEach);
		
		
		
		if(invertTrainTest == true){
			for(int i = 1;i<numData;i++){
				if(i>testStart && i<testEnd){
					trainData.add(lines[i]);
				}else{
					testData.add(lines[i]);
				}
			}
		}else{
			for(int i = 1;i<numData;i++){
				if(i>testStart && i<testEnd){
					testData.add(lines[i]);
				}else{
					trainData.add(lines[i]);
				}
			}
		}
				
		
		
		/*
		if(invertTrainTest == true){
			for(int i = 1;i<numData;i++){
				trainData.add(lines[i]);
				testData.add(lines[i]);
			}
		}else{
			for(int i = 1;i<numData;i++){
				if(i>testStart && i<testEnd){
					testData.add(lines[i]);
					trainData.add(lines[i]);
				}
			}
		}
		*/
		
	}
	
	public void printList(LinkedList<String> l){
		for(String temp:l){
			System.out.println(temp);
		}
	}
	
	private String createDataDirectory(int dirNum,String inputDir,String prefix){
		String newDirName = inputDir + "/" + prefix + "_" + dirNum; 
		File dir = new File(newDirName);
		if(!dir.exists()){
			dir.mkdir();
		}
		return newDirName;
	}
	
	private void writeToFile(String inputDir,String inputFileName,
			LinkedList<String> dataList,String semanticLabel){
		
		String inputFile = inputDir + "/" + inputFileName;
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(inputFile));
			fw.write(semanticLabel + "\n");
			for(String tempS:dataList){
				fw.append(tempS + "\n");
			}
			fw.close();
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
		
		
	}
	
	public void performCrossValidation(String inputDir,String inputFileName){
		
		String inputFile = inputDir + "/" + inputFileName;
		String s = readFile(inputFile);
		//System.out.println(s);
		
		if(isDataNumeric(s,inputFileName)){
			System.out.println("\n ----------------------- The file " + inputFileName + " has Numeric Data -------------");
			System.out.println("----------> No Training and Testing Sets are being generated for this");
			return;
		}
		
		
		
		String[] lines = s.split("\n");
		s=null; //So as to reduce memory consumption
		
		String semanticLabel = lines[0];
		int numSets = getNumSets();
		
		String prefixTrain = "Train";
		String prefixTest = "Test";
		
		boolean invertTrainTest = true;
		
		for(int i = 1;i<numSets + 1;i++){
			LinkedList<String> trainData = new LinkedList<String>();
			LinkedList<String> testData = new LinkedList<String>();
			
			buildTrainingAndTestSets(lines, i, trainData, testData,invertTrainTest);
			
			createDataDirectory(i, inputDir, prefixTrain);
			createDataDirectory(i, inputDir, prefixTest);
			
			String trainDirPath = inputDir + "/" + prefixTrain + "_" + i;
			String testDirPath = inputDir + "/" + prefixTest + "_" + i;
			if(trainData.size()!=0){
				writeToFile(trainDirPath,inputFileName,trainData,semanticLabel);
			}else{
				writeToFile(trainDirPath,inputFileName,testData,semanticLabel);
			}
			
			if(testData.size()!=0){
				writeToFile(testDirPath,inputFileName,testData,semanticLabel);
			}else{
				writeToFile(testDirPath,inputFileName,trainData,semanticLabel);
			}
			
			
			
			
			
			/*
			System.out.println("Train"+i);
			printList(trainData);
			System.out.println("Test"+i);
			printList(testData);
			*/
		}
		
	}
	
	public void runFile(){
		String inputDir = "/home/v/karma-isi/schema.org/Nquad/TrainingSet";
		String inputFileName = "Article_image.txt";
		askNumSets();
		performCrossValidation(inputDir, inputFileName);
	}
	
	public void runFolder(){
		askNumSets();
		String inputDir = "/home/v/karma-isi/schema.org/Nquad/TrainingSet";
		File dir = new File(inputDir);
		File[] childListStrings = dir.listFiles();
		
		
		for(File tempFile:childListStrings){
			if(!tempFile.isDirectory()){				
				performCrossValidation(inputDir, tempFile.getName());
			}
			
		}
	}
	
	private boolean isSpecialChar(char c){
		char[] special_char_list = {'\n','-','_',':','?','%','$'};
		
		for(int i = 0;i<special_char_list.length;i++){
			if(special_char_list[i] == c){
				return true;
			}
		}
		return false;
	}
	
	private boolean isDataNumeric(String s,String inputFileName){
		int numericCount = 0;
		int textualCount = 0;
		
		int i = 0;
		char c;
		do{
			c = s.charAt(i);
			i++;
		}while(c!='\n');
		
		i++;
		for(;i<s.length();i++){
			
			c = s.charAt(i);
			if(Character.isDigit(c)){
				numericCount++;
			}else if (isSpecialChar(c)){
				//Do Nothing
			}else{
				textualCount++;
			}
		}
		int totalCount = numericCount + textualCount;
		double numericRatio = (double)(numericCount)/totalCount;
		//double textualRatio = textualCount/totalCount;
		//System.out.println("\n" + inputFileName+" NUMERIC RATIO: " + numericRatio);
		if(numericRatio >= NUMERIC_THRESHOLD){
			return true;
		}
		
		return false;
	}
	
	
	
	

}