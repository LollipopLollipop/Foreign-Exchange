import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
/**
 * 
 * @author ding
 * Main workflow of HW4: 
 * load prep-ed data from csv to Cassandra (can be skipped next time)
 * load training dataset from Cassandra
 * build the random forest based on training set
 * classify testing set, analyze predicted accuracy and store result into Cassandra
 */

public class HW4 {
	//private static final int TOTAL_INSTANCES = 10000;
	public static void main(String[] args) {
		Cluster cluster;
		Session session;
		
		// Connect to the cluster and keyspace "demo"
		cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		session = cluster.connect("dev");
		
		//load preped data in csv file to Cassandra table (only executed once)
//		try {
//			//use buffered reader and writer for fast IO
//			BufferedReader br = new BufferedReader(new FileReader(args[0]));
//			String thisLine = null;
//			
//			while ((thisLine = br.readLine()) != null) {
//				StringTokenizer featureTokenizer = new StringTokenizer(thisLine, ",");
//				String[] featuresNLabel = new String[6];
//				int i=0;
//				while(featureTokenizer.hasMoreTokens()){
//					featuresNLabel[i++] = featureTokenizer.nextToken();
//				}
//				// Insert one record into the users table
//				session.execute("insert into forex (time, currency_pair, high_bid, low_bid, close_bid, label) "
//						+ "values ('"+featuresNLabel[1]+"','"+featuresNLabel[0]+"',"+
//						Double.parseDouble(featuresNLabel[2])+","+
//						Double.parseDouble(featuresNLabel[3])+","+
//						Double.parseDouble(featuresNLabel[4])+","+
//						Integer.parseInt(featuresNLabel[5])+")");				
//			}
//		}catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		int rfid = Integer.parseInt(args[0]);
		
		ArrayList<CandleStick> testingInstances = 
				new ArrayList<CandleStick>();
		ArrayList<CandleStick> trainingInstances = 
				new ArrayList<CandleStick>();
		
		
		// Use select to get records, 10000 is chosen as the dataset size with 8K being training data and 2K being testing data
		ResultSet results = session.execute("SELECT * FROM forex LIMIT 10000");
		int i=0;
		for (Row row : results) {
			//System.out.format("%s %s\n", row.getString("time"), row.getDouble("high_bid"));
			CandleStick cs = new CandleStick(row.getString("currency_pair"), 
					row.getString("time"), row.getDouble("high_bid"),
					row.getDouble("low_bid"), row.getDouble("close_bid"),
					row.getInt("label"));
			if(i<8000)
				trainingInstances.add(cs);
			else
				testingInstances.add(cs);
			i++;
		}
		
		//N specifies the number of trees to grow
		int N = Integer.parseInt(args[1]);
		//System.out.println("N value is " + N);
		RandomForest rf = new RandomForest(N);
		rf.buildForest(trainingInstances);
		ArrayList<Integer> predictedLabels = rf.classify(testingInstances);
		//System.out.println("predicted size "+ predictedLabels.size());
		int matchCount = 0;
		i=0;
		for(CandleStick testCase : testingInstances){
			if(testCase.getLabel()==predictedLabels.get(i))
				matchCount++;
			i++;
		}
		double precision = (double)matchCount/2000;
		//store prediction result and random forest info into Cassandra 
		//rfid is given by command line params
		session.execute("INSERT INTO randforest (rfid, trees, stats, n, precision) VALUES ("+rfid+", '"+
				rf.toString(0)+"', '"+rf.toString(1)+"',"+N+","+precision+")");
		
		// Clean up the connection by closing it
		cluster.close();
		
	}

}
