package q4;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class TempoArtist implements Writable{
	private float tempo = 0;
	private String name;
	
	public TempoArtist (float t, String n) {
		tempo = t;
		name = n;
	}
	
	public TempoArtist () {
		tempo = 0;
		name = "";
	}
	
	public void readFields(DataInput in) throws IOException {
		
		tempo = in.readFloat();
		name = in.readLine();
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(tempo);
		out.writeBytes(name + "\n");;
	}

	public float getTempo() {
		return tempo;
	}

	public void setTemop(float tempo) {
		this.tempo = tempo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = new String(name);
	}

	@Override
	public String toString() {
		return tempo + "\t" + name;
	}

}
