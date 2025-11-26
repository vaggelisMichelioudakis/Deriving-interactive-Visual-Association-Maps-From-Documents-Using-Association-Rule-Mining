package org.example.View;

import org.example.Taxonomy.*;
import org.example.Taxonomy.Node;
import org.example.Taxonomy.Rule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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

    public static void newProcess(int i, int maxDepth){
        rules = new HashMap<>();

        // 1. Get ALL frequent itemsets (any length) using Eclat
        Map<String, Word> allFrequentItemsets = index.findFrequentItemsetsEclat(index.getTopTerms(), support, i, maxDepth);

        //Map<String, Word> allFrequentItemsets = index.findFrequentItemsetsEclat(index.getTerms(), support, i, maxDepth);

        // We need a helper to generate sorted keys
        // (You should move this to Index.java and make it public)
        // For now, a local copy is fine.
        Index tempIndex = new Index(); // Just to access the key method

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
                String consequentKey = tempIndex.generateKey(consequent);

                Word antecedentWord = allFrequentItemsets.get(antecedentKey);
                Word consequentWord = allFrequentItemsets.get(consequentKey);

                // Antecedent might not be frequent (shouldn't happen, but good to check)
                if (antecedentWord == null || consequentWord == null) {
                    continue;
                }

                double support_antecedent = antecedentWord.getSupport();
                double support_consequent = consequentWord.getSupport();

                // 6. Calculate confidence
                // Confidence = Support({A, B}) / Support({A})
                double conf = support_itemset / support_antecedent;

                if (conf >= confidence) {

                    // ===== NEW LIFT CALCULATION ========
                    // ===================================
                    double lift = (support_itemset * i) / (support_antecedent * support_consequent);
                    // ===================================

                    try {
                        String key = tempIndex.generateKey(antecedent) + "->" + tempIndex.generateKey(consequent);
                        Rule rule = new Rule(antecedent, consequent, conf, support_itemset, lift);

                        // Avoid duplicate rules
                        if (!rules.containsKey(key)) {
                            rules.put(key, rule);
                            //System.out.println("New rule: " + antecedent + " -> " + consequent + " CONFIDENCE: " + conf);
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
                                            Rule rule = new Rule(first,second,conf,support2,0);
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
        Index tempIndex = new Index(); // Just to access the key method

        long startTotal = System.currentTimeMillis();

        support = request.getSupport();
        confidence = request.getConfidence();
        phrase_length = request.getPhrase_length();
        path = request.getPath(); //  <----- UNCOMMENT AFTER EXPERIMENTING BIG MAN
        //path = "Groceries_dataset.csv";
        threshold = request.getChunkThr();
        granularity = request.getGranularity();
        topK = request.getTopK();
        index = new Index();

        System.out.println("Support: " + support);
        System.out.println("Confidence: " + confidence);
        System.out.println("Phrase Length: " + phrase_length);
        System.out.println("Threshold: " + threshold);
        System.out.println("Granularity: " + granularity);
        System.out.println("Top K: " + topK);

        GranularityEnhancement.setGranularity(GranularityEnhancement.Granularity.valueOf(granularity));

        // -------------------------------------------------
        // 1. READ + SEGMENT ALL FILES
        // -------------------------------------------------
        long startRead = System.currentTimeMillis();

        try {

            File file = new File(path);

            int i = 0 ;
            for (File fileEntry : file.listFiles()) {
                if (fileEntry.isDirectory()) {
                    System.out.println(fileEntry.getAbsolutePath());
                } else {
                    try {
                        String content = readFileAsString(fileEntry.getAbsolutePath());

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
        }catch (Exception e){
            e.printStackTrace();
        }

        long endRead = System.currentTimeMillis();
        System.out.println("[TIME] File reading + segmentation: " + (endRead - startRead) + " ms");

        // -------------------------------------------------
        // 2. TF-IDF CALCULATION
        // -------------------------------------------------
        long startTfIdf = System.currentTimeMillis();
        index.calculateTf_ID();
        index.setTHRESHOLD(support, phrase_length);
        long endTfIdf = System.currentTimeMillis();
        System.out.println("[TIME] TF-IDF calculation: " + (endTfIdf - startTfIdf) + " ms");

        // -------------------------------------------------
        // 3. RULE GENERATION (newProcess)
        // -------------------------------------------------
        long startRules = System.currentTimeMillis();
        //process(I);
        newProcess(I, phrase_length);
        long endRules = System.currentTimeMillis();
        System.out.println("[TIME] Rule generation (Eclat): " + (endRules - startRules) + " ms");

        // -------------------------------------------------
        // 4. BUILD NODES / EDGES (initFrame)
        // -------------------------------------------------
        long startGraph = System.currentTimeMillis();
        initFrame();
        long endGraph = System.currentTimeMillis();
        System.out.println("[TIME] Graph construction (nodes/edges): " + (endGraph - startGraph) + " ms");

        // -------------------------------------------------
        // 5. POPULATE GraphDt OBJECT
        // -------------------------------------------------
        long startDto = System.currentTimeMillis();
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
            Node fromNode = entry.getValue();
            for(Map.Entry<Node, List<Double>> node : fromNode.getchildren().entrySet()){

                Node toNode = node.getKey();

                // Re-create the key to find the rule
                String antecedentKey = tempIndex.generateKey(fromNode.getTerms());
                String consequentKey = tempIndex.generateKey(toNode.getTerms());
                String ruleKey = antecedentKey + "->" + consequentKey;

                // Look up the rule we calculated in newProcess()
                Rule foundRule = rules.get(ruleKey);

                if (foundRule != null) {
                    double lift = foundRule.getLift();

                    // =====> ADD THIS LINE TO PRINT THE LIFT VALUE <=====
                    //System.out.println("RULE: " + ruleKey + " | LIFT: " + lift);
                    // ========================================================

                    // We found the rule! Now we can get the lift.
                    Edge edge = new Edge(
                            fromNode.getValue(),          // 'from' ID
                            toNode.getValue(),            // 'to' ID
                            foundRule.getConfidence(),    // Confidence
                            foundRule.getSupport() / I,       // Support (absolute count)
                            foundRule.getLift()         // <-- The new Lift value!
                    );
                    graphDt.addEdge(edge);
                }
            }
        }
        long endDto = System.currentTimeMillis();
        System.out.println("[TIME] GraphDt population: " + (endDto - startDto) + " ms");

        // -------------------------------------------------
        // 6. PageRank
        // -------------------------------------------------
        System.out.println("Applying PageRank...");
        long startPR = System.currentTimeMillis();
        Map<String, Double> ranks = PageRank.applyPageRank(nodes, 0.85, 50);
        for (Map.Entry<String, Double> entry : ranks.entrySet()) {
            graphDt.setNodeRank(entry.getKey(), entry.getValue());
        }
        long endPR = System.currentTimeMillis();
        System.out.println("[TIME] PageRank: " + (endPR - startPR) + " ms");

        // -------------------------------------------------
        // TOTAL
        // -------------------------------------------------
        long endTotal = System.currentTimeMillis();
        System.out.println("[TIME] TOTAL execution time: " + (endTotal - startTotal) + " ms");

        graphDt.setTime(endTotal-startTotal);

        return ResponseEntity.ok(graphDt);
    }

    // Add this new method INSIDE your VisualRestController class
    // Make sure to import java.util.stream.Collectors and java.util.Arrays

    @GetMapping("/api/get-evidence")
    public ResponseEntity<EvidenceResponse> getEvidence(
            @RequestParam String from,
            @RequestParam String to) {

        List<String> snippets = new ArrayList<>();
        final int MAX_SNIPPETS = 5; // Let's not send more than 5 snippets

        // 1. Check if we have a path. This relies on the 'path' variable
        // being set by the last '/api/submit-values' call.
        if (path == null || path.isEmpty()) {
            snippets.add("Error: No data path found. Please submit values first.");
            return ResponseEntity.ok(new EvidenceResponse(snippets));
        }

        try {
            // 2. Get the search terms from the node IDs (e.g., "good man" -> ["good", "man"])
            // We must lowercase them to match the tokens.
            List<String> fromTerms = Arrays.asList(from.toLowerCase().split(" "));
            List<String> toTerms = Arrays.asList(to.toLowerCase().split(" "));

            File fileDir = new File(path);
            for (File fileEntry : fileDir.listFiles()) {
                if (fileEntry.isDirectory()) continue;

                String content = readFileAsString(fileEntry.getAbsolutePath());

                // 3. Re-run the segmentation just like in EnterValues
                List<String[]> segments = GranularityEnhancement.segmentText(content);

                // 4. Search each segment
                for (String[] tokenizedSegment : segments) {
                    // Convert the token array to a List for easy searching
                    List<String> segmentAsList = Arrays.asList(tokenizedSegment);

                    // 5. Check if this segment contains ALL 'from' terms AND ALL 'to' terms
                    if (segmentAsList.containsAll(fromTerms) && segmentAsList.containsAll(toTerms)) {

                        // 6. This is the "good enough" workaround:
                        // We re-join the tokens to create a readable snippet.
                        String snippet = String.join(" ", tokenizedSegment);

                        snippets.add("..." + snippet + "...");

                        // 7. Stop once we have enough snippets
                        if (snippets.size() >= MAX_SNIPPETS) {
                            break;
                        }
                    }
                }
                if (snippets.size() >= MAX_SNIPPETS) {
                    break;
                }
            }

            if (snippets.isEmpty()) {
                snippets.add("No evidence snippets found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            snippets.clear();
            snippets.add("Error while searching for evidence.");
        }

        return ResponseEntity.ok(new EvidenceResponse(snippets));
    }
}
