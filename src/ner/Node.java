package ner;

public class Node {
	private String LABEL, WORD, POS, WORDCON, POSCON, ABBR, CAP, LOCATION;
	
	public Node(String label,String word) {
		LABEL = label;
		WORD = word;
		WORDCON = POS = POSCON = ABBR = CAP = LOCATION = null;
	}
	public String[] getAll() {
		return new String[]{LABEL, WORD, WORDCON, POS,  POSCON, ABBR, CAP, LOCATION};
	}
	public void setWORDCON(String s) {
		WORDCON = s;
	}
	public void setPOS (String pos) {
		this.POS = pos;
	}
	public void setPOSCON(String s) {
		POSCON = s;
	}
	public void setABBR(boolean isOption) {
		if (isOption) {
			ABBR = WORD.length() <= 4 && WORD.matches("^([a-zA-Z]){0,3}(\\.+)$|\\.") ? "yes" : "no";
		} else {
			ABBR = "n/a";
		}
	}
	public void setCAP (boolean isOption) {
		if (isOption) {
			CAP = Character.isUpperCase(WORD.charAt(0)) ? "yes":"no";
		} else {
			CAP = "n/a";
		}
	}
	public void setABBR(boolean isOption, String originalWord) {
		if (isOption) {
			ABBR = WORD.length() <= 4 && originalWord.matches("^([a-zA-Z]){0,3}(\\.+)$|\\.") ? "yes" : "no";
		} else {
			ABBR = "n/a";
		}
	}
	public void setCAP (boolean isOption, String originalWord) {
		if (isOption) {
			CAP = Character.isUpperCase(originalWord.charAt(0)) ? "yes":"no";
		} else {
			CAP = "n/a";
		}
	}
	public void setLocation(boolean isOption, boolean inFile) {
		if (isOption) {
			LOCATION = inFile ? "yes" : "no";
		} else {
			LOCATION = "n/a";
		}
	}
	
	
}
