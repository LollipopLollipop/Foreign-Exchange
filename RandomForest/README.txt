In the src folder, there are four source code files:
#1 HW3.java 
	This is the main work routine of HW3.
	First read in prep-ed data records, then split into training and testing sets, grow the random forest based on training set and finally predict labels for test samples, a comparison between predicted labels and actual labels are also included.

#2 CandleStick.java
	Wrapper class extended from HW1 to better represent each prep-ed data record and also allow more flexible object oriented operations.

#3 DecisionTree.java
	Custom data structure created to represent the decision tree built in this assignment. A decision tree is represented by a root node. The nested DTNode defined in this class represent each node inside the tree, contains information such as associated instances, test condition, entropy, left and right children, etc. 

#4 RandomForest.java
	Custom data structure created to represent the random forest. RandomForest class contains an integer variable N to represent the number of trees in the forest, a list of DecisionTree and a list of float point numbers to represent the performance stats of each DecisionTree.

