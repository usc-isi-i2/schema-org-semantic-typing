Contact Information:
I have tried my best to keep the information as accurate as possible.
If there is any issue with the running of the code please email me at pranavgupta3131@gmail.com

AIM of JAR : 
Will train and test on the datasets provided to it.

AIM of FILE :
1. Set up correct directory structure so as jar can run properly
2. Commands required to run the jar
3. Running the bash scripts

#1 Setting up correct directory structure
1. Make a folder with path as
 "/home/pranav/exp_train_all" 
This is the folder where everything
	a.Dynamically created training and testing sets
	b.Results
	c.Sources
will be stored
2. Make a folder with path as
"/home/pranav/data_files2"
This is the folder where your datasets(i.e. the nquad files ) will be stored 
E.g. You have three files
	a.) x.nq
	b.) y.nq
	c.) z.nq

Then the directory should look like this
/home/pranav/data_files2
	-->x/
		-->x.nq
	-->y/
		-->y.nq
	-->z/
		-->z.nq

#2 Commands Required to run the jar
If your directory structure is correctly set up then the following command should work. 
( If problems are encountered during running of this command, please email me at pranavgupta3131@gmail.com )
java -cp karma-nquad-with-dependencies.jar edu.isi.driver.DriverCompleteTraining <Name of Directory where you want to create all files> <true/false whether training is restricted or not> <true/false whether semantic labels are removed or not> <list of datasets>  

java -cp karma-nquad-with-dependencies.jar edu.isi.driver.DriverCompleteTraining complete_no_limit false false x y z ( example of unrestricted training)

java -cp karma-nquad-with-dependencies.jar edu.isi.driver.DriverCompleteTraining complete_limit true false x y z ( example of limited training)

#3 Running the bash scripts
This section will be updated when bash scripts are available.
If you still need the instructions, please drop a mail at pranavgupta3131@gmail.com
