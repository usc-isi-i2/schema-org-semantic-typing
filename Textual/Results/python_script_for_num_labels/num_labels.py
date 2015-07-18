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
	
def extract_total_semantic_labels_train(trainDirPath,datadump):
	dirSemanticLabelFile.write('\n\n' + datadump + '\n');
	dirSemanticLabelFile.write('TRAIN' + '\n');
	trainDataList = os.listdir(trainDirPath);
	num_total_train_data = 0;
	for train_file_name in trainDataList:
		train_file_path = trainDirPath + "/" + train_file_name
		num_lines = sum(1 for line in open(train_file_path))
		num_curr_train_data = num_lines - 1;
		num_total_train_data += num_curr_train_data;
		dirSemanticLabelFile.write(train_file_name + sep + str(num_curr_train_data) + '\n'); 
	return num_total_train_data;

def extract_total_semantic_labels_test(testDirPath,datadump):
	dirSemanticLabelFile.write(datadump + '\n');
	dirSemanticLabelFile.write('TEST' + '\n');
	testDataList = os.listdir(testDirPath);
	num_total_test_data = 0;
	for test_file_name in testDataList:
		test_file_path = testDirPath + "/" + test_file_name
		num_lines = sum(1 for line in open(test_file_path))
		num_curr_test_data = num_lines - 1;
		num_total_test_data += num_curr_test_data;
		dirSemanticLabelFile.write(test_file_name + sep + str(num_curr_test_data) + '\n'); 
	return num_total_test_data;		

		
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
order_list_names = ['Data Set','MRR','Accuracy','Number of Training Labels','Number of Testing Labels','Average Number of Training Semantic Labels','Average Number of Testing Semantic Labels'];
order_str_names = sep.join(order_list_names);

dirCSVFileName='CumulativeResults/results_'+dirName+'.csv';
dirCSVFile=open(dirCSVFileName,'w');
dirCSVFile.write(order_str_names + '\n');

dirSemanticLabelFileName = 'CumulativeResults/semantic_labels_'+dirName+'.csv';
dirSemanticLabelFile = open(dirSemanticLabelFileName,'w');
dirSemanticLabelFile.write('Number of Semantic Labels for Each Dataset' + '\n');


allTrainList=[];
allTestList=[];

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
		#2. Get Number of Training Samples and Number of Testing Samples
		
		
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
				
		#2 Get Number of Training Samples and Number of Testing Samples
		########################################################################################################################
		
		#numTrainLabels
		trainDirPath = datadumpPath + "/" + trainDirName;
		tempTrainList = os.listdir(trainDirPath);
		allTrainList.extend(tempTrainList);
		numTrainLabels = len(tempTrainList);
		totalTrain += numTrainLabels;
		
		#2a Get the number of training data samples in each file
		total_semantic_train_labels = extract_total_semantic_labels_train(trainDirPath,datadump);
		avg_semantic_train_labels = total_semantic_train_labels/numTrainLabels;

				
		#numTestLabels
		testDataPath = datadumpPath + "/" + testDirName;
		tempTestList = os.listdir(testDataPath);
		allTestList.extend(tempTestList);		
		numTestLabels = len(tempTestList);
		totalTest += numTestLabels	
		
		#2b Get the number of training data samples in each file
		total_semantic_test_labels = extract_total_semantic_labels_test(testDataPath,datadump);
		avg_semantic_test_labels = total_semantic_test_labels/numTestLabels;
		
		print 'Num Train Labels : ',numTrainLabels
		print 'Num Test Labels : ',numTestLabels		
		
		#We also need to calculate the cumulative results
		totalAccuracy += float(accuracy.strip('%')) * numTestLabels;
		totalMRR += float(mrr) * numTestLabels;		
		########################################################################################################################
		
		
		
		#3 -------> Printing the data to a CSV File
		order_list = [datadump,mrr,accuracy,str(numTrainLabels),str(numTestLabels),str(avg_semantic_train_labels),str(avg_semantic_test_labels)];
		order_str = sep.join(order_list);
		dirCSVFile.write(order_str + '\n');
		

allTrainSet = set(allTrainList);
allTestSet = set(allTestList);
diffSet = allTrainSet - allTestSet;

dirCSVFile.write('\n')
dirCSVFile.write('Total Training Labels:'+str(len(allTrainSet)) + '\n');
dirCSVFile.write('Total Testing Labels:'+str(len(allTestSet)) + '\n');
dirCSVFile.write('Number of Labels Which are present only in Testing or Training Set:'+str(len(diffSet)) + '\n');
		
totalAccuracy = totalAccuracy/totalTest
totalMRR = totalMRR/totalTest


dirCSVFile.write('\n')
dirCSVFile.write('Total Accuracy:' + str(totalAccuracy) + '\n');
dirCSVFile.write('Total MRR:' + str(totalMRR) + '\n');
