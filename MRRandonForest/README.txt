In the src folder, there are four source code files:
#1 MRRandomForestSetup.java 
	Setup the Cassandra keyspace and columnfamily for future training
	Take in user input: N

#2 MRRandomForestSetup.java 
	Train the RandomForest model from data stored in Cassandra
	Store the model in JSON format at randomforest_model/ 

#3 CandleStick.java
	Wrapper class extended from HW1 to better represent each prep-ed data record and also allow more flexible object oriented operations.

#4 DecisionTree.java
	Custom data structure created to represent the decision tree built in this assignment. A decision tree is represented by a root node. The nested DTNode defined in this class represent each node inside the tree, contains information such as associated instances, test condition, entropy, left and right children, etc. 

The RandomForest JSON serialized model is stored at randomforest_model/part-r-00000
