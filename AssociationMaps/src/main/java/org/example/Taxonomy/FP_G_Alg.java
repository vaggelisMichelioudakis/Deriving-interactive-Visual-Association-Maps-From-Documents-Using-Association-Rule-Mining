package org.example.Taxonomy;

import java.util.HashMap;
import java.util.Map;

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
