import java.util.*;

class Word implements Comparable {

    public String value;
    public int count;
    public Map<String, Int> n_k; //Counts the number of times this word is found in each Text_j
    public Map<String, Double> p_w_v;



    public Word(String v) {
        n_k = new HashMap<String, Int>();
        p_w_v = new HashMap<String, Double>();
        value = v;
        count = 1;
    }

    public Word(String v, int c) {
	        value = v;
	        count = c;
    }

    public void increment(){
		count++;

	}

	public void increment(String targetKey){
			if(n_k.containsKey(targetKey)){
				n_k.get(targetKey).increment();

			} else {
				n_k.put(targetKey, new Int(1));
			}
	}

	public void calculateP(int n, int vocabSize, String target){
		int nk;
		double pkv;

		if(n_k.containsKey(target)){
			nk = n_k.get(target).value;
		} else{
			nk = 0;
		}

		pkv = ((double)nk +  1.0) / (double)(n + vocabSize);

		if(pkv == 0){
			throw new Error("Can't be set to 0");
		}

		p_w_v.put(target, pkv);


	}

	public double getP(String targetKey){
		if(p_w_v.containsKey(targetKey)){

			double pwv = p_w_v.get(targetKey);
			if(pwv == 0){
							throw new Error("Can't be set to 0");
			}

			return pwv;

		} else {
			return 1.0;
		}
	}

	public String toString(){
		String out = "" + value + " count = " + count + " " + n_k.size() + "  " + p_w_v.size();
		out = out + "\n";

		for(String keya :n_k.keySet()){
			out = out + "    n_k(" + keya +") = " + n_k.get(keya) + "\n";
		}

		out = out + "\n";

		for(String keyb :p_w_v.keySet()){
					out = out + "    p_w_k(" + keyb +") = " + p_w_v.get(keyb) + "\n";
		}


		return out;
	}

	 public int compareTo(Object thatObject) {


	    if(!(thatObject instanceof Word))
	    	throw new ClassCastException();

	    Word w = (Word)thatObject;


		if(this.count < w.count)
			return -1;
		if(this.count > w.count)
			return 1;

	    return 0;
  	}

  	public boolean equals(Object thatObject) {

	    if(thatObject instanceof Word){
			Word w = (Word)thatObject;
			return this.value.equals(w.value);
		} else if (thatObject instanceof String){
			String s = (String)thatObject;
			return s.equals(this.value);
		} else {
			return false;
		}

	 }

	  /**
	   * Return a hash code value for this object using the algorithm from Bloch:
	   * fields are added in the following order: title, year, director.
	   */
  public int hashCode() {
	    // TODO //DONE
	    int result = 17;
	    result = 37 * result + this.value.hashCode();



	    return result;
  }
}
