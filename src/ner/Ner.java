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
import java.util.HashSet;

public class Ner {

	public static void main(String[] args) throws FileNotFoundException {
		// hashset of ftype
		HashSet<String> ftype = new HashSet<>();
		for (int i = 2; i < args.length; i++) {
			ftype.add(args[i]);
		}
		// train.readable
		generateReadableFile(args[0],ftype);
		// test.readable
		generateReadableFile(args[1],ftype);
		
		// 

	}

	private static void generateReadableFile(String text, HashSet<String> ftype) {
		File file = new File(text+".readable");
		FileWriter fw = null;
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try {
			InputStream in = new FileInputStream(text);
			reader = new BufferedReader(new InputStreamReader(in));
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			String line;
			StringBuilder sb = new StringBuilder();
			
			while ((line = reader.readLine().trim()) != null) {
				while (!line.isEmpty()) {
					String[] split = line.split("\\s+");
				}
				sb.append("WORD: ");
				
				writer.write(line);
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
		
	}

}
