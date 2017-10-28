package q8;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper
 */
public class Q8Mapper extends Mapper<LongWritable, Text, Text, Text> {
	
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	if (key.get() != 0 || !(value.toString().contains("analysis_sample_rate"))) { // skip the header
    		// find out the tempo for this song
	    	String line = value.toString();
	    	try {
	    		String[] fields = line.split("\t");
	    		if (fields.length != 54)
	    			return;
	    		
	    		// Find out the genre
	    		String terms = fields[13];
	    		String[] termsList = null;
	    		if (terms.length() <= 8) {
	    			return;
	    		} else {
	    			terms = terms.substring(4, terms.length()-4);
	    			termsList = terms.split("\"\", \"\"");
	    		}
	    		
	    		String freqs = fields[14];
	    		String[] freqListString = null;
	    		float maxFreq = 0;
	    		if (freqs.length() <= 2) {
	    			return;
	    		} else {
	    			freqs = freqs.substring(1, freqs.length()-1);
	    			freqListString = freqs.split(", ");
	    			
	    			//Check if the terms length and freq length match
	    			if (termsList.length != freqListString.length || freqListString.length == 0) {
	    				return;
	    			}	    			
	    			
	    			//Find out the index for maximum frequency
	    			maxFreq = Float.parseFloat(freqListString[0]);
	    			for (int i = 1; i != freqListString.length; ++i) {
	    				float freqTmp = Float.parseFloat(freqListString[i]);
	    				if (freqTmp > maxFreq) {
	    					maxFreq = freqTmp;
	    				}
	    			}
	    			
	    			// If there are multiple terms with same freq, output all of them
	    			for (int i = 0; i != freqListString.length; ++i) {
	    				if (maxFreq == Float.parseFloat(freqListString[i])) {
	    			        context.write(new Text("0"), new Text(termsList[i]));	    					
	    				}
	    			}
	    		}
		        
	    	} catch (NumberFormatException e) {
	    		
	    	}
    	}
    }
}
