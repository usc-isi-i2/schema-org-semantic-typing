package edu.isi.predict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * @author pranav
 * @date 29th June 2015
 * @Aim This file shall refine all the training and testing files.
 * 
 * It will read the training file.
 * If the data is not suitable for our purpose then it shall be put in some "ToIgnore" Directory
 *
 */
public class RefineFiles {
	
	private boolean isSpecialChar(char c){
		char[] special_char_list = {'-','_',':','%','$','?'};
		
		for(int i = 0;i<special_char_list.length;i++){
			if(special_char_list[i] == c){
				return true;
			}
		}
		return false;
	}
	
	private int refineData(File inputFile){
		
		double NUMERIC_THRESHOLD = 0.5;
		double SPECIAL_CHAR_THRESHOLD = 0.5; 
		
		/**
		 * 0 --> All Good. Train and Test
		 * 1 --> No data in the file
		 * 2 --> Numeric Data
		 * 3 --> Special Characters
		 */
		
		
		int numericCount = 0;
		int specialCharCount = 0;
		int textualCount = 0;
		int totalCount = 1;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			
			//Ignore the first line as it has the semantic label in it.
			String line = null;
			line = br.readLine();			
			
			if(line == null){
				br.close();
				return 1;
			}else{
				
				//totalCount += line.length();

				do{
					line = br.readLine();
					
					if(line!=null){
						int lineLength = line.length();
						//totalCount += lineLength;
						
						char c;
						int i = 0;
						while(i<lineLength){
							c = line.charAt(i);
							if(isSpecialChar(c)){
								specialCharCount++;
							}else if(Character.isDigit(c)){
								numericCount++;
							}else{
								textualCount++;
							}
							i++;
						}
						
					}
					
				}while(line!=null);
				
			}
			
			totalCount+=numericCount + specialCharCount + textualCount;
			
			double numericRatio = (double)(numericCount)/totalCount;
			double specialCharRatio = (double)(specialCharCount)/totalCount;
			//System.out.println("Ratios of " + inputFile.getName() + "\n NumericRatio" + numericRatio + "\n Special Character Mark Ratio" + specialCharRatio);
			
			br.close();
			
			if(numericRatio >= NUMERIC_THRESHOLD){
				return 2;
			}else if(specialCharRatio >= SPECIAL_CHAR_THRESHOLD){
				return 3;
			}
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		
		return 0;
	}

	private boolean processFolder(String inputFolderName){
		/**
		 * This method is given folder name of that folder which has both 
		 * TrainSet and TestSet built into it.
		 */
		
		String inputFolder = inputFolderName;
		
		String testDirName = inputFolder + "/" + "Test";
		String trainDirName = inputFolder + "/" + "Train";
		
		String toIgnoreTrainName = inputFolder + "/" + "ToIgnoreTrainSet";
		String toIgnoreTestName = inputFolder + "/" + "ToIgnoreTestSet";
		
		File testDir = new File(testDirName);
		File trainDir = new File(trainDirName);
		
		File toIgnoreTestDir = new File(toIgnoreTestName);
		File toIgnoreTrainDir = new File(toIgnoreTrainName);
		
		if(!testDir.exists()){
			System.out.println("\n The Test Directory does not exist");
			return false;
		}
		
		if(!trainDir.exists()){
			System.out.println("\n The Train Directory does not exist");
			return false;
		}
		
		
		if(!toIgnoreTestDir.exists()){
			toIgnoreTestDir.mkdir();
		}
		
		
		if(!toIgnoreTrainDir.exists()){
			toIgnoreTrainDir.mkdir();
		}
		
		File[] trainFiles = trainDir.listFiles();
		
		
		for(File fTrain : trainFiles){
			int fileRefineValue = refineData(fTrain);
			if(fileRefineValue!=0){
				/*
				 * This file shall now be put inside the toIgnoreFolder.
				 */			
				
				
				File fTest = new File(testDirName + "/" + fTrain.getName());
				
				fTrain.renameTo(new File(toIgnoreTrainName + "/" + fTrain.getName()));
				
				if(fTest.exists()){
					fTest.renameTo(new File(toIgnoreTestName + "/" + fTest.getName()));
				}
				
				
				String FILE_REFINE_ERROR = "";
				
				if(fileRefineValue == 1){
					FILE_REFINE_ERROR = "NO DATA";
				}else if(fileRefineValue == 2){
					FILE_REFINE_ERROR = "NUMERIC DATA";
				}else if(fileRefineValue == 3){
					FILE_REFINE_ERROR = "SPECIAL CHARACTER DATA";
				}
				
				System.out.println("\n Ignoring File : " + fTrain.getName() + 
						" because of " + FILE_REFINE_ERROR);		
				
				
			}
		}
		
		return true;
		
	}
	
	public void runFolder(String inputFolderName){
		
		boolean runValue = processFolder(inputFolderName);
		if(runValue){
			System.out.println("\n Successful Run");
		}else{	
			System.out.println("\n Unsuccessful Run");
		}
		
	}
	
	
}