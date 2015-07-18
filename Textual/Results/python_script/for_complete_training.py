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
order_list_names = ['Data Set','MRR','Accuracy','Number of Training Labels','Number of Testing Labels'];
order_str_names = sep.join(order_list_names);

dirCSVFileName='results_'+dirName+'.csv';
dirCSVFile=open(dirCSVFileName,'w');
dirCSVFile.write(order_str_names + '\n');

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
		tempTrainList = os.listdir(datadumpPath + "/" + trainDirName);
		allTrainList.extend(tempTrainList);
		numTrainLabels = len(tempTrainList);
		totalTrain += numTrainLabels;
		
		#numTestLabels
		tempTestList = os.listdir(datadumpPath + "/" + testDirName);
		allTestList.extend(tempTestList);		
		numTestLabels = len(tempTestList);
		totalTest += numTestLabels	
		
		print 'Num Train Labels : ',numTrainLabels
		print 'Num Test Labels : ',numTestLabels		
		
		#We also need to calculate the cumulative results
		totalAccuracy += float(accuracy.strip('%')) * numTestLabels;
		totalMRR += float(mrr) * numTestLabels;		
		########################################################################################################################
		
		
		
		#3 -------> Printing the data to a CSV File
		order_list = [datadump,mrr,accuracy,str(numTrainLabels),str(numTestLabels)];
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
