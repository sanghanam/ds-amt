package edu.kaist.mrlab.annotation.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class SilverStandard {
	public static void main(String[] ar) throws Exception {

		Set<String> gsSet = new HashSet<String>();

		BufferedReader br = Files.newBufferedReader(Paths.get("data/gold/gold_standard_link.txt"));
		BufferedReader br2 = Files.newBufferedReader(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink.txt"));
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/gold/silver-standard.txt"));

		String input = null;
		while ((input = br.readLine()) != null) {
			gsSet.add(input);
		}

		while ((input = br2.readLine()) != null) {
			if(gsSet.contains(input)) {
				bw.write(input + "\t" + "yes" + "\n");
			} else {
				bw.write(input + "\t" + "no" + "\n");
			}
		}
		
		bw.close();

	}
}
