package q7;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper
 */
public class Q7Mapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	
	@Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	if (key.get() != 0 || !(value.toString().contains("analysis_sample_rate"))) { // skip the header
    		// find out the tempo for this song
	    	String line = value.toString();
	    	try {
	    		String[] fields = line.split("\t");
	    		if (fields.length != 54)
	    			return;
	    		
//	    		String artistID = fields[4];
	    				        
		        context.write(new Text(fields[11]), new IntWritable(1));
	    	} catch (NumberFormatException e) {
	    		
	    	}
    	}
    }
}
