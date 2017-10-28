package q5;

import java.io.*;
import org.apache.hadoop.io.Writable;

public class InfoTuple implements Writable{
	private float hot = 0;
	private String name;
	private String title;

	public void readFields(DataInput in) throws IOException {
		hot = in.readFloat();
		name = in.readLine();
		title = in.readLine();
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(hot);
		out.writeBytes(name + "\n");
		out.writeBytes(title + "\n");		
	}

	public void copy(InfoTuple it) {
		this.hot = it.hot;
		this.name = new String(it.name);
		this.title = new String(it.title);
	}
	
	public float getHotness() {
		return hot;
	}

	public void setHotness(float hot) {
		this.hot = hot;
	}

	public String getArtistName() {
		return name;
	}
	
	public void setArtistName(String name) {
		this.name = name;
	}
	
	public String getSongTitle() {
		return title;
	}

	public void setSongTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return hot + "\t" + name + "\t" + title;
	}

}
