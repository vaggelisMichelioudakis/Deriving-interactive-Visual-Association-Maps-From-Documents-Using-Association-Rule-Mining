package org.example.Taxonomy;

//import mitos.stemmer.Stemmer;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Index {

    private Map<String, Word> terms;
    private int phrase_length = 0;
    private double THRESHOLD = 0;
    private List<HashSet<String>> collections;
    private static HashSet<String> tokens;

    // This will hold the results from our Eclat algorithm
    private Map<String, Word> finalFrequentItemsets;
    private double absoluteMinSupport;

    private Map<String, Word> topTerms;
    private static POSCheck posCheck;

    public static String tokenize(String word) {
        // Define a regex pattern to match all unwanted characters
        String regex = "[^a-zA-Z0-9]";
        word = word.replaceAll(regex, "");
        if (word.isEmpty()) {
            return "A";
        }

        return word.toLowerCase();
    }

    public double getAvgSupport(){

        int sum = 0;
        for(Map.Entry<String, Word> entry : terms.entrySet()){
            sum += entry.getValue().calculateSupport();
        }
        double result = (double)sum / terms.size();
        return result;

    }

    public void setTHRESHOLD(double th, int phrase_length){
        this.THRESHOLD = th;
        this.phrase_length = phrase_length;
    }

    public Index (){

        collections = new ArrayList<>();

        HashSet<String> stp = new HashSet<>();
        terms = new HashMap<>();

        //File stopWords = new File("AssociationMaps/StopWords");
        //stemmer.Initialize();

        String resourcePath = "config/StopWords";

        this.posCheck = new POSCheck();

        try (
                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ){
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineWords = line.split("\\s+"); // split line by whitespace
                for (String word : lineWords) {
                    stp.add(tokenize(word));
                }
            }
        }catch(IOException | NullPointerException e) {
            // Handle the error more explicitly!
            System.err.println("!!!!!!!!!!!!!! FAILED TO LOAD STOPWORDS !!!!!!!!!!!!!!");
            System.err.println("Could not find or read the file at resource path: " + resourcePath);
            e.printStackTrace();
        }
        tokens = stp;

    }

    public List<HashSet<String>> getCollections (){
        return collections;
    }

    public void addCollection(String[] newCollection, int id, int phrase_length){
        HashSet<String> tmp = new HashSet<>();
        this.phrase_length = phrase_length;
        if(phrase_length == 0 || phrase_length == 1){

            for(String word: newCollection){
                //String string = tokenize(stemmer.Stem(word));
               String string = tokenize(word);

                // we can add the position check here if its needed eg.:
                // posCheck.isAdjective(string) posCheck.isNoun(string) etc.
                // if(!string.endsWith("A") && !tokens.contains(string) && posCheck.isNoun(string) ){
                if(!string.endsWith("A") && !tokens.contains(string) && !posCheck.isNumber(string) ){
                    //String x = stemmer.Stem(string);
                    String x = (string);
                    tmp.add(x);

                    if(!terms.containsKey(x)){
                        List<String> temp = new ArrayList<>();
                        temp.add(x);
                        Word term = new Word(temp);
                        term.addFrequency(id);
                        this.terms.put(x, term);
                    }else{
                        terms.get(x).addFrequency(id);
                    }


                }
            }


            collections.add(tmp);
        }else{

            for(int i =0; i < newCollection.length; i ++){
                String word = new String("");
                int k = 0;
                for(int j = 0; j < phrase_length ; j++){
                    if(j+i+k < newCollection.length) {
                        String string = tokenize((newCollection[j + i + k]));

                        // we can add the position check here if its needed eg.:
                        // posCheck.isAdjective(string) posCheck.isNoun(string) etc.
                        if (!string.endsWith("A") && !tokens.contains(string)  ) {
                            String x = (string);
                            word += x + " ";
                        } else {
                            k++;
                            j--;

                        }
                    }

                }
                tmp.add(word);
                if(!terms.containsKey(word)){
                    List<String> temp = new ArrayList<>();
                    temp.add(word);
                    Word term = new Word(temp);
                    term.addFrequency(id);
                    this.terms.put(word, term);
                }else{
                    terms.get(word).addFrequency(id);
                }
            }
            collections.add(tmp);
        }
    }

    public void calculateTf_ID(){
        for( Map.Entry<String, Word> entry : terms.entrySet()){
            double tf = 0 ;
            double idf = 0;
            for(Map.Entry<Integer, Integer> frequencies : entry.getValue().getFrequencies().entrySet()){
                tf += frequencies.getValue();
            }
            int N = collections.size();
            int DF = entry.getValue().getFrequencies().size();

            if (DF == 0) continue;

            idf = Math.log((double) N / DF);
            double TF_IDF = tf * idf;
            entry.getValue().setTf_idf(TF_IDF);
        }
        topTerms = new HashMap<>();
        storeTopTerms(1000);
    }

    public void storeTopTerms(int topX) {
        // Sort terms by TF-IDF score in descending order
        List<Map.Entry<String, Word>> sortedTerms = terms.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().getTf_idf(), entry1.getValue().getTf_idf()))
                .toList();

        topTerms = new HashMap<>();
        if (sortedTerms.isEmpty()) {
            return;
        }

        // Add the most important term by default
        String firstTerm = sortedTerms.get(0).getKey();
        topTerms.put(firstTerm, sortedTerms.get(0).getValue());

        for (int i = 0; i < Math.min(topX, sortedTerms.size()); i++) {
            String term = sortedTerms.get(i).getKey();
            topTerms.put(term, sortedTerms.get(i).getValue());
            //System.out.println((i+1) + ". " + term + " (TF-IDF: " + sortedTerms.get(i).getValue().getTf_idf() + ")");
        }
    }


    public Map<String, Word> getTopTerms(){ return topTerms;}

    public Map<String, Word> getTerms (){
        return terms;
    }

    // Function to calculate factorial
    public static BigInteger factorial(int num) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= num; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    // Function to calculate C(n, N)
    public static BigInteger binomialCoefficient(int n, int N) {
        BigInteger numerator = factorial(N);
        BigInteger denominator = factorial(n).multiply(factorial(N - n));
        return numerator.divide(denominator);
    }

    // dynamic code to implement n choose r
    public static List<List<String>> getCombinations(ArrayList<String> arr, int r) {
        List<List<String>> result = new ArrayList<>();
        List<String> combination = new ArrayList<>();
        generateCombinations(arr, r, 0, combination, result);
        return result;
    }

    private static void generateCombinations(ArrayList<String> arr, int r, int index, List<String> combination, List<List<String>> result) {
        // If the current combination is of the required length, add it to the result list
        if (combination.size() == r) {
            result.add(new ArrayList<>(combination));
            return;
        }

        // Iterate through the list elements starting from 'index'
        for (int i = index; i < arr.size(); i++) {
            combination.add(arr.get(i));
            generateCombinations(arr, r, i + 1, combination, result);
            combination.remove(combination.size() - 1);
        }
    }

    // run aprioris algo to find the subests with the minimum support
    public Map<String, Word> Findsubsets(Map<String, Word> Subset, int depth,int total){
        Map<String, Word> newSet = new HashMap<>();
        if(THRESHOLD < 1){
            THRESHOLD = THRESHOLD * total;
        }

        // Eliminate all the x with support < threshold only on the first step
        if(depth == 2){
            boolean flag = false;
            for(Map.Entry<String, Word> entry : Subset.entrySet()){
                entry.getValue().calculateSupport();
                if(entry.getValue().getSupport() >=THRESHOLD && entry.getKey()!=null){
                    newSet.put(entry.getKey(),entry.getValue());
                    flag = true;
                }
            }
            if(!flag){
                return Subset;
            }
        }else{
            newSet = Subset;
        }

        // Create new x with one more element from the set before
        ArrayList<String> tmp = new ArrayList<>();
        for(Map.Entry<String, Word> entry: newSet.entrySet()){
            for(String str : entry.getValue().getValues()){
                if(!tmp.contains(str)){
                    tmp.add(str);
                }
            }
        }
        BigInteger result = binomialCoefficient(depth, tmp.size());
        if(result.compareTo(BigInteger.valueOf((long) 3E6)) > 0){
            System.out.println("TOO MANY SUBSETS STOPPING THE ALGORITHM");
            return Subset;
        }

        // time powerset
        System.out.print("Start timing powerset\n");
        long startTime = System.currentTimeMillis();
        List<List<String>> ret = getCombinations(tmp, depth);
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time for powerset: " + (long) (endTime - startTime)/ 1000);
        // Narrow down the x

        Map<String, Word> x = new HashMap<>();
        System.out.print("Start timming search\n");
        startTime = System.currentTimeMillis();
        for(List<String> list : ret){
            String key = "";
            for(String word : list){
                key += word;
            }

            int id = 0;
            for(HashSet<String> doc : collections) {

                boolean f = true;
                for(String word : list){
                    if(!doc.contains(word)){
                        f = false;
                        break;
                    }
                }

                if(f){
                    if(!x.containsKey(key)){
                        Word term = new Word(list);
                        term.addFrequency(id);
                        x.put(key, term);
                    }else{
                        x.get(key).addFrequency(id);
                    }
                }
                id ++;
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time for search: " + (long) (endTime - startTime)/ 1000);


        // check if the new subsets satisfy the support threshold
        x.entrySet().removeIf(entry -> entry.getValue().getTotalFreq() < THRESHOLD);

        if(x.isEmpty()){
            System.out.println("MAX DEPTH :" + depth);
            return Subset;

        }else{
            x = Findsubsets(x, depth + 1, total);
        }

        return x;
    }

    public int getSuppoort(List<String> list){
        int support = 0;

        for(HashSet<String> doc : collections){

            boolean flag = true;
            for(String term : list){
                if(!doc.contains(term)){
                    flag = false;
                    break;
                }
            }
            if(flag){
                support += 1;
            }

        }

        return support;
    }

    public double getConfidence(List<String> list, int sup){
        double confidence = 0;

        confidence = (double)  sup / getSuppoort(list) ;
        return confidence;
    }

    /**
     * Generates a canonical, sorted key from a list of terms.
     * e.g., ["B", "A"] -> "A B"
     */
    public String generateKey(List<String> items) {
        List<String> sortedItems = new ArrayList<>(items);
        Collections.sort(sortedItems);
        // Using a single space as a delimiter.
        return String.join(" ", sortedItems);
    }

    /**
     * Public entry point for the Eclat algorithm.
     *
     * @param initialTerms The map of top terms (e.g., getTopTerms())
     * @param relativeMinSupport The support threshold (e.g., 0.1)
     * @param totalDocuments The total number of documents (e.g., 'I')
     * @return A map of all frequent itemsets (key) and their Word objects.
     */
    public Map<String, Word> findFrequentItemsetsEclat(Map<String, Word> initialTerms, double relativeMinSupport, int totalDocuments, int maxDepth) {

        System.out.println("Starting Eclat algorithm...");
        this.finalFrequentItemsets = new HashMap<>();

        // 1. Convert relative support (0.1) to absolute support (e.g., 3)
        if (relativeMinSupport < 1) {
            this.absoluteMinSupport = relativeMinSupport * totalDocuments;
        } else {
            this.absoluteMinSupport = relativeMinSupport;
        }

        System.out.println("Absolute support threshold: " + this.absoluteMinSupport);
        System.out.println("Maximum phrase length is: " + maxDepth);

        // 2. Build the initial set of frequent 1-itemsets
        List<Map.Entry<String, Set<Integer>>> frequent1Items = new ArrayList<>();
        for (Map.Entry<String, Word> entry : initialTerms.entrySet()) {
            entry.getValue().calculateSupport(); // Ensure support is calculated

            if (entry.getValue().getSupport() >= this.absoluteMinSupport) {
                // Add to final results map
                this.finalFrequentItemsets.put(entry.getKey(), entry.getValue());

                // Add to our list for recursion
                frequent1Items.add(Map.entry(entry.getKey(), entry.getValue().getFrequencies().keySet()));
            }
        }

        // Sort lexicographically to optimize recursion
        frequent1Items.sort(Map.Entry.comparingByKey());

        System.out.println("Found " + frequent1Items.size() + " frequent 1-itemsets.");

        // 3. Start the recursive mining
        long startTime = System.currentTimeMillis();
        eclatRecursive(new ArrayList<>(), frequent1Items,maxDepth);
        long endTime = System.currentTimeMillis();
        System.out.println("Eclat mining finished in " + (endTime - startTime) + " ms.");

        return this.finalFrequentItemsets;
    }

    /**
     * The recursive Eclat mining function.
     *
     * @param prefix The itemset we are currently extending (e.g., ["A"])
     * @param candidates The list of items that can be added (e.g., [("B", tidsetB), ("C", tidsetC)])
     */
    private void eclatRecursive(List<String> prefix, List<Map.Entry<String, Set<Integer>>> candidates, int maxDepth) {

        for (int i = 0; i < candidates.size(); i++) {
            Map.Entry<String, Set<Integer>> entryA = candidates.get(i);
            String itemA = entryA.getKey();
            Set<Integer> tidsetA = entryA.getValue();

            // Create the new itemset (prefix + itemA)
            List<String> newPrefix = new ArrayList<>(prefix);
            newPrefix.add(itemA);

            // --- This is where we store the new frequent itemset ---
            // (We already stored the depth-1 items)
            if (newPrefix.size() > 1) {
                Word newWord = new Word(newPrefix);
                newWord.setTidset(tidsetA);
                String itemsetKey = generateKey(newPrefix);
                this.finalFrequentItemsets.put(itemsetKey, newWord);
            }

            // --- Create the conditional database for the next recursion ---
            List<Map.Entry<String, Set<Integer>>> newCandidates = new ArrayList<>();

            if (newPrefix.size() <= maxDepth){

                for (int j = i + 1; j < candidates.size(); j++) {
                    Map.Entry<String, Set<Integer>> entryB = candidates.get(j);
                    String itemB = entryB.getKey();
                    Set<Integer> tidsetB = entryB.getValue();

                    // --- START OF FIX ---

                    // 1. Create an EMPTY set. This uses almost no memory.
                    Set<Integer> newTidset = new HashSet<>();

                    // 2. Determine which set is smaller to iterate over
                    Set<Integer> smallerSet, largerSet;
                    if (tidsetA.size() < tidsetB.size()) {
                        smallerSet = tidsetA;
                        largerSet = tidsetB;
                    } else {
                        smallerSet = tidsetB;
                        largerSet = tidsetA;
                    }

                    // 3. Build the intersection by iterating over the SMALLER set
                    //    and adding to the EMPTY set.
                    for (Integer tid : smallerSet) {
                        if (largerSet.contains(tid)) {
                            newTidset.add(tid);
                        }
                    }
                    // --- END OF FIX ---

                    // 4. If the new itemset is frequent, add it for the next recursion
                    if (newTidset.size() >= this.absoluteMinSupport) {
                        newCandidates.add(Map.entry(itemB, newTidset));
                    }
                }

            }

            // If we have new candidates, recurse deeper
            if (!newCandidates.isEmpty()) {
                eclatRecursive(newPrefix, newCandidates, maxDepth);
            }
        }
    }

}