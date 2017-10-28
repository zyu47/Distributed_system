package q4;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
//import java.util.*;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q4Reducer extends Reducer<IntWritable, TempoArtist, NullWritable, Text> {
    @Override
    protected void reduce(IntWritable key, Iterable<TempoArtist> values, Context context) throws IOException, InterruptedException {
    	
    	TempoArtist[] top = new TempoArtist[10];
    	for (int i = 0; i != top.length; ++i)
    	{
    		top[i] = new TempoArtist();
    	}
    	int minInd = 0;
    	float minTempo = 0;
        // Iterate through all input values for this key
    	for(TempoArtist v : values){
    		if(v.getTempo() > minTempo) {
    			top[minInd].setName(v.getName());
    			top[minInd].setTemop(v.getTempo());
    			minInd = findMin(top);
    			minTempo = top[minInd].getTempo();
    		}
    	}
    	String res = "";
    	for (int i =0; i != top.length; ++i) {
    		res += (top[i].toString() + "\n");
    	}
        context.write(NullWritable.get(), new Text(res));
    }
    public int findMin (TempoArtist[] tempoList)
    {
    	float minTempo = tempoList[0].getTempo();
    	int minInd = 0;
    	for (int i = 0; i != tempoList.length; ++i)
    	{
    		if (tempoList[i].getTempo() < minTempo) {
    			minInd = i;
    			minTempo = tempoList[i].getTempo();
    		}
    	}
    	return minInd;
    }
}
