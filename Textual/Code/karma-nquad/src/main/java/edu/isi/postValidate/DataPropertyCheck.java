package edu.isi.postValidate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.isi.driver.Driver;


public class DataPropertyCheck {
	
	//private static final String TRAINING_LABELS_DIR = "/home/pranav/workspace_karma3/Nquad/Train_1";
	//private static final String TESTING_LABELS_DIR = "/home/pranav/workspace_karma3/Nquad/Test_1";
	
	//private static final String TRAINING_LABELS_DIR = "/home/pranav/tmp_karma_output/Train_1";
	//private static final String TESTING_LABELS_DIR = "/home/pranav/tmp_karma_output/Test_1";
	
	private static final String TRAINING_LABELS_DIR = Driver.OUTPUT_TRAIN_DIR;
	private static final String TESTING_LABELS_DIR = Driver.OUTPUT_TEST_DIR;
	
	
	
	private static final double B0 = 1;
	private static final double B1 = 0.5;
	private static final double B2 = 0.2;
	
	private static ArrayList<String> trainingSemanticLabels = null;
	private static ArrayList<String> testingSemanticLabels = null;
	
	static{
		File f1 = new File(TRAINING_LABELS_DIR);
		String[] f1Files = f1.list();
		trainingSemanticLabels = new ArrayList<String>();
		for(String tempS:f1Files){
			trainingSemanticLabels.add(removeTXTFromFileName(tempS));
		}
		
		
		File f2 = new File(TESTING_LABELS_DIR);
		String[] f2Files = f2.list();
		testingSemanticLabels = new ArrayList<String>();
		for(String tempS:f2Files){
			testingSemanticLabels.add(removeTXTFromFileName(tempS));
		}	
		
	}
	
	
	
	private static String removeTXTFromFileName(String filename){
		return filename.substring(0, filename.length()-4);
	}

	
	public static int getNumberOfSemanticLabelsWhichAreNotInTrainingSet(){
		int count = 0;
		for(String tempS:testingSemanticLabels){
			if(!trainingSemanticLabels.contains(tempS)){
				count++;
			}
		}
		return count;
	}
	
	public boolean trainingSetContainsLabel(String semanticLabel){
		return trainingSemanticLabels.contains(semanticLabel);
	}
	
	private static String extractSemanticLabelFromCorrectLabel(String line){
		//System.out.println("\n Printing ---> " + line);		
		String toRightPipe = line.split("\\|")[1];				
	
		String[] splitByForwardSlash = toRightPipe.split("/");
	
		String class_label = splitByForwardSlash[splitByForwardSlash.length - 2];
		String data_property = splitByForwardSlash[splitByForwardSlash.length - 1];
		
		return class_label + "_" + data_property;
	}
	
	private static String extractSemanticLabelFromPredictedLabel(String line){
		String toRightPipe = line.split("\\|")[1];
		String toLeftOfScore = toRightPipe.split("\\s")[0];
		
		String[] splitByForwardSlash = toLeftOfScore.split("/");
		String class_label = splitByForwardSlash[splitByForwardSlash.length - 2];
		String data_property = splitByForwardSlash[splitByForwardSlash.length - 1];
		return class_label + "_" + data_property;
		
	}
	
	public String getClassLabel(String semanticLabel){
		return semanticLabel.split("_")[0];
	}
	
	public static String getDataProperty(String semanticLabel){
		return semanticLabel.split("_")[1];
	}
	
	private static String readFile(String inputFileName){
		String s = null;
		try {
			s = new String(Files.readAllBytes(Paths.get(inputFileName)),StandardCharsets.UTF_8);
		} catch (IOException e) {
			
			System.out.println("\n FILES NOT PRESENT ... SORRY :(");
			e.printStackTrace();
		}
		return s;
	}
	
