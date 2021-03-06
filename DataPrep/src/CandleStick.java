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
	public CandleStick(){
		
	}
	public CandleStick(String time, String[] high, String[] low, String[] close){
		this.currencyPair = high[0];
		this.time = time;
		this.high = Double.parseDouble(high[2]);
		this.low = Double.parseDouble(low[2]);
		this.close = Double.parseDouble(close[2]);
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
	
	public Double getClose(){
		return this.close;
	}
}
