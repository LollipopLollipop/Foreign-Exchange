

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dingz
 * MRRandomForestSetup
 * 	Setup the Cassandra keyspace and columnfamily for future training
 * 	Take in user input: N
 * 
 * 
 */

public class MRRandomForestSetup
{
    private static final Logger logger = LoggerFactory.getLogger(MRRandomForestSetup.class);

    public static final int TEST_COUNT = 4;
    public static final int TRAINING_COUNT = 10000;
    public static int N = 0;

    public static void main(String[] args) throws Exception
    {
    	ArrayList<CandleStick> totTraining = new ArrayList<CandleStick>();
    	N = Integer.parseInt(args[0]);
		try {
			//use buffered reader and writer for fast IO
			BufferedReader br = new BufferedReader(new FileReader("Preped-USDJPY-2015-08.csv"));
			String thisLine = null;
			int count = 0;
			while ((thisLine = br.readLine()) != null && count<TRAINING_COUNT) {
				StringTokenizer featureTokenizer = new StringTokenizer(thisLine, ",");
				String[] featuresNLabel = new String[6];
				int i=0;
				while(featureTokenizer.hasMoreTokens()){
					featuresNLabel[i++] = featureTokenizer.nextToken();
				}
				//wrap each record into CandleStick obj
				CandleStick cs = new CandleStick(featuresNLabel);
				totTraining.add(cs);
				count ++;
			}
			br.close();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    	
    	Cassandra.Iface client = createConnection();

        setupKeyspace(client);

        client.set_keyspace(MRRandomForest.KEYSPACE);
        

        for(int i=0; i<N; i++){
			Collections.shuffle(totTraining);
        	ByteBuffer key = ByteBufferUtil.bytes("key-if-verse"+i);
            final ColumnParent colParent = new ColumnParent(MRRandomForest.COLUMN_FAMILY);
            for (CandleStick cs : totTraining)
            {
                client.add(key,
                           colParent,
                           new CounterColumn(ByteBufferUtil.bytes(cs.toString()),
                           0),
                           ConsistencyLevel.ONE );
            }
            logger.info("added key-if-verse"+i);

        }        

        
        System.exit(0);
    }

    private static Map<ByteBuffer,Map<String,List<Mutation>>> getMutationMap(ByteBuffer key, String cf, Column c)
    {
        Map<ByteBuffer,Map<String,List<Mutation>>> mutationMap = new HashMap<ByteBuffer,Map<String,List<Mutation>>>();
        addToMutationMap(mutationMap, key, cf, c);
        return mutationMap;
    }

    private static void addToMutationMap(Map<ByteBuffer,Map<String,List<Mutation>>> mutationMap, ByteBuffer key, String cf, Column c)
    {
        Map<String,List<Mutation>> cfMutation = new HashMap<String,List<Mutation>>();
        List<Mutation> mList = new ArrayList<Mutation>();
        ColumnOrSuperColumn cc = new ColumnOrSuperColumn();
        Mutation m = new Mutation();

        cc.setColumn(c);
        m.setColumn_or_supercolumn(cc);
        mList.add(m);
        cfMutation.put(cf, mList);
        mutationMap.put(key, cfMutation);
    }

    private static void setupKeyspace(Cassandra.Iface client) throws TException, InvalidRequestException, SchemaDisagreementException {
        List<CfDef> cfDefList = new ArrayList<CfDef>();

        CfDef input = new CfDef(MRRandomForest.KEYSPACE, MRRandomForest.COLUMN_FAMILY);
        input.setComparator_type("UTF8Type");
        input.setDefault_validation_class("CounterColumnType");
        cfDefList.add(input);

        KsDef ksDef = new KsDef(MRRandomForest.KEYSPACE, "org.apache.cassandra.locator.SimpleStrategy", cfDefList);
        ksDef.putToStrategy_options("replication_factor", "1");
        client.system_add_keyspace(ksDef);
        int magnitude = client.describe_ring(MRRandomForest.KEYSPACE).size();
        try
        {
            Thread.sleep(1000 * magnitude);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Cassandra.Iface createConnection() throws TTransportException
    {
        if (System.getProperty("cassandra.host") == null || System.getProperty("cassandra.port") == null)
        {
           logger.warn("cassandra.host or cassandra.port is not defined, using default");
        }
        return createConnection( System.getProperty("cassandra.host","localhost"),
                                 Integer.valueOf(System.getProperty("cassandra.port","9160")),
                                 Boolean.valueOf(System.getProperty("cassandra.framed", "true")) );
    }

    private static Cassandra.Client createConnection(String host, Integer port, boolean framed) throws TTransportException
    {
        TSocket socket = new TSocket(host, port);
        TTransport trans = framed ? new TFramedTransport(socket) : socket;
        trans.open();
        TProtocol protocol = new TBinaryProtocol(trans);

        return new Cassandra.Client(protocol);
    }

}
