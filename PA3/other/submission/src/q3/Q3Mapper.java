package q3;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper
 */
public class Q3Mapper extends Mapper<LongWritable, Text, IntWritable, FloatWritable> {
	
	@Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	if (key.get() != 0 || !(value.toString().contains("analysis_sample_rate"))) { // skip the header
    		// find out the tempo for this song
	    	String line = value.toString();
	    	try {
	    		String[] fields = line.split("\t");
	    		if (fields.length != 54)
	    			return;
	    		
	    		float dancerbility = Float.parseFloat(fields[21]);
	    		
		        // emit dancerbility.
		        context.write(new IntWritable(1), new FloatWritable(dancerbility));
	    	} catch (NumberFormatException e) {
	    		
	    	}
    	}
    }
}
