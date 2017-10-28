package q1;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q1Reducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        // Count for each genre
    	Map<String, Long> tagsCnt = new HashMap<String, Long>(); 
        
        // Iterate through all input values for this key
        for(Text v : values){
        	if (tagsCnt.containsKey(v.toString())) {
        		tagsCnt.put(v.toString(), tagsCnt.get(v.toString()) + 1);
        	} else {
        		tagsCnt.put(v.toString(), new Long(1));
        	}
        }
        
        Map.Entry<String, Long> maxEntry = null;
        for (Map.Entry<String, Long> entry : tagsCnt.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        context.write(key, new Text(maxEntry.getKey()));
    }
}
