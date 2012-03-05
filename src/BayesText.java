import java.util.*;
import java.io.*;


public final class BayesText {


  public static void main(String... args) throws FileNotFoundException {

	///////////////////////////////////////////////
	//  Read example and vaildation document files
	//////////////////////////////////////////////

   List<File> exampleFiles = new ArrayList<File>();
   List<File> validationFiles = new ArrayList<File>();

   String filename;
   if(args.length == 2){
	   //Get file names from seperate example and vaidation directories.

	   	   File exampleDirectory= new File(args[0]);
	   	   File validationDirectory= new File(args[1]);

	   	   List<File> files = BayesText.getFileListing(exampleDirectory);

	   	   for(int i = 0; i < files.size(); i++){
					exampleFiles.add(files.get(i));
		   }

		   files = BayesText.getFileListing(validationDirectory);

		   for(int i = 0; i < files.size(); i++){
		   			validationFiles.add(files.get(i));
		   }

   } else if(args.length == 1){
   	   //Split files into 2/3 examples and 1/3 validation set

	   File startingDirectory= new File(args[0]);

	   List<File> files = BayesText.getFileListing(startingDirectory);

	   File file2;

	   for(int i = 0; i < files.size(); i++){
						file2 = files.get(i);

						if((i + 1) % 3 == 0){
								validationFiles.add(file2);
						} else {
								exampleFiles.add(file2);
						}

		}

	} else {
		System.out.println("\nAn incorrect number of arguments were set.");
		System.exit(0);
	}


	System.out.println("\n\nExample Set Size = " + exampleFiles.size());
	System.out.println("Validation Set Size = " + validationFiles.size());

    Map<String, Word> vocabulary = new HashMap<String, Word>();
    Map<String, TargetValue> docs = new HashMap<String, TargetValue>();

    String line = new String();
    int count = 0;

    /////////////////////////////////////////
    // Define docs, i.e  Target Values
    ////////////////////////////////////////////
	System.out.println("\nDefining target values...\n");
	File file3;
	String directory;

	for(int i = 0; i < exampleFiles.size(); i++){
				file3 = exampleFiles.get(i);

				directory = exampleFiles.get(i).getParentFile().getName();

				if(!docs.containsKey(directory)){
					System.out.println("\ttarget = " + directory);
					docs.put(directory, new TargetValue(directory));
				}

				docs.get(directory).add(file3);

	}

	int numTargetvalues = docs.size();


	//////////////////////////////////////
	//Build Vocabulary
	//////////////////////////////////////
	System.out.println("\nBuilding vocabulary...\n");

	try{
	try{

	String token;


    for(File file : exampleFiles ){

      	FileReader fr = new FileReader(file);
		BufferedReader bIn = new BufferedReader(fr);

		count++;
		int wordCount = 0;

		while((line = bIn.readLine()) != null){
				line = line.trim().toLowerCase(); //.replaceAll("[^A-Za-z]", "");

				StringTokenizer st = new StringTokenizer(line, " ");

				if(st.countTokens()  >  0){

					while(st.hasMoreTokens()){
							token = st.nextToken().replaceAll("[^A-Za-z]", ""); //.replaceAll("^[^A-Za-z]|[^A-Za-z]$", "");

							wordCount++;

							if(vocabulary.containsKey(token)){
								vocabulary.get(token).increment();

							} else {
								vocabulary.put(token, new Word(token));

							}

							//Count word per target value, which in this case is the file's parent directory path.
							vocabulary.get(token).increment(file.getParentFile().getName());
						//}
					}


				}
		}


		docs.get(file.getParentFile().getName()).incrementN(wordCount);

		if((count >= 20 && count % (exampleFiles.size() / 20) == 0) || exampleFiles.size() < 20){
					System.out.println("\t" + count + " of " + exampleFiles.size()  + " documents read.    " + vocabulary.size() + " unique words found ");
		}


		bIn.close();
		fr.close();


    }

    } catch (FileNotFoundException nf){
		System.out.println("Data File not found."); System.exit(0);
	}
	} catch (IOException i){
			System.out.println("Error with Data File.");
			System.out.println(" "+ i.getMessage());
			System.exit(0);

	}



	//Remove unhelpful words.
	if(vocabulary.size() > 1000){
		//Remove words that occured less than 3 times
		int  numMaxWords = 100;

		System.out.println("\nRemoving " + numMaxWords + " most frequent words and words that occur less than 3 times... ");

		String key;

		Set<String> keys = vocabulary.keySet();
		Iterator it = keys.iterator();

		List<Word> words = new ArrayList<Word>();

		int k;

		while(it.hasNext()){
			key = (String)it.next();
			k = vocabulary.get(key).count;

			if(k < 3){
						it.remove();
			}

			words.add(new Word(key, k));

		}


		//Remove 100 most frequent words

		if(numMaxWords < words.size()){
			Collections.sort(words);  //Java does not allow good type checking here. :(
			for(int i = words.size() - 1 ; i > (words.size() - 1 - numMaxWords); i--){
				key = words.get(i).value;
				if(key != null){

					vocabulary.remove(key);
				}
			}
		}
	}



	System.out.println();
	System.out.println("Final vocabulary size = " + vocabulary.size());
	System.out.println();



	///////////////////////////////////////////////////////////////
	// 2.Calculate the required p(vj) and p(pwk|vj) probablity terms
	////////////////////////////////////////////////////////////////



	System.out.println("\nCalculating  p(v_j) and p(w_k|v_j) probablity terms...\n");

	//2. determine P(v_j) values
	//3 Determine total numberof distinct word posistions in Text_j
			//	Text_j is a single documenet created by concatenating all documents belonging to one target value.

	TargetValue t;

	for(String target : docs.keySet()){
		t = docs.get(target);
		t.setPV(exampleFiles.size());

		for(String word : vocabulary.keySet()){

				vocabulary.get(word).calculateP(docs.get(target).getN(), vocabulary.size(), target);
		}

	}


	///////////////////////////////////////////////////////////////
	// Classify
	////////////////////////////////////////////////////////////////

	System.out.println("\nClassifying validation set...\n");
	int correct = 0;


	try{
	try{


	Date d = new Date();
	String tempstring = d.toString().replaceAll("\\s", "_").replaceAll("\\:", "");
	String outfile = "bayestext_out_" + tempstring + ".txt";
	FileWriter fw = new FileWriter(outfile);
	BufferedWriter bw = new BufferedWriter(fw);
	bw.write("Document_Path\tClassification\tCorrect");
	bw.newLine();
	bw.newLine();




	count = 0;
	HashMap<String, Double> argmaxProducts;

	String token;

	for(File file : validationFiles){//1



		argmaxProducts = new HashMap<String, Double>();

		FileReader fr = new FileReader(file);
		BufferedReader bIn = new BufferedReader(fr);

		String classification = null;


		double product;

		while((line = bIn.readLine()) != null){
				line = line.trim().toLowerCase(); //.replaceAll("[^A-Za-z]", "");

				StringTokenizer st = new StringTokenizer(line, " ");

				if(st.countTokens()  >  0){

					while(st.hasMoreTokens()){
						token = st.nextToken().replaceAll("[^A-Za-z]", "");//.replaceAll("^[^A-Za-z]|[^A-Za-z]$", ""); //token = a_i;

						if(vocabulary.containsKey(token)){

							for(String target : docs.keySet()){
									if(!argmaxProducts.containsKey(target)){
										argmaxProducts.put(target, 1.0);
									}

									if(argmaxProducts.containsKey(target)){

										product = argmaxProducts.get(target) * -1.0/Math.log10(vocabulary.get(token).getP(target));//Order of magnitde sholdwork fine.
										//product = argmaxProducts.get(target) * vocabulary.get(token).getP(target);

										argmaxProducts.put(target, product);
									}

							}

						}

					}
			}
		}

		bIn.close();
		fr.close();



		//Determine argmax for file amoung all traget values.
		double v_bn;
		double v_max = 0.0;


		for(String target : argmaxProducts.keySet()){
			product = argmaxProducts.get(target);

			v_bn = docs.get(target).getPV() * product;

			if(v_bn >= v_max && product < 1.0){
						v_max = v_bn;
						classification = target;
			}
		}

		bw.write(file + "\t" + classification);



		count++;
		if((count >= 20 && count % (validationFiles.size() / 20) == 0) || validationFiles.size() < 20){
							System.out.println("\t" + count + " of " + validationFiles.size() + " documents classified."); // + " class = " + classification +" v_nb = "+ -1.0/Math.log10(v_max));
		}


		//System.out.println(file.getParentFile().getName() + " " + classification);
		if(!docs.containsKey(file.getParentFile().getName())){

			bw.write("\tn/a");
		} else if(file.getParentFile().getName().equals(classification)){
					bw.write("\tT");
					//The document could be correct and not counted as correct if it's parent direcotory is generic.
					//That is, if the user dosen't specify target values for the validation set then we can't know if it's correct.
					correct++;

		} else {
			bw.write("\tF");
		}



		bw.newLine();


	}//1

	bw.close();
	fw.close();

	} catch (FileNotFoundException nf){
				System.out.println("Data File not found."); System.exit(0);
	}

	} catch (IOException i){
			System.out.println("Error with Data File.");
			System.out.println(" "+ i.getMessage());
			System.exit(0);

	}



	double accuracy = (double)correct/(double)validationFiles.size();
	System.out.println("");
	System.out.println("Number correctly classified = " + correct);
	System.out.println("Accuracy = " + (double)((int)(accuracy * 1000.00))/10.0 + "%");

  }


  static public List<File> getFileListing(File aStartingDir) throws FileNotFoundException {
    validateDirectory(aStartingDir);
    List<File> result = getFileListingNoSort(aStartingDir);

    return result;
  }


  static private List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
    List<File> result = new ArrayList<File>();
    File[] filesAndDirs = aStartingDir.listFiles();
    List<File> filesDirs = Arrays.asList(filesAndDirs);
    for(File file : filesDirs) {
      if(file.isFile()){
      	result.add(file);
  	  } else {
        //must be a directory
        //recursive call
        List<File> deeperList = getFileListingNoSort(file);
        result.addAll(deeperList);
      }
    }
    return result;
  }




  // Directory is valid if it exists, does not represent a file, and can be read.

  static private void validateDirectory (File aDirectory) throws FileNotFoundException {
    if (aDirectory == null) {
      throw new IllegalArgumentException("Directory should not be null.");
    }
    if (!aDirectory.exists()) {
      throw new FileNotFoundException("Directory does not exist: " + aDirectory);
    }
    if (!aDirectory.isDirectory()) {
      throw new IllegalArgumentException("Is not a directory: " + aDirectory);
    }
    if (!aDirectory.canRead()) {
      throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
    }
  }



}