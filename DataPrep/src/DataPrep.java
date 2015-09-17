import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;


public class DataPrep {

	public static void main(String[] args) {
		try {
			//use buffered reader and writer for fast IO
			//input and output csv files are passed in as arguments
			//at current stage, this DataPrep only cater to one currencyPair,
			//but can be extended to support multiple pairs as well in the future 
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
			String thisLine = null;
			//timeSlot refers to the current minute being processed
			String timeSlot = null;
			//closePrice, highPrice and lowPrice refer to 
			//records with close, high and low BID price in that minute
			String[] closePrice = new String[4];
			String[] highPrice = new String[4];
			String[] lowPrice = new String[4];
			Double localMax = Double.MIN_VALUE;
			Double localMin = Double.MAX_VALUE;
			
			//CandleStick obj contains the high, low, and close BID prices for transactions in certain time frame
			CandleStick prev = null;
			
			while ((thisLine = br.readLine()) != null) {
				//use tokenizer but not split for fast processing
				StringTokenizer fields = new StringTokenizer(thisLine, ",");
				//current record parsed store in curPrice
				String[] curPrice = new String[4];
				int i=0;
				while(fields.hasMoreTokens()){
					curPrice[i++] = fields.nextToken();
				}
				
				//if not within 1 minute block, create a CandleStick obj accordingly
				if(timeSlot!=null && !curPrice[1].substring(0, 14).equals(timeSlot)){
					
					CandleStick cur = new CandleStick(timeSlot, highPrice, lowPrice, closePrice);
					//every time a new CandleStick obj created, 
					//the prev CandleStick obj's labels can hence be determined
					//therefore format and output the prev CandleStick obj as one record of output
					if(prev!=null){
						bw.write(prev.getFeaturesAndLabel(cur));
						bw.flush();						
					}
					prev = cur;
					
					//update time related reference accordingly 
					timeSlot = curPrice[1].substring(0, 14);
					closePrice = new String[4];
					highPrice = new String[4];
					lowPrice = new String[4];
					localMax = Double.MIN_VALUE;
					localMin = Double.MAX_VALUE;
					
				}
				if(timeSlot==null)
					timeSlot = curPrice[1].substring(0, 14);
				
				//compare and update localMax, localMin and localClose for each 1 min time frame
				Double curVal = Double.parseDouble(curPrice[2]);
				if(curVal > localMax){
					highPrice = curPrice;
					localMax = curVal;
				}
				if(curVal < localMin){
					lowPrice = curPrice;
					localMin = curVal;
				}	
				closePrice = curPrice;
				
			}
			br.close();
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
