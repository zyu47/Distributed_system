package q4;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper
 */
public class Q4Mapper extends Mapper<LongWritable, Text, IntWritable, TempoArtist> {
	
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
	    		String artist = fields[11];
		        
		        context.write(new IntWritable(1), new TempoArtist(tempo, artist));
	    	} catch (NumberFormatException e) {
	    		
	    	}
    	}
    }
}
