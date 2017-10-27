package ner;

public class Word {
	private String LABEL, WORD, POS;
	public Word (String l, String p, String w) {
		LABEL = l;
		WORD = w;
		POS = p;
	}
	public String getLabel() {
		return LABEL;
	}
	public String getWord() {
		return WORD;
	}
	public String getPos() {
		return POS;
	}
	
	public void setWord(String w) {
		WORD = w;
	}
	public void setPos(String p) {
		POS = p;
	}
}
