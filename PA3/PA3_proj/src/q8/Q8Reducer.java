package q8;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q8Reducer extends Reducer<Text, Text, NullWritable, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        Map<String, Long> tagsCnt = new HashMap<String, Long>();
        
        // Iterate through all input values for this key
        for(Text v : values){
        	if (tagsCnt.containsKey(v.toString())) {
        		tagsCnt.put(v.toString(), tagsCnt.get(v.toString()) + 1);
        	} else {
        		tagsCnt.put(v.toString(), new Long(1));
        	}
        }
        
        String[] topText = new String[10];
        long[] topLong = new long[10];
        
        int minInd = 0;
        long minCnt = 0;

        for (Map.Entry<String, Long> entry : tagsCnt.entrySet())
        {
        	if (entry.getValue() > minCnt) {
        		topText[minInd] = entry.getKey();
        		topLong[minInd] = entry.getValue();
        		minInd = findMin(topLong);
        	}            
        }
        String res = "";
        for (int i = 0; i != topLong.length; ++i) {
        	res += (Float.toString(topLong[i]) + "\t" + topText[i] + "\n");
        }
        context.write(NullWritable.get(), new Text(res));
    }
    public int findMin (long[] cnt)
    {
    	long minCnt = cnt[0];
    	int minInd = 0;
    	for (int i = 1; i != cnt.length; ++i)
    	{
    		if (cnt[i] < minCnt) {
    			minInd = i;
    			minCnt = cnt[i];
    		}
    	}
    	return minInd;
    }
}
