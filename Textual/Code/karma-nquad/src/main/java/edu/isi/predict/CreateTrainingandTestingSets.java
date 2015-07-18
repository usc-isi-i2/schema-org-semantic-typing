package edu.isi.predict;

import java.io.File;

import java.util.Random;

import edu.isi.driver.Driver;
import edu.isi.quadNode.ProcessQuad;


public class CreateTrainingandTestingSets {
	//private static final String INPUT_DIR = "/home/pranav/tmp_karma_output/webpage_sources";
	private static double TRAIN_AND_TEST_THRESHOLD = 0.7;
	//private static final String TRAIN_DIR = "/home/pranav/tmp_karma_output/Train_1/" ;
	//private static final String TEST_DIR = "/home/pranav/tmp_karma_output/Test_1/";
	//private static final String HOME_FOLDER = "/home/pranav/tmp_karma_output";
	
	private static void shuffleArray(File[] ar) {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--)   {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      File a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	  
	
	public static void createTestingSets(int beginningIndexForTestFiles,File[] fileList){
	
		//File inputDir = new File(Driver.WEBPAGE_SRC_DIR);
		//File[] fileList = inputDir.listFiles();
		//shuffleArray(fileList);

			
		
		//boolean dirCreated = new File(CreateTrainingandTestingSets.TEST_DIR).mkdir();
		//File destination =new File(CreateTrainingandTestingSets.TEST_DIR);
		for(int i=beginningIndexForTestFiles; i<fileList.length;i++){
			 ProcessQuad.generateSemanticLabelAndContent(fileList[i], Driver.OUTPUT_TEST_DIR);			 
		}
	}
	
	
	public static void createTrainingSets(int noOfTrainingFiles,File[] fileList){
		//File inputDir = new File(Driver.WEBPAGE_SRC_DIR);
		//File[] fileList = inputDir.listFiles();
		//shuffleArray(fileList);
		/*
		 * In such cases, you should not sort the array.
		 * Instead you need to shuffle it.
		 */
		
		//boolean dirCreated = new File(CreateTrainingandTestingSets.TRAIN_DIR).mkdir();
		//File destination =new File(CreateTrainingandTestingSets.TRAIN_DIR);
				
		for(int i=0;i<noOfTrainingFiles;i++){			 
				 ProcessQuad.generateSemanticLabelAndContent(fileList[i], Driver.OUTPUT_TRAIN_DIR);			
		}
	}
	
	public static void createDataSets(){
		
		File inputDir = new File(Driver.WEBPAGE_SRC_DIR);
		File[] fileList = inputDir.listFiles();
		int noOfSources = fileList.length; //Can we do it without this
		//int noOfTrainingFiles =   (int) (noOfSources * TRAIN_AND_TEST_THRESHOLD);
		int noOfTrainingFiles = noOfSources ;
		shuffleArray(fileList);
		createTrainingSets(noOfTrainingFiles,fileList);
		//int beginningIndexForTestFiles = noOfTrainingFiles;
		//createTestingSets(beginningIndexForTestFiles,fileList);
		
	}
	
	
	
}
