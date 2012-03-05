import java.io.*;
import java.util.*;

class TargetValue {

    private ArrayList<File> files;
    private double p_v;
	private int N;
	private String label;

    public TargetValue(String l) {
       label = l;
       files = new ArrayList<File>();
       p_v = 0.0;
       N = 0;
    }

    public void add(File f){
		if(!files.contains(f)){
			files.add(f);
		}
	}

	public double setPV(int numExamples){
		//List<File> list;
		p_v =  ((double)files.size()/(double)numExamples);

		return p_v;


	}

	public double getPV(){
		return p_v;
	}

	public void incrementN(int n){
		N += n;

	}

	public int getN(){
		return N;
	}

	public String toString(){
		return label + "  N = " + N + " p(vj) =  " + p_v + "  files = " + files.toString() + " files.size() = " + files.size() ;

	}




}
