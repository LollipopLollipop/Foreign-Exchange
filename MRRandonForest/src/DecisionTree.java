import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.WritableComparable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author dingz
 * DecisionTree & DTNode
 * DTNode contains:
 * instances associate with the node
 * entropy of associated instances
 * left and right children 
 * test condition: 
 * 	testCond integer idx to represent which feature to split &
 * 	thresholdInstance 
 * 
 * 
 */

public class DecisionTree implements java.io.Serializable{
	private DTNode root = new DTNode();
	private Double precision = 0.0;
	//features used by this DTree
	private List<Integer> featureIdx = new ArrayList<Integer>();
	public DecisionTree(){}
	public DecisionTree(ArrayList<CandleStick> allTrainingInstances, List<Integer> featureIdx){
		this.root.addInstances(allTrainingInstances);
		this.featureIdx = featureIdx;
	}
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		json.put("precision", this.precision);
		json.put("features", this.featureIdx);
		json.put("tree", inOrder(this.root));
		return json;
		
	}
	public void train(){
		buildDTree(this.root);
	}
	
	private DTNode buildDTree(DTNode curRoot){
		//leaf node, all nodes are pure
		if(curRoot.setEntropy()==0||curRoot.getInstancesSize()==1){
//			System.out.println("cur root entropy =0, instances are " + curRoot.getInstancesSize());
//			for(CandleStick cs:curRoot.getInstances()){
//				System.out.println(cs.toString());
//			}
			return curRoot;
		}
		//System.out.println("cur root entropy " + curRoot.getEntropy());
		int bestAttr = -1;
		double bestIG = Double.MIN_VALUE;
		//the threshold instance at current root node
		CandleStick thresholdInstance = new CandleStick();
		//subsets of instances associated with current node, will be assigned to left and right child node of current node
		List<CandleStick> leftInstances = new ArrayList<CandleStick>();
		List<CandleStick> rightInstances = new ArrayList<CandleStick>();
		
		//loop through attributes to find the best split
		//4 features in current prep-ed dataset
		for(int i:this.featureIdx){
			//sort instances in each feature domain
			if(i==0){
				curRoot.getInstances().sort(CandleStick.CandleStickTimeComparator);
			}else if(i==1){
				curRoot.getInstances().sort(CandleStick.CandleStickHighPriceComparator);
			}else if(i==2){
				curRoot.getInstances().sort(CandleStick.CandleStickLowPriceComparator);
			}else if(i==3){
				curRoot.getInstances().sort(CandleStick.CandleStickClosePriceComparator);
			}
			
			//loop through all possible split between nodes in each feature domain to find the best split
			for(int thresholdIdx = 1; thresholdIdx<curRoot.getInstancesSize(); thresholdIdx++){
				List<CandleStick> left =  new ArrayList<CandleStick>(
						curRoot.getInstances().subList(0, thresholdIdx));
				List<CandleStick> right =  new ArrayList<CandleStick>(
						curRoot.getInstances().subList(thresholdIdx, curRoot.getInstancesSize()));

				
				double leftEntropy = calcEntropy(left);
				//System.out.println("cur root left entropy " + leftEntropy);
				double rightEntropy = calcEntropy(right);
				//System.out.println("cur root right entropy " + rightEntropy);
				double infoGain = curRoot.getEntropy() - 
						((double)left.size()/curRoot.getInstancesSize())*leftEntropy - 
						((double)right.size()/curRoot.getInstancesSize())*rightEntropy;
				//System.out.println(infoGain);
				//System.out.println(bestIG);
				if(infoGain>bestIG){
					
					bestIG = infoGain;
					bestAttr = i;
					//System.out.println("update best attr " + bestAttr);
					thresholdInstance = curRoot.getInstances().get(thresholdIdx);
					leftInstances = left;
					rightInstances = right;
				}
			}
		}
		
		if(bestAttr==-1){
			System.out.println("best attr is -1");
//			for(CandleStick cs:curRoot.getInstances()){
//				System.out.println(cs.toString());
//			}
			return curRoot;
		}

		//set current root node's test condition, left and right children based on argmax(IG)
		curRoot.setTestCondition(bestAttr, thresholdInstance);
		DTNode leftBranch = new DTNode();
		leftBranch.addInstances(leftInstances);
		curRoot.left = leftBranch;
		DTNode rightBranch = new DTNode();
		rightBranch.addInstances(rightInstances);
		curRoot.right = rightBranch;

		//recursively grow the d-tree
		buildDTree(curRoot.left);
		buildDTree(curRoot.right);
		return curRoot;
	}
	
	public String toString(){
		return inOrder(root);
	}
	
	public void setPrecision(double d){
		this.precision = d;
	}
	private String inOrder(DTNode curRoot)
	{
	 
	  if(curRoot==null) return " ";
	  return inOrder(curRoot.getLeft())+curRoot.toString()+inOrder(curRoot.getRight());  
	  
	}


	public class DTNode implements java.io.Serializable{
		private transient List<CandleStick> instances = new ArrayList<CandleStick>();
		private double entropy;
		private DTNode left;
		private DTNode right;
		private int testCond;
		private CandleStick thresholdIns;
		
		public DTNode(){
			
		}
		public int getInstancesSize() {
			
			return this.instances.size();
		}
		public List<CandleStick> getInstances() {
			
			return this.instances;
		}
		private void addInstances(List<CandleStick> instances){
			this.instances = instances;
		}
		private double setEntropy(){
			this.entropy = calcEntropy(this.instances);
			return this.entropy;
		}
		private double getEntropy(){
			return this.entropy;
		}

		private DTNode getLeft(){
			return this.left;
		}
		private DTNode getRight(){
			return this.right;
		}
		private void setTestCondition(int c, CandleStick thresholdInstance){
			//System.out.println("set test cond on attribute on " + c + " with threshold instance as " +
			//		thresholdInstance.toString());
			this.testCond = c;
			this.thresholdIns = thresholdInstance;
		}
		public String toString(){
			String s = null;
			if(this.thresholdIns==null){
				s="[]";
				return s;
			}
			if(this.testCond==0)
				s = "[time|"+this.thresholdIns.getTime()+"]";
			else if(this.testCond==1)
				s = "[high|"+this.thresholdIns.getHigh()+"]";
			else if(this.testCond==2)
				s = "[low|"+this.thresholdIns.getLow()+"]";
			else if(this.testCond==3)
				s = "[close|"+this.thresholdIns.getClose()+"]";
			return s;
		}
	}
	
	//math util to calcEntropy given list of instances
	private double calcEntropy(List<CandleStick> sampleInstances){
		int n = sampleInstances.size();
		int riseCount = 0;
		int dropCount = 0;
		int constantCount = 0;
		for(CandleStick instance : sampleInstances){
			if(instance.getLabel()==1)
				riseCount++;
			else if(instance.getLabel()==-1)
				dropCount++;
			else
				constantCount++;
		}
		double riseProb = ((double)riseCount)/n;
		double dropProb = ((double)dropCount)/n;
		double constantProb = ((double)constantCount)/n;
		Double entropy = 0.0;
		//avoid NAN condition Math.log(0) is invalid
		if(riseProb!=0)
			entropy -= riseProb*Math.log(riseProb);
		if(dropProb!=0)
			entropy -= dropProb*Math.log(dropProb);
		if(constantProb!=0)
			entropy -= constantProb*Math.log(constantProb);
		if(entropy.equals(Double.NaN)){
			System.out.println(n);
			System.out.println(riseProb);
			System.out.println(dropProb);
			System.out.println(constantProb);
		}
		return entropy;
	}
	
	public ArrayList<Integer> classify(ArrayList<CandleStick> testingInstances) {
		ArrayList<Integer> predictedLabels = new ArrayList<Integer>();
		for(CandleStick cs: testingInstances){
			int predictedLabel = findMatch(this.root, cs);
			predictedLabels.add(predictedLabel);
		}
		return predictedLabels;
	}
	
	//find which deepest subset given CandleStick obj should be grouped into
	private Integer findMatch(DTNode curRoot, CandleStick cs) {
		if(curRoot.entropy==0 || (curRoot.left==null&&curRoot.right==null)){
			return curRoot.getInstances().get(0).getLabel();
		}
		else{
			if(inLeftBranch(curRoot, cs)){
				return findMatch(curRoot.left, cs);
			}else{
				return findMatch(curRoot.right, cs);
			}
				
		}
	}
	
	//helper function to determine which sub branch given CandleStick obj should be grouped into
	private boolean inLeftBranch(DTNode curRoot, CandleStick cs) {
		CandleStick toCmp = curRoot.thresholdIns;
		int testCondition = curRoot.testCond;
		if(cs.compareWith(toCmp, testCondition)<0){
			return true;
		}else{
			return false;
		}
	}
	public Double getPrecision() {
		// TODO Auto-generated method stub
		return this.precision;
	}
	
}
