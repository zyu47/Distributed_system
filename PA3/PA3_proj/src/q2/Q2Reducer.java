package q2;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reducer: Input to the reducer is the output from the mapper. It receives word, list<count> pairs.
 * Sums up individual counts per given word. Emits <word, total count> pairs.
 */
public class Q2Reducer extends Reducer<IntWritable, CountAverageTuple, IntWritable, CountAverageTuple> {
    @Override
    protected void reduce(IntWritable key, Iterable<CountAverageTuple> values, Context context) throws IOException, InterruptedException {
        float sum = 0;
        float count = 0;
        
        // Iterate through all input values for this key
        for(CountAverageTuple val : values){
        	sum += val.getCount() * val.getAverage() * 1.0;
            count += val.getCount();
        }
        CountAverageTuple res = new CountAverageTuple();
        res.setCount(count);
        res.setAverage(sum/count);
        context.write(key, res);
    }
}
