package q2;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper
 */
public class Q2Mapper extends Mapper<LongWritable, Text, IntWritable, CountAverageTuple> {
	private CountAverageTuple outCountAverage = new CountAverageTuple();
	
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	if (key.get() != 0 || !(value.toString().contains("analysis_sample_rate"))) { // skip the header
    		// find out the tempo for this song
	    	String line = value.toString();
	    	try {
	    		String[] fields = line.split("\t");
	    		if (fields.length != 54)
	    			return;
	    		
	    		float tempo = Float.parseFloat(fields[47]);
	    		
		        // emit average of tempo and the count.
		    	outCountAverage.setCount(1);
		    	outCountAverage.setAverage(tempo);
		        context.write(new IntWritable(1), outCountAverage);
	    	} catch (NumberFormatException e) {
	    		
	    	}
    	}
    }
}
