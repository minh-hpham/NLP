package ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class NerML {

	public static void main(String[] args) throws IOException {
		// hashset of ftype
		HashSet<String> ftype = new HashSet<>();
		for (int i = 3; i < args.length; i++) {
			ftype.add(args[i]);
		}

		//HashSet<String> locs = getLocations(args[2]);
		// for (String loc : locs) {
		// System.out.println(loc);
		// }
		ArrayList<ArrayList<Word>> train = getWords(args[0]);
		// for (int i = 0 ; i < train.size(); i++) {
		// ArrayList<Word> s = train.get(i);
		// for (int j = 0; j < s.size(); j ++) {
		// Word w = s.get(j);
		// System.out.println(w.getLabel() + " " + w.getPos()+ " " + w.getWord()
		// );
		// }
		// System.out.println();
		// System.out.println();
		// }

		// HashSet<String> trainwords = new HashSet<>();
		// HashSet<String> trainpos = new HashSet<>();
		// for (ArrayList<Word> s : train) {
		// for (Word w : s) {
		// trainwords.add(w.getWord());
		// trainpos.add(w.getPos());
		// }
		// }

		// HashMap<String, Integer> feature = generateTrainFeature(train,
		// ftype);
		// for (String key : feature.keySet()) {
		// System.out.println(key);
		// }
		// // train.readable
		// ArrayList<Node> trainNodes = generateTrainNodes(train, ftype, locs);
		// generateReadableFile(trainNodes, "train.txt.readable");
		// // test.readable
		//
		// ArrayList<ArrayList<Word>> test = getWords(args[1]);
		// ArrayList<Node> testNodes = generateTestNodes(test, ftype, locs,
		// trainwords, trainpos);
		// generateReadableFile(testNodes, "test.txt.readable");
		//
		// // SET UP VECTOR
		// HashMap<String, Integer> integerLabel = new HashMap<>();
		// integerLabel.put("O", 0);
		// integerLabel.put("B-PER", 1);
		// integerLabel.put("I-PER", 2);
		// integerLabel.put("B-LOC", 3);
		// integerLabel.put("I-LOC", 4);
		// integerLabel.put("B-ORG", 5);
		// integerLabel.put("I-ORG", 6);
		// // train.vector
		// generateVector(trainNodes, feature, integerLabel,
		// "train.txt.vector");
		// // test.vector
		// generateVector(testNodes, feature, integerLabel, "test.txt.vector");

	}

	private static HashSet<String> getLocations(String filename) throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(filename));
		HashSet<String> data = new HashSet<>();
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine()) != null) {
				data.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	private static ArrayList<ArrayList<Word>> getWords(String filename) throws FileNotFoundException, IOException {
		// InputStream in = new FileInputStream(new File(filename));
		ArrayList<ArrayList<Word>> data = new ArrayList<>();
		ArrayList<Word> sentence;
		String[] split;
		String line = null;
		Scanner reader = null;
		FileReader fileReader = null;
		ArrayList<String> test = new ArrayList<>();
		try {
			fileReader = new FileReader(filename);
			reader = new Scanner(fileReader);
			while (reader.hasNextLine()) {
				line = reader.nextLine();
				System.out.println(line);
			}
		}
		// Scanner reader = null;
		// String line = null;
		// try {
		// reader = new Scanner(new InputStreamReader(in));
		// while (reader.hasNextLine()) {
		// sentence = new ArrayList<>();
		// while (reader.hasNextLine() && (line = reader.nextLine()).isEmpty()
		// == false) {
		// System.out.println(line);
		// split = line.split("\\s+");
		// sentence.add(new Word(split[0], split[1], split[2]));
		// }
		// if (sentence.isEmpty() == false) {
		// data.add(sentence);
		// }
		// }
		// }
		catch (Exception e) {
			System.out.println(line);
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (fileReader != null)
					fileReader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
//			for (int i = 0; i < test.size(); i++) {
//				System.out.println(test.get(i));
//			}
		}
		return data;
	}
}
