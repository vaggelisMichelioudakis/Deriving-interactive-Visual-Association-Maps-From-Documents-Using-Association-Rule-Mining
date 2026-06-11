package org.example.Taxonomy;

import java.util.HashMap;
import java.util.Map;

/**
 * The present class was to be used at the start of the changing of the backend. Then was decided to
 * use the more efficient Eclat algorithm
 */

public class FP_G_Alg {
	class FPTree {
		Map<String, FPTreeNode> headerTable;
		FPTreeNode root;
	}
	class FPTreeNode {
		String item;
		int count;
		Map<String, FPTreeNode> children = new HashMap<>();
		FPTreeNode parent;

		FPTreeNode(String item, FPTreeNode parent) {
			this.item = item;
			this.parent = parent;
			this.count = 1;
		}
	}
}
