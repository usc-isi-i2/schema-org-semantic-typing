package edu.isi.predict;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.isi.driver.Driver;
import edu.isi.karma.modeling.semantictypes.SemanticTypeLabel;
import edu.isi.karma.semantictypes.typinghandler.LuceneBasedSTModelHandler;

/**
 * 
 * @author aditi
 * @date 17th June 2015
 * 
 *
 */

public class PredictSemanticType{
	
	private  String TESTING_SET_INPUT_DIR ;
	private final static int NUMBER_OF_PREDICTED_SEMANTIC_TYPES = Driver.MAX_SUGGESTED_SEMANTIC_LABELS;
	private final static int MAX_TESTING_EXAMPLES = Driver.MAX_TESTING_TUPLES;
	//private static int count = 0;
	private FileReader fr = null;
	private BufferedReader br = null;

	public  void setTestingDirectory(String dirName){
		this.TESTING_SET_INPUT_DIR = dirName;
	}
	public PredictSemanticType(){
		
	}
	public PredictSemanticType(String filename) {

		String file =  filename;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			
			System.out.println(file);
			e.printStackTrace();
		}
		br = new BufferedReader(fr);
	}
	
	
	
	public double runOneFile(){		
		double reciprocalRank = 0;
		try {
			 reciprocalRank =  this.predictOnFile();
			// System.out.println("RR: "+reciprocalRank+"\n");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		 return reciprocalRank;
	}
	
	public  void runOneFolder(){
		
		//PredictSemanticType.count++;
		File dirName = new File(this.TESTING_SET_INPUT_DIR);
		//System.out.println(" Running folder "+ PredictSemanticType.count);
		String[] childList = dirName.list();
		//System.out.println(childList);
		double sumReciprocalRank = 0;
		double meanReciprocalRank = 0;
		double reciprocalRank = 0;
		double accuracy =0 ;
		int noOfCorrectPredictions = 0 ;
		for(String inputFileName:childList){
			String fileName = dirName + "/" + inputFileName;
			//System.out.println("Printing file : " + fileName + "\n");
			PredictSemanticType tr = new PredictSemanticType(fileName);
			
			reciprocalRank = tr.runOneFile();
			//System.out.println(reciprocalRank+ "\n");
			sumReciprocalRank+=reciprocalRank;
			if(reciprocalRank==1){
				noOfCorrectPredictions++;
			}
			try {
				tr.fr.close();
				tr.br.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		 int noOfColumns = dirName.listFiles().length;
		 meanReciprocalRank = sumReciprocalRank / noOfColumns ;
		 accuracy = ((double) noOfCorrectPredictions / noOfColumns )* 100 ; 
		 
		 
		 try {
			String fileName = "EvaluatedResults-PredictedSemanticLabels";
			
			FileWriter fw = new FileWriter(new File(Driver.RESULTS_DIR + fileName + ".txt"),true);
			fw.append("Mean Reciprocal Rank of the testing datset : " + meanReciprocalRank + "\n" );
			fw.append("Accuracy of the testing datset : " + accuracy + "%\n" );
			
			fw.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		 
		
	}
	
	public int getRank(String correctSemanticlabel, List<SemanticTypeLabel> predictedSemanticTypeList){
		int rank=0;
		boolean flag = false;
		for(SemanticTypeLabel predictedLabel : predictedSemanticTypeList){
			
			rank++;
			if(correctSemanticlabel.equals(predictedLabel.getLabel())){
				flag = true;
				break;
			}
		}
		if(flag==true){
		return rank;
		}
		else
			return 0;
	}
	
	
	public double evaluateReciprocalRankOfPredictedSemanticLabels( String correctSemanticLabel, List<SemanticTypeLabel> predictedSemanticTypeList){
		
		int rank=0;
		double reciprocalRank;
		
		
		rank = getRank(correctSemanticLabel,predictedSemanticTypeList);
		if(rank==0){
			reciprocalRank = 0 ;
		}
		else {
			reciprocalRank= (double)1/rank;
		}
		
		return reciprocalRank;
		
	}
	
	
	
	public double predictOnFile() throws IOException{
		String correctSemanticLabel = null;
		correctSemanticLabel = br.readLine();
		double reciprocalRank =0;
		int count =0;
		LuceneBasedSTModelHandler modelHandler = new LuceneBasedSTModelHandler("semantic-type-files");
		modelHandler.setModelHandlerEnabled(true);
		LinkedList<String> examples = new LinkedList<String>();
		List<SemanticTypeLabel> predictedSemanticTypeList = new LinkedList<SemanticTypeLabel>();
		String currLine = null;
		do{
			currLine = br.readLine();
			if(currLine == null)break;
			examples.add(currLine);
			count++;
			
		}while(currLine!=null && count < MAX_TESTING_EXAMPLES);
		
		if(!examples.isEmpty()){
			//System.out.println("Examples: "+ examples+"\n");
			
			predictedSemanticTypeList = modelHandler.predictType( examples, PredictSemanticType.NUMBER_OF_PREDICTED_SEMANTIC_TYPES);
			
			if(predictedSemanticTypeList == null){
				System.out.println(correctSemanticLabel);
				return reciprocalRank;
			}
			
			//System.out.println("\n PREDICTED SEMANTIC LABEL:" + predictedSemanticTypeList + "\n CORRECT SEMANTIC TYPE :" + correctSemanticLabel);
			String prefixFileName = "PredictedSemanticLabels";
			String prefixFileName1 = "NoFirstHitSemanticLabels";
			String prefixFileName2 = "NoCorrectSemanticLabels";
			String fileName = Driver.RESULTS_DIR +prefixFileName ;
			String fileName1 = Driver.RESULTS_DIR +prefixFileName1 ;
			String fileName2 = Driver.RESULTS_DIR +prefixFileName2;
			
			FileWriter fileWrite = new FileWriter(new File(fileName + ".txt"),true);
			
			
			//fileWrite.append("Column Content: "+ examples + "\n");
			fileWrite.append("Correct Semantic Label :" + correctSemanticLabel + "\n");
			fileWrite.append("Predicted Semantic Labels : \n" );
			for(SemanticTypeLabel predictedLabel : predictedSemanticTypeList){				
				
				fileWrite.append("Label: " + predictedLabel.getLabel() + " Score : " + predictedLabel.getScore() + "\n" );
			}
			fileWrite.append("********************************************************\n");
			
			reciprocalRank = evaluateReciprocalRankOfPredictedSemanticLabels( correctSemanticLabel, predictedSemanticTypeList);
			
			fileWrite.close();
			
			FileWriter fileWrite1 = new FileWriter(new File(fileName1 + ".txt"),true);
			if(predictedSemanticTypeList.size()>0 && !correctSemanticLabel.equals(predictedSemanticTypeList.get(0).getLabel())){
				fileWrite1.append("The label is: "+correctSemanticLabel+"\n");
			}
			fileWrite1.close();
			
			
			FileWriter fileWrite2 = new FileWriter(new File(fileName2 + ".txt"),true);
			boolean flag=false;
			for(SemanticTypeLabel predictedLabel : predictedSemanticTypeList){
				//fileWrite.append("Label: " + predictedLabel.getLabel() + " Score : " + predictedLabel.getScore() + "\n" );
				if(correctSemanticLabel.equals(predictedLabel.getLabel())){
					flag=true;
					break;
				}
			}
			if(flag==false){
				fileWrite2.append("The label is: "+correctSemanticLabel+"\n");
			}
			
			fileWrite2.close();
			
			examples.clear();
			
		}
		
		return reciprocalRank;
	}
	/*
	public static void main(String[] args) {
		//String inputFileName = "GeoCoordinates_latitude.txt";
		//TrainLuceneIndex tr = new TrainLuceneIndex(inputFileName);
		//tr.runOneFolder();
		long startTime = System.currentTimeMillis();
		String dirName = "/home/v/karma-isi/schema.org/Nquad/TrainingSet/";
		PredictSemanticType pr = new PredictSemanticType();
		pr.setTestingDirectory(dirName);
		pr.runOneFolder();
		long endTime = System.currentTimeMillis();
		long timeDuration = endTime - startTime ;
		System.out.println(" Executed in : "+ timeDuration + "\n");
		//tr.runOneFile();
	}
	*/
	

}
