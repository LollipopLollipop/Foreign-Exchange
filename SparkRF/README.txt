There are 2 source code folders for this assignment: Convert2LIBSVM & SparkRF

Convert2LIBSVM (Java):
	As the Spark MLLIB documentation indicates, many machine learning estimators can work directly with text file in LIBSVM format. Based on this, the first part of the project is to transform the Foreign Exchange data from the comma separated format to LIBSVM format. 

SparkRF (Scala):
	This part performs RandomForest Classification on the LIBSVM formatted data. I used sbt to package source codes (with dependencies specified) into jar that can be run as self-contained applications on Hortonworks Sandbox Spark.

I understand that we better read/write data from/to Cassandra. After some research I found that we can use spark-cassandra-connector(https://github.com/datastax/spark-cassandra-connector/) to access Cassandra from Spark. 

However, since I installed Cassandra on host machine and ran Spark on VirtualBox sandbox, I was stuck trying to get the 2 processes running simutaneously but with no luck. The system kept complaining that some ports were already used by one or another. I remember Professor did mention that finish Spark RandomForest implementation without Cassandra is acceptable.