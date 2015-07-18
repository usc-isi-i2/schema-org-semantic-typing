package edu.isi.driver;

public class Driver {
	
	public static String BASE_FOLDER = "/home/pranav/exp_train_all";
	//public static String BASE_FOLDER = "/mnt/data1/karma/controlled_experiments";
	public static String EXPERIMENT_NAME = null;		
	public static String HOME_FOLDER = null; 
	public static String INPUT_DIR_BASE = "/home/pranav/data_files2";
	//public static String INPUT_DIR_BASE = "/mnt/data1/karma/data_files";
	//public static String INPUT_DIR_BASE = "/home/pranav/fusionRepo/data_files";
	public static String INPUT_DIR = null; 
	
	public static String DATASET_DIR = null;
	public static String WEBPAGE_SRC_DIR = null;	
	public static String OUTPUT_TEST_DIR = null;
	public static String OUTPUT_TRAIN_DIR = null;
	public static String RESULTS_DIR = null;
	public static double onlyTrainTime=0;
	public static double onlyTestTime=0;
	public static double trainAndTestTime=0;
	public static double CompleteTime=0;
	
	
	
	public static boolean isTrainingLimited = false;
	/**
	 * When isLimited = true
	 * Then MAX_TRAINING_TUPLES will train at the maximum on 10000 from a given dataset
	 * 
	 * When isLimited = false
	 * Training is done on the entire data from each dataset for all semantic labels
	 * but MAX_TRAINING_TUPLES, will set the batch size i.e. the training shall be performed 
	 * in a batch of size set by MAX_TRAINING_TUPLES 
	 */	
	public static int MAX_TRAINING_TUPLES  = 10000;
	public static int MAX_TESTING_TUPLES = 1000;	
	public static int MAX_SUGGESTED_SEMANTIC_LABELS = 4;
	
	public static boolean remove_typing_files = false;
}
