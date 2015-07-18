package edu.isi.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;
import edu.isi.postValidate.DataPropertyCheck;
import edu.isi.predict.CreateTrainingandTestingSets;
import edu.isi.predict.GenerateSetsSortedBySources;
import edu.isi.predict.PredictSemanticType;
import edu.isi.predict.RefineFiles;
import edu.isi.trainlucene.TrainLuceneIndex;

public class DriverPartialTraining {
	
	public static void initializeStaticVariables(String[] args){
		
		
		Driver.EXPERIMENT_NAME = args[0]; //eg: diff_training_name
		Driver.HOME_FOLDER = Driver.BASE_FOLDER + "/" + Driver.EXPERIMENT_NAME;
		
		String dirPath = args[1];
		String dirPathTokens[] = dirPath.split("/");
		String dirName = dirPathTokens[dirPathTokens.length - 1];
		
		Driver.INPUT_DIR = Driver.INPUT_DIR_BASE + "/" + args[1];
		
		Driver.DATASET_DIR = Driver.HOME_FOLDER + "/" + dirName;
		Driver.WEBPAGE_SRC_DIR = Driver.DATASET_DIR + "/" + "webpage_sources";
		Driver.OUTPUT_TEST_DIR = Driver.DATASET_DIR + "/" + "Test/";
		Driver.OUTPUT_TRAIN_DIR = Driver.DATASET_DIR + "/" + "Train/";
		Driver.RESULTS_DIR = Driver.DATASET_DIR + "/" + "Results/";
		
		if(args[2].equals("false")){
			Driver.isTrainingLimited = false;
		}else{
			Driver.isTrainingLimited = true;
		}
		
		if(args[3].equals("false")){
			Driver.remove_typing_files = false;
		}else{
			Driver.remove_typing_files = true;
		}
		
		new File(Driver.HOME_FOLDER).mkdir();
		new File(Driver.DATASET_DIR).mkdir();
		new File(Driver.WEBPAGE_SRC_DIR).mkdir();	
		new File(Driver.OUTPUT_TRAIN_DIR).mkdir();
		new File(Driver.OUTPUT_TEST_DIR).mkdir();
		new File(Driver.RESULTS_DIR).mkdir();
		
		
	}

	
	public static void main(String[] args) {
		double trainStart,trainEnd,testStart,testEnd,start,end;
		double trainingTime=0,testingTime=0,totalTime=0;
		start= System.currentTimeMillis();
		DriverPartialTraining.initializeStaticVariables(args);
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Initialized");
				
		System.out.println(Driver.INPUT_DIR);
		/*
		GenerateSetsSortedBySources g1 = new GenerateSetsSortedBySources();
		g1.runFolder(Driver.INPUT_DIR);
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Sources Generated");
		//webpage sources will be created by now
		
		CreateTrainingandTestingSets.createDataSets();
		//Train_1
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Train and Test Generated");
		
		RefineFiles rf  =  new RefineFiles();
		rf.runFolder(Driver.DATASET_DIR);
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Files Refined");
		//The files with Numeric Data, Null Data and Special Characters have been removed
		*/
		
		LuceneBasedSTModelHandler modelHandler = null;
		if(Driver.remove_typing_files){
			modelHandler = new LuceneBasedSTModelHandler("semantic-type-files");
			modelHandler.removeAllLabels();
			System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Labels Removed");
		}
		
		trainStart= System.currentTimeMillis();	
		TrainLuceneIndex tr =new TrainLuceneIndex();
		tr.setTrainingDirectoryName(Driver.OUTPUT_TRAIN_DIR);
		tr.runOneFolder();
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Lucene Indices Created");
		trainEnd = System.currentTimeMillis();
		trainingTime = trainEnd - trainStart;
		
		testStart = System.currentTimeMillis();
		PredictSemanticType pr= new PredictSemanticType();
		pr.setTestingDirectory(Driver.OUTPUT_TEST_DIR);
		pr.runOneFolder();
		System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Predictions Performed");
		
		testEnd = System.currentTimeMillis();
		testingTime = testEnd -testStart;
		
		
		if(Driver.remove_typing_files){
			
			String filename = "PredictedSemanticLabels.txt";
			String inputFile = Driver.RESULTS_DIR + filename;
			double customMRR = DataPropertyCheck.processFile(inputFile);
			double numDifferentSemanticLabels = DataPropertyCheck.getNumberOfSemanticLabelsWhichAreNotInTrainingSet();
			
			try {
				FileWriter fw = new FileWriter(new File(Driver.RESULTS_DIR + "CustomMRR.txt"),true);
				fw.append("\n Custom MRR : " + customMRR);
				fw.append("\n Number of Semantic Labels Which are not in Training Data : " + numDifferentSemanticLabels );
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		end = System.currentTimeMillis();
		totalTime = end-start;
		try {
		FileWriter fw = new FileWriter(new File(Driver.RESULTS_DIR+"ExecutionTime.txt"));
		fw.append("Training Time: "+trainingTime);
		fw.append("Testing Time: "+testingTime);
		fw.append("Total execution Time: "+totalTime);
		fw.close();
		}catch(Exception e){
			
		}
		
			
				
	}

}
