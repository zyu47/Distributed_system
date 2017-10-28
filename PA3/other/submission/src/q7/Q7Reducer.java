package q7;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q7Reducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
    	    	
        // Iterate through all input values for this key
    	int count = 0;
    	for(IntWritable v : values){
    		count += v.get();
    	}
    	
        context.write(key, new IntWritable(count));
    }
}
