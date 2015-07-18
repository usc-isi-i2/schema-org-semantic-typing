def extract_accuracy(mrr_accuracy_data):
	mrr_accuracy_arr = mrr_accuracy_data.split("\n");
	accuracy_arr = mrr_accuracy_arr[1].split(":");
	accuracy = accuracy_arr[1];
	return accuracy;
	
def extract_mrr(mrr_accuracy_data):
	mrr_accuracy_arr = mrr_accuracy_data.split("\n");
	mrr_arr = mrr_accuracy_arr[0].split(":");
	mrr = mrr_arr[1];
	return mrr;
	
def extract_custom_mrr(custom_mrr_data):
	custom_mrr_arr = custom_mrr_data.split("\n");
	customMRR_arr = custom_mrr_arr[1].split(":");
	customMRR = customMRR_arr[1];
	return customMRR;	

def extract_num_different_examples(custom_mrr_data):
	custom_mrr_arr = custom_mrr_data.split("\n");
	numDifferentExamples_arr = custom_mrr_arr[2].split(":");
	numDifferentExamples = numDifferentExamples_arr[1];
	return numDifferentExamples;	
	
def extract_time(execution_time_data):
	arr_1 = execution_time_data.split(":");
	
	#trainTime
	trainTime = arr_1[1].split("Testing")[0];
	
	#testTime
	testTime = arr_1[2].split("Total")[0];
	
	#totalTime
	totalTime = arr_1[3];
	
	return trainTime,testTime,totalTime

import sys

#Get the directory name
dirName=sys.argv[1];
print 'Working on the directory:',dirName


#Get the directory List
import os
dirList=os.listdir(dirName);
#print 'DirList : ',dirList;

resultsDirName='Results'
testDirName='Test'
trainDirName='Train'

mrr_accuracy_filename='EvaluatedResults-PredictedSemanticLabels.txt'
custom_mrr_filename='CustomMRR.txt';
execution_time_filename='ExecutionTime.txt';

#Separator --> This shall be used in the separating files
sep=','
order_list_names = ['Data Set','MRR','Accuracy','CustomMRR','Number of Training Labels','Number of Testing Labels','Number of Testing Labels which are not present in training set','Train Time','Test Time','Total Time'];
order_str_names = sep.join(order_list_names);

dirCSVFileName='results_'+dirName+'.csv';
dirCSVFile=open(dirCSVFileName,'w');
dirCSVFile.write(order_str_names + '\n');

totalTrain = 0;
totalTest = 0;

totalAccuracy = 0;
totalMRR = 0;
totalCustomMRR = 0;

for datadump in dirList:
	datadumpPath = dirName + "/" + datadump;
	if( os.path.isdir(datadumpPath)):
		#Work on each datadump separately
		print "\nCurrently Working On : ",datadump
		particularDumpList = os.listdir(datadumpPath)
		#print particularDumpList

		#1. Get MRR and Accuracy
		#2. Get Custom MRR and the Number of Testing Samples which are not present in the training data
		#3. Get Execution Time
		#4. Get Number of Training Samples and Number of Testing Samples
		
		
		#1 Get MRR and Accuracy
		########################################################################################################################
		with open(datadumpPath + "/" + resultsDirName + "/" + mrr_accuracy_filename) as mrr_accuracy_file:
			mrr_accuracy_data=mrr_accuracy_file.read();
			#MRR
			mrr = extract_mrr(mrr_accuracy_data);
			print 'MRR :',mrr
			
			#Accuracy
			accuracy = extract_accuracy(mrr_accuracy_data);
			print 'Accuracy : ',accuracy
			
			
		########################################################################################################################
		
		#2 Get Custom MRR and the Number of Testing Samples which are not present in the training data
		########################################################################################################################
		with open(datadumpPath + "/" + resultsDirName + "/" + custom_mrr_filename) as custom_mrr_file:
			custom_mrr_data = custom_mrr_file.read();
			
			#CustomMRR
			customMRR = extract_custom_mrr(custom_mrr_data);
			print 'CustomMRR:',customMRR
			
			#DifferentExamples
			numDifferentExamples = extract_num_different_examples(custom_mrr_data);
			print 'Number of Semantic Labels which were present in testing set but not in training set :',numDifferentExamples
			
		########################################################################################################################
		
		#3 Get Execution Time
		########################################################################################################################
		with open(datadumpPath + "/" + resultsDirName + "/" + execution_time_filename) as execution_time_file:
			execution_time_data=execution_time_file.read();
			
			trainTime,testTime,totalTime=extract_time(execution_time_data)
			
			trainTime=float(trainTime)/1000;
			testTime=float(testTime)/1000;
			totalTime=float(totalTime)/1000;
			print 'Train Time:',trainTime
			print 'Test Time:',testTime
			print 'Total Time:',totalTime
		########################################################################################################################

		
		#4 Get Number of Training Samples and Number of Testing Samples
		########################################################################################################################
		
		#numTrainLabels
		numTrainLabels = len(os.listdir(datadumpPath + "/" + trainDirName));
		totalTrain += numTrainLabels;
		#numTestLabels
		numTestLabels = len(os.listdir(datadumpPath + "/" + testDirName));
		totalTest += numTestLabels	
		
		print 'Num Train Labels : ',numTrainLabels
		print 'Num Test Labels : ',numTestLabels		
		
		#We also need to calculate the cumulative results
		totalAccuracy += float(accuracy.strip('%')) * numTestLabels;
		totalMRR += float(mrr) * numTestLabels;
		totalCustomMRR += float(customMRR) * numTestLabels;
		########################################################################################################################
		
		
		
		#5 -------> Printing the data to a CSV File
		order_list = [datadump,mrr,accuracy,customMRR,str(numTrainLabels),str(numTestLabels),numDifferentExamples,str(trainTime),str(testTime),str(totalTime)];
		order_str = sep.join(order_list);
		dirCSVFile.write(order_str + '\n');
		
totalAccuracy = totalAccuracy/totalTest
totalMRR = totalMRR/totalTest
totalCustomMRR = totalCustomMRR/totalTest

dirCSVFile.write('\n')
dirCSVFile.write('Total Accuracy:' + str(totalAccuracy) + '\n');
dirCSVFile.write('Total MRR:' + str(totalMRR) + '\n');
dirCSVFile.write('Total CustomMRR:' + str(totalCustomMRR) + '\n');
