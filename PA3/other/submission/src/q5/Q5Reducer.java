package q5;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q5Reducer extends Reducer<Text, InfoTuple, NullWritable, Text> {
    @Override
    protected void reduce(Text key, Iterable<InfoTuple> values, Context context) throws IOException, InterruptedException {
        InfoTuple[] top = new InfoTuple[10];
        for (int i = 0; i != top.length; ++i) {
        	top[i] = new InfoTuple();
        }
        int minInd = 0; // used for updating top InfoTuple
        float minHot = 0; // used for updating top InfoTuple
        
        // Iterate through all input values for this key
        for(InfoTuple val : values){
        	if (val.getHotness() > minHot) {
        		top[minInd].copy(val);
        		minInd = findMin(top);
        		minHot = top[minInd].getHotness();
        	}
        }

		String res = "";
		for (int i =0; i != top.length; ++i) {
			res += (key + "\t" + top[i].toString() + "\n");
		}
		context.write(NullWritable.get(), new Text(res));
    }
    
    public int findMin (InfoTuple[] infoList)
    {
	    float minHot = 1000;
	    int minInd = 0;
	    for (int i = 0; i != infoList.length; ++i)
	    {
	    	if (infoList[i].getHotness() < minHot) {
	    		minInd = i;
	    		minHot = infoList[i].getHotness();
	    	}
	    }
	    return minInd;
    }
}


