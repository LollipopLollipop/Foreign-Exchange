import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author dingz
 * CandleStick contains the high, low, and close 
 * transaction details in terms of BID price in certain time frame
 * 
 * At current stage, all the high/low/close variabes are BID prices only 
 * label variable indicates the variation of close bid price at next time frame
 * 1: rise
 * -1: drop
 * 0: no change
 * 
 * Wrap input records at each time frame into user-defined class 
 * to allow better flexibility when it comes to features construction
 * 
 *
 */
public class CandleStick {
	private String currencyPair = null;
	private String time = null;
	//bid price 
	private Double high = 0.0;
	private Double low = 0.0;
	private Double close = 0.0;
	private int label = 0;
	//private HashMap<String, Object> featuresMap = new HashMap<String, Object>();
	public CandleStick(){
		
	}
	//ctor when passed in prep-ed data records
	public CandleStick(String[] featuresNLabel){
		this.currencyPair = featuresNLabel[0];
		this.time = featuresNLabel[1];
		this.high = Double.parseDouble(featuresNLabel[2]);
		this.low = Double.parseDouble(featuresNLabel[3]);
		this.close = Double.parseDouble(featuresNLabel[4]);
		this.label = Integer.parseInt(featuresNLabel[5]);
		
//		featuresMap.put("time", this.time);
//		featuresMap.put("high", this.high);
//		featuresMap.put("low", this.low);
//		featuresMap.put("close", this.close);
	}
	
	//ctor when passed in raw csv records
	public CandleStick(String time, String[] high, String[] low, String[] close){
		this.currencyPair = high[0];
		this.time = time;
		this.high = Double.parseDouble(high[2]);
		this.low = Double.parseDouble(low[2]);
		this.close = Double.parseDouble(close[2]);
		
//		featuresMap.put("time", this.time);
//		featuresMap.put("high", this.high);
//		featuresMap.put("low", this.low);
//		featuresMap.put("close", this.close);
		
	}
	public String getFeaturesAndLabel(CandleStick next) {
		//use StringBuilder other than String concatenation for fast operation
		StringBuilder sb = new StringBuilder();
		sb.append(this.currencyPair+",");
		sb.append(this.time+",");
		sb.append(this.high+",");
		sb.append(this.low+",");
		sb.append(this.close+",");
		if(next.getClose()>this.close)
			sb.append(1);
		else if(next.getClose()<this.close){
			sb.append(-1);
		}else{
			sb.append(0);
		}
		sb.append("\n");
		return sb.toString();
	}
//	public Set<String> getFeatures(){
//		HashSet<String> features = new HashSet<String>();
//		features.add("time");
//		features.add("high");
//		features.add("low");
//		features.add("close");
//		return this.featuresMap.keySet(); 
//	}
	public String getTime(){
		return this.time;
	}
	public Double getHigh(){
		return this.high;
	}
	public Double getLow(){
		return this.low;
	}
	public Double getClose(){
		return this.close;
	}
	public int getLabel(){
		return this.label;
	}
//	public Object getFeatureVal(String attribute) {
//		// TODO Auto-generated method stub
//		return this.featuresMap.get(attribute);
//	}
//	public Double getPrice(String attribute){
//		if(attribute.equals("high"))
//			return this.high;
//		else if(attribute.equals("low"))
//			return this.low;
//		else
//			return this.close;
//	}
	
	public String toString(){
		//use StringBuilder other than String concatenation for fast operation
		StringBuilder sb = new StringBuilder();
		sb.append(this.currencyPair+",");
		sb.append(this.time+",");
		sb.append(this.high+",");
		sb.append(this.low+",");
		sb.append(this.close+",");
		sb.append(this.label);
		return sb.toString();
	}
	
	public static Comparator<CandleStick> CandleStickTimeComparator 
    	= new Comparator<CandleStick>() {

		@Override
		public int compare(CandleStick o1, CandleStick o2) {
			// TODO Auto-generated method stub
			return o1.getTime().compareTo(o2.getTime());
		}
	
	};
	
	public static Comparator<CandleStick> CandleStickHighPriceComparator 
    	= new Comparator<CandleStick>() {

		@Override
		public int compare(CandleStick o1, CandleStick o2) {
			// TODO Auto-generated method stub
			return o1.getHigh().compareTo(o2.getHigh());
		}
	
	};
	
	public static Comparator<CandleStick> CandleStickLowPriceComparator 
		= new Comparator<CandleStick>() {

		@Override
		public int compare(CandleStick o1, CandleStick o2) {
			// TODO Auto-generated method stub
			return o1.getLow().compareTo(o2.getLow());
		}
	
	};
	
	public static Comparator<CandleStick> CandleStickClosePriceComparator 
		= new Comparator<CandleStick>() {
	
		@Override
		public int compare(CandleStick o1, CandleStick o2) {
			// TODO Auto-generated method stub
			return o1.getClose().compareTo(o2.getClose());
		}

	};
	
	/**
	 * 
	 * @param toCmp
	 * @param testCondition associated with each decision tree node, int index for features included in CandleStick
	 * 0-time, 1-high bid price, 2-low bid price, 3-close bid price
	 * @return
	 */
	public int compareWith(CandleStick toCmp, int testCondition) {
		if(testCondition==0){
			return this.getTime().compareTo(toCmp.getTime());
		}else if(testCondition==1){
			return this.getHigh().compareTo(toCmp.getHigh());
		}else if(testCondition==2){
			return this.getLow().compareTo(toCmp.getLow());
		}else if(testCondition==3){
			return this.getClose().compareTo(toCmp.getClose());
		}else{
			System.err.println("invalid attribute idx...error");
			return 0;
		}
	}
}
