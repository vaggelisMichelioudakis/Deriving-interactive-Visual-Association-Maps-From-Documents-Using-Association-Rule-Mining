# Deriving-interactive-Visual-Association-Maps-From-Documents-Using-Association-Rule-Mining

## The Work:

This project presents a comprehensive re-engineering of the **"Word Taxonomy Explorer"**, transforming a legacy script into a high-performance, scalable visual analytics dashboard for extracting interactive knowledge graphs from unstructured text. To address the severe memory constraints and computational bottlenecks inherent in traditional association rule mining, the system's backend was completely overhauled by migrating from the breadth-first Apriori algorithm to the memory-efficient, depth-first Eclat algorithm. The frontend presentation layer was redesigned as an event-driven, Single-Page Application (SPA) dashboard that prioritizes user interaction and data explainability.

---

## User Manual

This section provides a step-by-step guide on how to install, configure, and use the software.

### 1. **Prerequisites**
- **Java Development Kit (JDK)**: Ensure you have JDK 17 or later installed on your system.
- **Maven**: The project uses Maven for dependency management and building. Make sure Maven is installed.
- **Text Documents**: Prepare the corpus of documents you want to analyze. Supported formats: `.txt` or any plain text format.

### 2. **Installation**
1. Clone the repository:
   ```bash
   git clone ...
   ```
2. Navigate to the project directory:
   ```bash
   cd AssociationMaps
   ```
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```

### 3. **Running the Program**
After building the project, you can run the program using the following command:
   ```bash
   java -jar target/WebApp1-1.0-SNAPSHOT.jar
   ```
   or 
   ```bash
   mvn spring-boot:run
   ```
#### 4. Starting the Program
- The program runs on `localhost:8080`.
  - After starting the program, open any web browser and navigate to:
    http://localhost:8080/visualize

---

#### 5. Input Parameters
On the visualization page, you will need to provide the following inputs:

1. **Folder Path**
- Find the full path to a folder on your computer - using the **"Select Folder"** button - that contains a collection of files.

2. **Granularity**
- From the dropbox select the **Granularity** option which you want your documents to be parsed.
- **Options: Document, Paragraph, Sentence and Sematic Chunk**

3. **Support Threshold**
- Set the minimum support threshold for the rules displayed in the knowledge map.
- Support indicates how frequently a rule appears in the dataset.
- Example: `0.1` (10%) or and absolute value like `10` (it's found in 10 documents)

4. **Confidence Threshold**
- Set the minimum confidence threshold for the rules displayed in the knowledge map.
- Confidence indicates the reliability of a rule.
- Example: `0.7` (70%)

5. **Chunk Threshold (Note: This parameter is active only when Chunk is selected)**
- Set the minimum Jaccard similarity coefficient required to group adjacent sentences into the same text block.
- This threshold determines the strictness of topic continuity. If the lexical overlap between two sentences drops below this value, the system detects a "semantic shift" and closes the current transaction to start a new one.
- Example: 0.3 (At least 30% of the unique words must overlap between sentences to keep them in the same chunk).

6. **Phrase Length**
- Specify the number of words each term in the map should contain.
- Example: `2` (for bigrams like "machine learning")

7. **Top-K Rules**
- Set the maximum number of rules/nodes to display in the knowledge map based on their global semantic importance.
- This utilizes the Weighted PageRank score to highlight the most critical "hub" concepts in the corpus, effectively filtering out less important peripheral nodes and reducing visual clutter.
- Example: 100 (Limits the visualization to only the top 100 most authoritative semantic associations).

---

#### 6. Interactive Knowledge Map
Once the program processes your input, an interactive knowledge map will be displayed. You can interact with the map as follows:

1. **Explore Terms (Nodes)**
- Click on any concept (node) to open the Node Info panel.
- This displays the term's calculated hierarchical level, its Weighted PageRank score (indicating its global importance as a "hub"), and its interconnected dependencies.

2. **Inspect Rules & Evidence (Edges)**
- Click on any connecting line (edge) to open the Evidence Modal.
- This not only displays the statistical strength of the rule (Confidence, Support, and Lift), but crucially retrieves the exact raw text snippets from the original documents. This allows you to verify exactly where and how the terms co-occurred in context.

3. **Live Filtering & State Updates**
- Use the sidebar to dynamically adjust thresholds (e.g., Min. Confidence, Min. Lift, or Max Edges per Node) to clarify the graph.
- Thanks to the event-driven architecture, the graph topology updates in real-time to reflect these new filters without requiring a full system restart or re-indexing of the corpus.

4. **Switch Visualization Modes**
- Move beyond the standard network topology by switching views via the Control Sidebar.
- You can utilize the Adjacency Heatmap to instantly spot dense logic clusters, or the Chord Diagram to analyze the directional flow and cyclic dependencies between concepts.

---

#### 7. Tips for Best Results
- Start with lower support and confidence thresholds to see more rules, then adjust as needed.
- Experiment with different phrase lengths to explore different levels of granularity in the terms.

### 8. Acknowledgments
**Efthimios Mitkousis:** For his foundational original work, **"Interactive Association Map Creation from Documents using Association Rule Mining"**, which served as the legacy baseline system that this project expanded upon and optimized.