package ngrams;

public class Data {
	private String sentence;
	private double unsmoothUnigrams;
	private double unsmoothBigrams;
	private double smoothBigrams;
	
	public Data (String sentence) {
		this.sentence = sentence;
	}
	public void setSentence (String sentence) {
		this.sentence = sentence;
	}
	public String getSentence () {
		return sentence;
	}
	public void setUnSmoothUnigrams (double p) {
		unsmoothUnigrams = p;
	}
	public void setUnSmoothBigrams (double p) {
		unsmoothBigrams = p;
	}
	public void setSmoothBigrams (double p) {
		smoothBigrams = p;
	}
	
	public double getUnSmoothUnigrams () {
		return unsmoothUnigrams;
	}
	public double getUnSmoothBigrams () {
		return unsmoothBigrams;
	}
	public double getSmoothBigrams () {
		return smoothBigrams;
	}
}
