import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author dingz
 * RandomForest contains:
 * 	N: number of trees in the forest
 *  trees: list of trees grown based on subset of training data
 *  stats: performance stats for each tree
 * 
 * 
 */

public class RandomForest {
	private int N = 0;
	private static final List<Integer> totalFeatures = Arrays.asList(0, 1, 2, 3);
	private ArrayList<DecisionTree> trees = new ArrayList<DecisionTree>();
	private ArrayList<Double> stats = new ArrayList<Double>();
	public RandomForest(int N){
		this.N = N;
	}
	public void buildForest(ArrayList<CandleStick> instances) {
		int TOTAL_INSTANCES = instances.size();
		for(int i=0; i<this.N; i++){
			//randomly select subset of about 2/3 of the instances for training 
			Collections.shuffle(instances);
			ArrayList<CandleStick> trainInstances = 
					new ArrayList<CandleStick>(instances.subList(0, (int) (2*TOTAL_INSTANCES/3)));
			ArrayList<CandleStick> testInstances = 
					new ArrayList<CandleStick>(instances.subList((int) (2*TOTAL_INSTANCES/3), TOTAL_INSTANCES));
			//randomly select subset of about √(# of features)
			Collections.shuffle(totalFeatures);
			//4 features in current data format, square root of 4 is 2
			List<Integer> usedFeatures = totalFeatures.subList(0, 2);
			System.out.println("selected "+ usedFeatures.size() + " features are " + usedFeatures.get(0) + "|" + usedFeatures.get(1));
			DecisionTree dt = new DecisionTree(trainInstances, usedFeatures);
			//build the tree
			dt.train();
			//predict labels for testing instances based on the d-tree
			ArrayList<Integer> predictedLabels = dt.classify(testInstances);
			//get the performance stats
			int matchCount = 0;
			int j = 0;
			for(CandleStick ins : testInstances){
				if(ins.getLabel() == predictedLabels.get(j++))
					matchCount++;
			}
			double accuracy = (double)matchCount/((double)j);
			System.out.println("tree " + i + "'s accuracy is " + accuracy);
			trees.add(dt);
			stats.add(accuracy);
		}
		
	}
	public String toString(int flag){
		String rf = "";
		if(flag==0){
			for(int i=0; i<this.N; i++){
				rf+=(i+":"+trees.get(i).toString()+"\n");
			}
		}
		else{
			for(int i=0; i<this.N; i++){
				rf+=(i+":"+stats.get(i)+"\n");
			}
		}
		return rf;
	}
	public ArrayList<Integer> classify(ArrayList<CandleStick> testingInstances) {
		//ArrayList<Integer> predictedLabels = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> treesPredictedLabels = new ArrayList<ArrayList<Integer>>();
		//get predicted labels by each tree
		for(int i=0; i<this.N; i++){
			ArrayList<Integer> labelsPerTree = trees.get(i).classify(testingInstances);
			treesPredictedLabels.add(labelsPerTree);
		}
		
		return countVotes(treesPredictedLabels);
		
		
	}
	//determine the predicted label by a voting mechanism
	private ArrayList<Integer> countVotes(
			ArrayList<ArrayList<Integer>> treesPredictedLabels) {
		
		ArrayList<Integer> predictedLabels = new ArrayList<Integer>();
		int insCount = treesPredictedLabels.get(0).size();
		System.out.println("test instance count " + insCount);
		for(int k=0; k<insCount; k++){
			Map<Integer, Integer> votes = new HashMap<Integer, Integer>();
			String labels = " ";
			for(int i=0; i<this.N; i++){
				Integer label = treesPredictedLabels.get(i).get(k);
				labels += label+" ";
				Integer count = votes.get(label);
				votes.put(label, count == null ? 1 : count + 1);
			}
			
			//get the label with max votes
			Entry<Integer, Integer> max = null;

		    for (Entry<Integer, Integer> e : votes.entrySet()) {
		        if (max == null || e.getValue() > max.getValue())
		            max = e;
		    }
		    labels += "max label is " + max.getKey();
		    System.out.println(k+": " + labels);
		    predictedLabels.add(max.getKey());
		}
		
		return predictedLabels;
	}
}
