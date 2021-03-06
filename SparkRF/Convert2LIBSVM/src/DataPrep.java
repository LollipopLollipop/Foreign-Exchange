import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Convert the comma separated foreign exchange data records to LIBSVM format 
 * that can be directly processed by MLLIB
 * @author JodiezZ
 *
 */
public class DataPrep {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//use buffered reader and writer for fast IO
			BufferedReader br = new BufferedReader(new FileReader("Preped-USDJPY-2015-08.csv"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("LIBSVM-USDJPY-2015-08.txt"));

			String thisLine = null;
			ArrayList<CandleStick> totalInstances = new ArrayList<CandleStick>();
			//int count = 0;
			while ((thisLine = br.readLine()) != null) {
				StringTokenizer featureTokenizer = new StringTokenizer(thisLine, ",");
				String[] featuresNLabel = new String[6];
				int i=0;
				while(featureTokenizer.hasMoreTokens()){
					featuresNLabel[i++] = featureTokenizer.nextToken();
				}
				//wrap each record into CandleStick obj
				CandleStick cs = new CandleStick(featuresNLabel);
				totalInstances.add(cs);
				//count ++;
			}
			//randomly split instances into training and testing sets
			Collections.shuffle(totalInstances);
			ArrayList<CandleStick> sampledInstances = 
					new ArrayList<CandleStick>(totalInstances.subList(0, 10000));
			for(CandleStick cs:sampledInstances){
				bw.write(cs.toLIBSVMFormat());
			}
			br.close();
			bw.flush();
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
