package ner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Ner {
	private final static String PHI = "PHI";
	private final static String PHIPOS = "PHIPOS";
	private final static String OMEGA = "OMEGA";
	private final static String OMEGAPOS = "OMEGAPOS";
	private final static String UNK = "UNK";
	private final static String UNKPOS = "UNKPOS";

	public static void main(String[] args) throws IOException {
		// hashset of ftype
		HashSet<String> ftype = new HashSet<>();
		for (int i = 2; i < args.length; i++) {
			ftype.add(args[i]);
		}
		HashSet<String> locs = getLocations(args[2]);

		ArrayList<ArrayList<Word>> train = getWords(args[0]);
		HashSet<String> trainwords = new HashSet<>();
		HashSet<String> trainpos = new HashSet<>();
		for (ArrayList<Word> s : train) {
			for (Word w : s) {
				trainwords.add(w.getWord());
				trainpos.add(w.getPos());
			}
		}

		HashMap<String, Integer> feature = generateTrainFeature(train, ftype);
		// train.readable
		ArrayList<Node> trainNodes = generateTrainNodes(train, ftype, locs);
		generateReadableFile(trainNodes, "train.txt.readable");
		// test.readable
		ArrayList<ArrayList<Word>> test = getWords(args[1]);
		ArrayList<Node> testNodes = generateTestNodes(test, ftype, locs, trainwords, trainpos);
		generateReadableFile(testNodes, "test.txt.readable");

		// SET UP VECTOR
		HashMap<String, Integer> integerLabel = new HashMap<>();
		integerLabel.put("O", 0);
		integerLabel.put("B-PER", 1);
		integerLabel.put("I-PER", 2);
		integerLabel.put("B-LOC", 3);
		integerLabel.put("I-LOC", 4);
		integerLabel.put("B-ORG", 5);
		integerLabel.put("I-ORG", 6);
		// train.vector
		generateVector(trainNodes, feature, integerLabel, "train.txt.vector");
		// test.vector
		generateVector(testNodes, feature, integerLabel, "test.txt.vector");
	}

	private static void generateVector(ArrayList<Node> nodes, HashMap<String, Integer> feature,
			HashMap<String, Integer> integerLabel, String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = null;
		BufferedWriter writer = null;
		int size = nodes.size();
		String[] all = null;
		String[] split = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				all = nodes.get(i).getAll();
				// word
				sb.append(all[1]);
				sb.append(" ");
				// label
				sb.append(integerLabel.get(all[0]));
				sb.append(" ");
				// features
				split = all[4].split("\\s+");
				ArrayList<Integer> index = new ArrayList<>();
				index.add(feature.get("pos-" + all[3]));
				index.add(feature.get("prev-pos-" + split[0]));
				index.add(feature.get("next-pos-" + split[1]));

				if (all[5].equals("yes")) {
					index.add(feature.get("abbreviated"));
				}
				if (all[6].equals("yes")) {
					index.add(feature.get("capitalized"));
				}
				if (all[7].equals("yes")) {
					index.add(feature.get("islocation"));
				}
				Collections.sort(index);
				// pos, prev pos, next pos
				for (int j = 0; j < index.size(); j++) {
					sb.append(index.get(j));
					sb.append(":1 ");
				}
				writer.write(sb.toString());
				sb.setLength(0);
				writer.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static HashMap<String, Integer> generateTrainFeature(ArrayList<ArrayList<Word>> train,
			HashSet<String> ftype) {
		HashMap<String, Integer> data = new HashMap<>();
		int al_size = train.size();
		int t_size = -1;
		int count = 0;
		String key = null;
		ArrayList<Word> temp;
		// all words
		for (int i = 0; i < al_size; i++) {
			temp = train.get(i);
			t_size = temp.size();
			for (int j = 0; j < t_size; j++) {
				key = "word-" + temp.get(j).getWord();
				if (data.containsKey(key) == false) {
					data.put(key, count++);
				}
			}
		}
		data.put("word-UNK", count++);
		// all pos
		data.put("prev-pos-PHIPOS", count++);
		data.put("next-pos-OMEGAPOS", count++);
		String[] pos = new String[] { "NNP", "NN", "VBD", "DT", "IN", "UNKPOS" };

		for (int j = 0; j < pos.length; j++) {
			key = "pos-" + pos[j];
			if (data.containsKey(key) == false) {
				data.put(key, count++);
			}
			key = "prev-pos-" + pos[j];
			if (data.containsKey(key) == false) {
				data.put(key, count++);
			}
			key = " next-pos-" + pos[j];
			if (data.containsKey(key) == false) {
				data.put(key, count++);
			}

		}

		if (ftype.contains("ABBR")) {
			data.put("abbreviated", count++);
		}
		if (ftype.contains("CAP")) {
			data.put("capitalized", count++);
		}
		if (ftype.contains("LOCATION")) {
			data.put("islocation", count++);
		}
		return data;
	}

	private static ArrayList<Node> generateTestNodes(ArrayList<ArrayList<Word>> test, HashSet<String> ftype,
			HashSet<String> locs, HashSet<String> trainwords, HashSet<String> trainpos) {
		int inputLength = test.size();
		ArrayList<Node> data = new ArrayList<Node>();
		ArrayList<Node> temp;
		for (int i = 0; i < inputLength; i++) {
			temp = generateSentenceTestNodes(test.get(i), ftype, locs, trainwords, trainpos);
			data.addAll(temp);
		}
		return data;
	}

	private static ArrayList<Node> generateSentenceTestNodes(ArrayList<Word> sentence, HashSet<String> ftype,
			HashSet<String> locs, HashSet<String> trainwords, HashSet<String> trainpos) {
		ArrayList<Node> data = new ArrayList<>();
		int s_len = sentence.size();
		Node current = null;
		String pos, wordcon, poscon = null;

		if (!trainwords.contains(sentence.get(0).getWord())) {
			sentence.get(0).setWord(UNK);
		}
		if (!trainpos.contains(sentence.get(0).getPos())) {
			sentence.get(0).setPos(UNKPOS);
		}
		current = new Node(sentence.get(0).getLabel(), sentence.get(0).getWord());

		if (!trainwords.contains(sentence.get(1).getWord())) {
			sentence.get(1).setWord(UNK);
		}
		wordcon = ftype.contains("WORDCON") ? PHI + " " + sentence.get(1).getWord() : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(0).getPos() : "n/a";
		current.setPOS(pos);

		if (!trainpos.contains(sentence.get(1).getPos())) {
			sentence.get(1).setPos(UNKPOS);
		}
		poscon = ftype.contains("POSCON") ? PHIPOS + " " + sentence.get(1).getPos() : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"));
		current.setCAP(ftype.contains("CAP"));
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(0).getWord()));

		data.add(current);
		for (int i = 1; i < s_len - 1; i++) {
			current = new Node(sentence.get(i).getLabel(), sentence.get(i).getWord());

			if (!trainwords.contains(sentence.get(i + 1).getWord())) {
				sentence.get(i + 1).setWord(UNK);
			}
			wordcon = ftype.contains("WORDCON") ? sentence.get(i - 1).getWord() + " " + sentence.get(i + 1).getWord()
					: "n/a";
			current.setWORDCON(wordcon);

			pos = ftype.contains("POS") ? sentence.get(i).getPos() : "n/a";
			current.setPOS(pos);

			if (!trainpos.contains(sentence.get(i + 1).getPos())) {
				sentence.get(i + 1).setPos(UNKPOS);
			}
			poscon = ftype.contains("POSCON") ? sentence.get(i - 1).getPos() + " " + sentence.get(i + 1).getPos()
					: "n/a";
			current.setPOSCON(poscon);

			current.setABBR(ftype.contains("ABBR"));
			current.setCAP(ftype.contains("CAP"));
			current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(i).getWord()));

			data.add(current);
		}
		current = new Node(sentence.get(s_len - 1).getLabel(), sentence.get(s_len - 1).getWord());

		wordcon = ftype.contains("WORDCON") ? sentence.get(s_len - 2).getWord() + " " + OMEGA : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(s_len - 1).getPos() : "n/a";
		current.setPOS(pos);

		poscon = ftype.contains("POSCON") ? sentence.get(s_len - 2).getPos() + " " + OMEGAPOS : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"));
		current.setCAP(ftype.contains("CAP"));
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(s_len - 1).getWord()));

		data.add(current);
		return data;
	}

	private static HashSet<String> getLocations(String filename) throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(filename));
		HashSet<String> data = new HashSet<>();
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine().trim()) != null) {
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

	private static ArrayList<Node> generateTrainNodes(ArrayList<ArrayList<Word>> inputFile, HashSet<String> ftype,
			HashSet<String> locs) {
		int inputLength = inputFile.size();
		ArrayList<Node> data = new ArrayList<Node>();
		ArrayList<Node> temp;
		for (int i = 0; i < inputLength; i++) {
			temp = generateSentenceTrainNodes(inputFile.get(i), ftype, locs);
			data.addAll(temp);
		}
		return data;
	}

	private static ArrayList<Node> generateSentenceTrainNodes(ArrayList<Word> sentence, HashSet<String> ftype,
			HashSet<String> locs) {
		ArrayList<Node> data = new ArrayList<>();
		int s_len = sentence.size();
		Node current = null;
		String wordcon, pos, poscon = null;
		current = new Node(sentence.get(0).getLabel(), sentence.get(0).getWord());

		wordcon = ftype.contains("WORDCON") ? PHI + " " + sentence.get(1).getWord() : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(0).getPos() : "n/a";
		current.setPOS(pos);

		poscon = ftype.contains("POSCON") ? PHIPOS + " " + sentence.get(1).getPos() : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"));
		current.setCAP(ftype.contains("CAP"));
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(0).getWord()));

		data.add(current);
		for (int i = 1; i < s_len - 1; i++) {
			current = new Node(sentence.get(i).getLabel(), sentence.get(i).getWord());

			wordcon = ftype.contains("WORDCON") ? sentence.get(i - 1).getWord() + " " + sentence.get(i + 1).getWord()
					: "n/a";
			current.setWORDCON(wordcon);

			pos = ftype.contains("POS") ? sentence.get(i).getPos() : "n/a";
			current.setPOS(pos);

			poscon = ftype.contains("POSCON") ? sentence.get(i - 1).getPos() + " " + sentence.get(i + 1).getPos()
					: "n/a";
			current.setPOSCON(poscon);

			current.setABBR(ftype.contains("ABBR"));
			current.setCAP(ftype.contains("CAP"));
			current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(i).getWord()));

			data.add(current);
		}
		current = new Node(sentence.get(s_len - 1).getLabel(), sentence.get(s_len - 1).getWord());

		wordcon = ftype.contains("WORDCON") ? sentence.get(s_len - 2).getWord() + " " + OMEGA : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(s_len - 1).getPos() : "n/a";
		current.setPOS(pos);

		poscon = ftype.contains("POSCON") ? sentence.get(s_len - 2).getPos() + " " + OMEGAPOS : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"));
		current.setCAP(ftype.contains("CAP"));
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(s_len - 1).getWord()));

		data.add(current);
		return data;
	}

	private static ArrayList<ArrayList<Word>> getWords(String filename) throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(new File(filename));
		ArrayList<ArrayList<Word>> data = new ArrayList<>();
		ArrayList<Word> sentence;
		Scanner reader = null;
		String line = null;
		String[] split;
		try {
			reader = new Scanner(new InputStreamReader(in));
			while (reader.hasNext()) {
				sentence = new ArrayList<>();
				while ((line = reader.nextLine()).isEmpty() == false) {
					split = line.split("\\s+");
					sentence.add(new Word(split[0], split[1], split[2]));
				}
				data.add(sentence);
			}
		} finally {
			reader.close();
		}
		return data;
	}

	private static void generateReadableFile(ArrayList<Node> nodes, String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = null;
		BufferedWriter writer = null;
		int size = nodes.size();
		String[] all = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				all = nodes.get(i).getAll();
				writer.write("WORD: " + all[1]);
				writer.write("WORDCON: " + all[2]);
				writer.write("POS: " + all[3]);
				writer.write("POSCON: " + all[4]);
				writer.write("ABBR: " + all[5]);
				writer.write("CAP: " + all[6]);
				writer.write("LOCATION: " + all[7]);
				writer.newLine();
				writer.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
