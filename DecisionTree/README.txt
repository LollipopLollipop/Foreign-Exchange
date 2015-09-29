In the src folder, there are three source code files:
#1 HW2.java 
	This is the main work routine of HW2.
	First read in prep-ed data records, then split into training and testing sets, grow the decision tree based on training set and finally predict labels for test samples, a comparison between predicted labels and actual labels are also included.

#2 CandleStick.java
	Wrapper class extended from HW1 to better represent each prep-ed data record and also allow more flexible object oriented operations.

#3 DecisionTree.java
	Custom data structure created to represent the decision tree built in this assignment. A decision tree is represented by a root node. The nested DTNode defined in this class represent each node inside the tree, contains information such as associated instances, test condition, entropy, left and right children, etc. 
