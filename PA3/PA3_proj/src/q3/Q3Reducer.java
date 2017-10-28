package q3;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q3Reducer extends Reducer<IntWritable, FloatWritable, IntWritable, FloatWritable> {
    @Override
    protected void reduce(IntWritable key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {
    	    	
        // Iterate through all input values for this key
    	List<Float> sval = new ArrayList<Float>();
    	for(FloatWritable v : values){
    		sval.add(v.get());
    	}
    	// Sort the values
    	Collections.sort(sval);
    	// find median
    	float median = sval.get(sval.size()/2).floatValue() + sval.get((sval.size()-1)/2).floatValue();
    	
        context.write(key, new FloatWritable(median/2));
    }
}
