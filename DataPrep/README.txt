In the src folder, there are two source code files:
#1 DataPrep.java 
	This is the main source code doing the data preparation such as time aligning, high/low/close price filtering and features construction, etc

#2 CandleStick.java
	Class created to hold transaction details at each time frame (1 min in this case). It contains the high, low and close BID prices in each time frame as well as a label to represent if close bid prices rise in next time frame

	This class is created with an attempt to have better flexibility when it comes to features construction. For example, in the future new features such as ASK prices can be added easily. 
