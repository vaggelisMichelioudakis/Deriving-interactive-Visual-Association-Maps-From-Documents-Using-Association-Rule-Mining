package org.example.View;

import org.example.Taxonomy.*;
import org.example.Taxonomy.Node;
import org.example.Taxonomy.Rule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class VisualRestController {

    private static GraphDt graphDt;
    private static String path;
    private static double support = 0;
    private static double confidence;
    private static int phrase_length = 0;
    private static String granularity;
    private static float threshold = 0;
    private static int topK = 0;
    private static HashMap<String,Rule> rules;
    private static int k = 0;

    private static Index index;

    private static Map<String, Node> nodes;

    private static int I;
    public static String readFileAsString(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        return content;
    }

    // In VisualRestController.java
    // REPLACE your 'process(int i)' method with this one:

    public static void newProcess(int i){
        rules = new HashMap<>();

        // 1. Get ALL frequent itemsets (any length) using Eclat
        Map<String, Word> allFrequentItemsets = index.findFrequentItemsetsEclat(index.getTopTerms(), support, i);

        // We need a helper to generate sorted keys
        // (You should move this to Index.java and make it public)
        // For now, a local copy is fine.
        Index tempIndex = new Index("null"); // Just to access the key method

        System.out.println("Generating rules from " + allFrequentItemsets.size() + " frequent itemsets...");

        // 2. Iterate over every frequent itemset
        for (Map.Entry<String, Word> entry : allFrequentItemsets.entrySet()) {
            List<String> itemset = entry.getValue().getValues();

            // ==============================================================
            // 💡 ADD THIS SAFETY CHECK 💡
            // ==============================================================
            // If an itemset has more than 20 items, skip it.
            // It's too big to generate subsets for and the rules are useless.
            // You can change 20 to 15 or 25.
            if (itemset.size() > 20) {
                System.out.println("SKIPPING rule generation for large itemset (size "
                        + itemset.size() + ")");
                continue; // Skip this loop, move to the next itemset
            }
            // ==============================================================

            // We can only generate rules from sets with 2 or more items
            if (itemset.size() < 2) {
                continue;
            }

            // This is the support of the *full set*, e.g., Support({A, B})
            double support_itemset = entry.getValue().getSupport();

            // 3. Get all non-empty, proper subsets
            // Your Word class already has this!
            List<List<String>> subsets = Word.getAllSubsets(itemset);

            for (List<String> antecedent : subsets) {
                // We only want proper subsets, i.e., not the full itemset itself
                if (antecedent.isEmpty() || antecedent.size() == itemset.size()) {
                    continue;
                }

                // 4. Find the consequent
                List<String> consequent = new ArrayList<>(itemset);
                consequent.removeAll(antecedent);

                // 5. Look up the support for the antecedent (e.g., Support({A}))
                // We generate the key (e.g., "A") to look in the map
                String antecedentKey = tempIndex.generateKey(antecedent); // Use the helper

                Word antecedentWord = allFrequentItemsets.get(antecedentKey);

                // Antecedent might not be frequent (shouldn't happen, but good to check)
                if (antecedentWord == null) {
                    continue;
                }

                double support_antecedent = antecedentWord.getSupport();

                // 6. Calculate confidence
                // Confidence = Support({A, B}) / Support({A})
                double conf = support_itemset / support_antecedent;

                if (conf >= confidence) {
                    // We found a valid rule!
                    try {
                        String key = tempIndex.generateKey(antecedent) + "->" + tempIndex.generateKey(consequent);
                        Rule rule = new Rule(antecedent, consequent, conf, support_itemset);

                        // Avoid duplicate rules
                        if (!rules.containsKey(key)) {
                            rules.put(key, rule);
                            System.out.println("New rule: " + antecedent + " -> " + consequent + " CONFIDENCE: " + conf);
                        }
                    } catch (Exception e) {}
                }
            }
        }

        System.out.println("Generated " + rules.size() + " rules.");

        // Add a threshold to the maximum number of rules we can return just to avoid hairball
        if(rules.size() > 2000){
            System.out.println("TOO MANY RULES");
            // we will only keep a randomly 2000 rules
            return;
        }
    }



    // todo: remove duplicates
                public static void process(int i)   {
                    rules = new HashMap<>();
                    index.setTHRESHOLD(support, phrase_length);

                    //HERE LIES THE OLD APRIORI IF NEEDED AGAIN
                    Map<String, Word> subSets = index.Findsubsets(index.getTerms(), 2, i);


                    for(Map.Entry<String, Word> entry : subSets.entrySet()){

                        /*System.out.println("_______________");
                        System.out.println(entry.getKey());
                        System.out.println("_______________");*/ //UNCOMMENT LATER


                        List<String> subset = entry.getValue().getValues();

                        for(int k = 0; k< subset.size(); k++){

                            List<String> first = new ArrayList<>();
                            first.add(subset.get(k));
                            int support1 = index.getSuppoort(first);

                            for(int l = 0; l< subset.size(); l++){
                                List<String> second = new ArrayList<>();
                                if(l!=k){

                                    second.add(subset.get(l));
                                    second.add(subset.get(k));
                                    int support2 = index.getSuppoort(second);
                                    double conf =  ((double)support2 / (double)support1);
                                    if(conf >= confidence){
                                        second.remove(second.size()-1);
                                        try{
                                            String key = first.get(0) + second.get(0);
                                            Rule rule = new Rule(first,second,conf,support2);
                                            rules.put(key, rule);
                                            System.out.println("New rule: " + first + " -> " + subset.get(l) + " CONFIDENCE: " + conf);
                                        }catch(Exception e){}
                                    }
                                }
                            }
            }


            // Add a threshold to the maximum number of rules we can return just to avoid hairball
            if(rules.size() > 2000){
                System.out.println("TOO MANY RULES");
                // we will only keep a randomly 2000 rules
                return;
            }
        }

    }


    private static void setLevels(){

        int max = 0;

        // set the level
        for(Node node : nodes.values()){
            node.setLevel(10,5);
            if(max < node.getLevel()){
                max = node.getLevel();
            }
        }

        if(max == 0){
            return;
        }

        List<Integer> uniqueLevels = new ArrayList<>();
        int size = nodes.size();
        for(Node node : nodes.values()){
            int level_norm = node.getLevel() * (size+1) / max;

            if(!uniqueLevels.contains(level_norm)){
                uniqueLevels.add(level_norm);
            }

            node.setLevel(level_norm);
        }

        // normalize
        uniqueLevels.sort(null);
        List<Integer> newLevels = new ArrayList<>();
        for(int i =0; i< uniqueLevels.size(); i++){
            newLevels.add(i+1);
        }

        for(Node node: nodes.values()){
            int level2 = uniqueLevels.indexOf(node.getLevel());
            node.setLevel(level2);
        }


    }

    // Create the nodes for each term
    private static void initFrame(){
        nodes = new HashMap<>();

        for(Map.Entry<String, Rule>entry : rules.entrySet()){
            for(String A : entry.getValue().getList()) {
                Node tmp;
                if (!nodes.containsKey(A)) {
                    ArrayList<String> c = new ArrayList<>();
                    c.add(A);
                    tmp = new Node(A, c);
                    nodes.put(A, tmp);
                } else {
                    tmp = nodes.get(A);
                }
                for (String B : entry.getValue().getOpposite()) {
                    Node tmp2;
                    if (!nodes.containsKey(B)) {
                        ArrayList<String> c = new ArrayList<>();
                        c.add(B);
                        tmp2 = new Node(B, c);
                        nodes.put(B, tmp2);
                    } else {
                        tmp2 = nodes.get(B);
                    }
                    tmp2.addParent(tmp);
                    tmp.addChild(tmp2, entry.getValue().getConfidence(), entry.getValue().getSupport()/I);
                }
            }

            setLevels();
        }
    }

    @PostMapping("/api/submit-values")
    public ResponseEntity<GraphDt> EnterValues(@RequestBody ValueRequest request){
        support = request.getSupport();
        confidence = request.getConfidence();
        phrase_length = request.getPhrase_length();
        path = request.getPath();
        threshold = request.getChunkThr();
        granularity = request.getGranularity();
        topK = request.getTopK();
        index = new Index("null");

        System.out.println("Support: " + support);
        System.out.println("Confidence: " + confidence);
        System.out.println("Phrase Length: " + phrase_length);
        System.out.println("Threshold: " + threshold);
        System.out.println("Granularity: " + granularity);
        System.out.println("Top K: " + topK);

        GranularityEnhancement.setGranularity(GranularityEnhancement.Granularity.valueOf(granularity));

        try {
            File file = new File(path);
            int i = 0 ;
            for (File fileEntry : file.listFiles()) {
                if (fileEntry.isDirectory()) {
                    System.out.println(fileEntry.getAbsolutePath());
                } else {
                    try {
                        String content = readFileAsString(fileEntry.getAbsolutePath());

                        //String[] tokens = content.split("\\s+");
                        // phrase length determins the number of words that make a term

                        //Here we try to adjust the txt-mining options Granularity
                        List<String[]> segments = GranularityEnhancement.segmentText(content);

                        for(String[] tokens : segments){
                            index.addCollection(tokens, i, phrase_length);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    i++;
                }
            }

            I = i;
            System.out.println("Processed " + I + " transactions (" + GranularityEnhancement.getGranularity() + " level)");

            index.calculateTf_ID();
            index.setTHRESHOLD(support, phrase_length);

            //process(I);
            newProcess(I);
            initFrame();

            graphDt = new GraphDt();

            for(Map.Entry<String, Node> entry : nodes.entrySet()){
                String label = "";
                for(String str : entry.getValue().getTerms()){
                    if(entry.getValue().getTerms().indexOf(str) == entry.getValue().getTerms().size() - 1){
                        label += str;
                    }else{
                        label += str + ", ";
                    }
                }
                graphDt.addNode(entry.getKey(), label, entry.getValue().getLevel());
            }

            for(Map.Entry<String, Node> entry : nodes.entrySet()){
                for(Map.Entry<Node, List<Double>> node : entry.getValue().getchildren().entrySet()){
                    Edge edge = new Edge(entry.getKey(), node.getKey().getValue(), node.getValue().get(0), node.getValue().get(1));
                    graphDt.addEdge(edge);
                }
            }

            System.out.println("DONE FOR: " + I +" FILES");
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Applying PageRank...");

        Map<String, Double> ranks = PageRank.applyPageRank(nodes, 0.85, 50);

        for (Map.Entry<String, Double> entry : ranks.entrySet()) {
            graphDt.setNodeRank(entry.getKey(), entry.getValue());
        }

        System.out.println("PageRank applied.");

        for (Map.Entry<String, Double> entry : ranks.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        return ResponseEntity.ok(graphDt);

    }

}
