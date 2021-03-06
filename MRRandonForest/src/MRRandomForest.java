

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.hadoop.ColumnFamilyInputFormat;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
/**
 * @author dingz
 * MRRandomForest
 * 	Train the RandomForest model from data stored in Cassandra
 * 	Store the model in JSON format 
 * 
 * 
 */
public class MRRandomForest extends Configured implements Tool
{
    private static final Logger logger = LoggerFactory.getLogger(MRRandomForest.class);    
    static final String KEYSPACE = "randomforest";
    static final String COLUMN_FAMILY = "forex_data";
    private static final List<Integer> totalFeatures = Arrays.asList(0, 1, 2, 3);
    private static final String OUTPUT_PATH_PREFIX = "randomforest_model";

    public static void main(String[] args) throws Exception
    {
        // Let ToolRunner handle generic command-line options
        ToolRunner.run(new Configuration(), new MRRandomForest(), args);
        System.exit(0);
    }

    public static class GrowTreeMapper extends Mapper<ByteBuffer, SortedMap<ByteBuffer, IColumn>, Text, Text>
    {
        public void map(ByteBuffer key, SortedMap<ByteBuffer, IColumn> columns, Context context) throws IOException, InterruptedException
        {
        	//randomly select subset of about √(# of features)
			Collections.shuffle(totalFeatures);
			//4 features in current data format, square root of 4 is 2
			List<Integer> usedFeatures = totalFeatures.subList(0, 2);
			ArrayList<CandleStick> trainInstances = 
					new ArrayList<CandleStick>();
			ArrayList<CandleStick> testInstances = 
					new ArrayList<CandleStick>();
			int count = 0;
            for (IColumn column : columns.values())
            {
                if(count<(MRRandomForestSetup.TRAINING_COUNT*2/3))
                	trainInstances.add(new CandleStick(ByteBufferUtil.string(column.name())));
                else
                	testInstances.add(new CandleStick(ByteBufferUtil.string(column.name())));
                count++;
            }
            //System.out.println("train:"+trainInstances.size());
            //System.out.println("test:"+testInstances.size());
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
			dt.setPrecision(accuracy);
			context.write(new Text("rf"), new Text(dt.toJSON().toString()));
        }
    }

    public static class AggregateReducer extends Reducer<Text, Text, Text, Text>
    {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
        {	
        	JSONArray rfJSON = new JSONArray();
            for (Text val : values){
            	rfJSON.add(val.toString());
            }
            context.write(key, new Text(rfJSON.toString()));
        }
    }
    
    public int run(String[] args) throws Exception
    {
        Job job = new Job(getConf(), "mr_randomforest");
        job.setJarByClass(MRRandomForest.class);
        job.setMapperClass(GrowTreeMapper.class);
        job.setReducerClass(AggregateReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH_PREFIX));


        job.setInputFormatClass(ColumnFamilyInputFormat.class);


        ConfigHelper.setInputRpcPort(job.getConfiguration(), "9160");
        ConfigHelper.setInputInitialAddress(job.getConfiguration(), "localhost");
        ConfigHelper.setInputPartitioner(job.getConfiguration(), "org.apache.cassandra.dht.RandomPartitioner");
        ConfigHelper.setInputColumnFamily(job.getConfiguration(), KEYSPACE, COLUMN_FAMILY);
        ConfigHelper.setRangeBatchSize(getConf(), 10000);
        SlicePredicate predicate = new SlicePredicate().setSlice_range(
                                                                        new SliceRange().
                                                                        setStart(ByteBufferUtil.EMPTY_BYTE_BUFFER).
                                                                        setFinish(ByteBufferUtil.EMPTY_BYTE_BUFFER).
                                                                        setCount(10000));
        ConfigHelper.setInputSlicePredicate(job.getConfiguration(), predicate);

        job.waitForCompletion(true);
        return 0;
    }
}
