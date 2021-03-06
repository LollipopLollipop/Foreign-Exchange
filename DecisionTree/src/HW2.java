import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * 
 * @author ding
 * Main workflow of HW2: 
 * read 10000 prep-ed data instances
 * randomize into training and testing set
 * grow the decision tree based on training set
 * classify testing set and analyze predicted result
 */

public class HW2 {
	private static final int TOTAL_INSTANCES = 10000;
	public static void main(String[] args) {
		try {
			//use buffered reader and writer for fast IO
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
			//read 10K instances
			String thisLine = null;
			ArrayList<CandleStick> totalInstances = new ArrayList<CandleStick>();
			int count = 0;
			while ((thisLine = br.readLine()) != null && count<TOTAL_INSTANCES) {
				StringTokenizer featureTokenizer = new StringTokenizer(thisLine, ",");
				String[] featuresNLabel = new String[6];
				int i=0;
				while(featureTokenizer.hasMoreTokens()){
					featuresNLabel[i++] = featureTokenizer.nextToken();
				}
				//wrap each record into CandleStick obj
				CandleStick cs = new CandleStick(featuresNLabel);
				totalInstances.add(cs);
				count ++;
			}
			//randomly split instances into training and testing sets
			Collections.shuffle(totalInstances);
			ArrayList<CandleStick> testingInstances = 
					new ArrayList<CandleStick>(totalInstances.subList(0, (int) (TOTAL_INSTANCES*0.2)));
			ArrayList<CandleStick> trainingInstances = 
					new ArrayList<CandleStick>(totalInstances.subList((int) (TOTAL_INSTANCES*0.2), TOTAL_INSTANCES));
			
			//System.out.println(trainingInstances.size());
			DecisionTree dt = new DecisionTree(trainingInstances);
			//build the tree
			dt.train();
			//dt.printAll();
			//System.out.println(testingInstances.size());
			//predict labels for testing instances based on the d-tree
			ArrayList<Integer> predictedLabels = dt.classify(testingInstances);
			//System.out.println("predicted size "+ predictedLabels.size());
			int matchCount = 0;
			for(int i=0; i<TOTAL_INSTANCES*0.2; i++){
				StringBuilder sb = new StringBuilder();
				sb.append("label: ");
				sb.append(testingInstances.get(i).getLabel());
				sb.append("\t");
				sb.append("predicted: ");
				sb.append(predictedLabels.get(i));
				sb.append("\n");
				bw.write(sb.toString());
				
				if(testingInstances.get(i).getLabel()==predictedLabels.get(i))
					matchCount++;
			}
			bw.write(matchCount+" out of 2000 predicted correctly. prob=" + (double)matchCount/2000 + "\n");
			br.close();
			bw.flush();
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