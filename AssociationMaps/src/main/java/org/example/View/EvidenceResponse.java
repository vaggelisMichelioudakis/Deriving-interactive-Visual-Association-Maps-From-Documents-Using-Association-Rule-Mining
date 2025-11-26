package org.example.View;

import java.util.List;

/**
 * A simple DTO to send the list of evidence snippets back to the frontend.
 */
public class EvidenceResponse {

	private List<String> snippets;

	public EvidenceResponse(List<String> snippets) {
		this.snippets = snippets;
	}

	// Standard getter
	public List<String> getSnippets() {
		return snippets;
	}

	// Standard setter (optional, but good practice)
	public void setSnippets(List<String> snippets) {
		this.snippets = snippets;
	}
}