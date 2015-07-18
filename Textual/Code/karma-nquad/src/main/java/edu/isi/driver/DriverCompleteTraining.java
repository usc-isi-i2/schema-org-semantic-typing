package edu.isi.driver;

import java.io.File;
import java.io.FileWriter;

import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;
import edu.isi.predict.CreateTrainingandTestingSets;
import edu.isi.predict.GenerateSetsSortedBySources;
import edu.isi.predict.PredictSemanticType;
import edu.isi.predict.RefineFiles;
import edu.isi.trainlucene.TrainLuceneIndex;

public class DriverCompleteTraining {
	
	public static void initializeStaticVariables(String[] args){
		
		
		Driver.EXPERIMENT_NAME = args[0]; //eg: diff_training_name
		Driver.HOME_FOLDER = Driver.BASE_FOLDER + "/" + Driver.EXPERIMENT_NAME;
		new File(Driver.HOME_FOLDER).mkdir();
		System.out.println(" inSide HOME FOLDER: "+ Driver.HOME_FOLDER+"\n");
		
		if(args[1].equals("false")){
			Driver.isTrainingLimited = false;
		}else{
			Driver.isTrainingLimited = true;
		}
		
		if(args[2].equals("false")){
			Driver.remove_typing_files = false;
		}else{
			Driver.remove_typing_files = true;
		}
		
		
		
		
	}

		public static void InitializeFolderNames(String folderName){
			String dirPath = folderName;
			String dirPathTokens[] = dirPath.split("/");
			String dirName = dirPathTokens[dirPathTokens.length - 1];
			
			Driver.INPUT_DIR = Driver.INPUT_DIR_BASE + "/" + dirPath;
			
			Driver.DATASET_DIR = Driver.HOME_FOLDER + "/" + dirName;
			Driver.WEBPAGE_SRC_DIR = Driver.DATASET_DIR + "/" + "webpage_sources";
			Driver.OUTPUT_TEST_DIR = Driver.DATASET_DIR + "/" + "Test/";
			Driver.OUTPUT_TRAIN_DIR = Driver.DATASET_DIR + "/" + "Train/";
			Driver.RESULTS_DIR = Driver.DATASET_DIR + "/" + "Results/";
			
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
		LuceneBasedSTModelHandler modelHandler = new LuceneBasedSTModelHandler("semantic-type-files");
		modelHandler.removeAllLabels();
		
		DriverCompleteTraining.initializeStaticVariables(args);
		System.out.println("HOME:  "+Driver.HOME_FOLDER+"\n");
		
		
		int i;
		for(i=3; i<args.length; i++){
			
			DriverCompleteTraining.InitializeFolderNames(args[i]);
			
			
			/*
			System.out.println("Results folder : "+ Driver.RESULTS_DIR+"\n");
			System.out.println(Driver.INPUT_DIR);
			System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Initialized");
			
			
			
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
			
			
			
			trainStart= System.currentTimeMillis();	
			
			TrainLuceneIndex tr =new TrainLuceneIndex();
			tr.setTrainingDirectoryName(Driver.OUTPUT_TRAIN_DIR);
			tr.runOneFolder();
			System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Lucene Indices Created");
			
			trainEnd = System.currentTimeMillis();
			trainingTime = trainEnd - trainStart;
				
		}
		
	
		
		for(i=3; i<args.length; i++){
			DriverCompleteTraining.InitializeFolderNames(args[i]);
			testStart = System.currentTimeMillis();
			PredictSemanticType pr= new PredictSemanticType();
			pr.setTestingDirectory(Driver.OUTPUT_TEST_DIR);
			pr.runOneFolder();
			testEnd = System.currentTimeMillis();
			testingTime = testEnd -testStart;
			System.out.println("\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>Predictions Performed");	
		}
		
		
		
	
		
			
		end = System.currentTimeMillis();
		totalTime = end-start;
		try {
		FileWriter fw = new FileWriter(new File(Driver.RESULTS_DIR+"ExecutionTime.txt"));
		fw.append("Training Time: "+trainingTime + "\n");
		fw.append("Testing Time: "+testingTime + "\n");
		fw.append("Total execution Time: "+totalTime + "\n");
		fw.close();
		}catch(Exception e){
			
		}
		
	}

}