	private static int getRankOfCompletePrediction(String correctSemanticLabel,ArrayList<String> predictedSemanticLabels){
		int rank = 0;
		boolean isFound = false;
		for(String tempS:predictedSemanticLabels){
			rank++;
			if(tempS.equalsIgnoreCase(correctSemanticLabel)){
				isFound = true;
				break;
			}
		}
		if(isFound == true){
			return rank;
		}else{
			return 0;
		}
		
	}
	
	private static int getRankOfDataPropertyPrediction(String correctSemanticLabel,ArrayList<String> predictedSemanticLabels){
		int rank = 0;
		boolean isFound = false;
		String correctDataProperty = getDataProperty(correctSemanticLabel);
		for(String tempS:predictedSemanticLabels){
			rank++;
			if(correctDataProperty.equalsIgnoreCase(getDataProperty(tempS))){
				isFound = true;
				break;
			}
		}
		if(isFound == true){
			return rank;
		}else{
			return 0;
		}
		
	}
	
	private static double computeScore(String correctSemanticLabel,ArrayList<String> predictedSemanticLabels){
		double tmpValue = 0;
		if(trainingSemanticLabels.contains(correctSemanticLabel)){
			int rank = getRankOfCompletePrediction(correctSemanticLabel, predictedSemanticLabels);
			if(rank != 0){
				//Means Exact Match Was Found
				tmpValue = B0 * (1.0/rank);
			}else{
				
				rank = getRankOfDataPropertyPrediction(correctSemanticLabel, predictedSemanticLabels);
				
				if(rank != 0){
					tmpValue = B2 * (1.0/rank);
				}else{
					tmpValue = 0;
				}
				
			}
		}else{
			int rank;
			rank = getRankOfDataPropertyPrediction(correctSemanticLabel, predictedSemanticLabels);
			
			if(rank != 0){
				tmpValue = B1 * (1.0/rank);
			}else{
				tmpValue = 0;
			}			
		}
		
		return tmpValue;
	}
	
	private static double processOneSemanticLabel(String tempS){
		
		double tmpValue = 0;
		
		String[] semanticLabelArray = tempS.split("\n");
		/*
		for(String tempS2:semanticLabelArray){
			System.out.println("\nTEMPS:" + tempS2);
		}
		*/
		
		
		String correctSemanticLabel = extractSemanticLabelFromCorrectLabel(semanticLabelArray[0]);
		
		ArrayList<String> predictedSemanticLabels = new ArrayList<String>();
		for(int i = 2;i<semanticLabelArray.length;i++){
			predictedSemanticLabels.add(extractSemanticLabelFromPredictedLabel(semanticLabelArray[i]));
		}
		
		/**************** Printing Correct and Predicted Semantic Labels *********/
		/*
		System.out.println("~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Correct : " + correctSemanticLabel);
		for(String tempS2:predictedSemanticLabels){
			System.out.println("Predicted:" + tempS2);
		}
		*/
		
		
		tmpValue = computeScore(correctSemanticLabel, predictedSemanticLabels);
		//System.out.println(tmpValue);
		//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
		return tmpValue;
	}
	
	public static double processFile(String inputFile){
		String s=readFile(inputFile);
		String[] splitByAstrix = s.split("\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\\*\n");
		
		double sumValue = 0;
		
		for(String tempS:splitByAstrix){
			double tmpValue = processOneSemanticLabel(tempS);
			sumValue += tmpValue;
		}
		
		//System.out.println(testingSemanticLabels.size());
		return sumValue/(testingSemanticLabels.size());
		
	}
	/*
	public static void main(String[] args){
		//String FILE_BASE_DIR= "/home/pranav/Documents/usc/node31_github/Web-Karma/karma-nquad/";
		String FILE_BASE_DIR = Driver.RESULTS_DIR;
		String filename = "PredictedSemanticLabels.txt";
		String inputFile = FILE_BASE_DIR + filename;
		double customMRR = processFile(inputFile);
		System.out.println(customMRR);
		double numDifferentSemanticLabels = getNumberOfSemanticLabelsWhichAreNotInTrainingSet();
		System.out.println(numDifferentSemanticLabels);
		
		
	}
	*/

}
