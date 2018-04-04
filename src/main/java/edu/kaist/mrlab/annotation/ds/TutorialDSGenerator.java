package edu.kaist.mrlab.annotation.ds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class TutorialDSGenerator {

	private static Set<String> goldSet = new HashSet<>();
	private static Map<String, Set<String>> tutorialYesMap = new HashMap<>();
	private static Map<String, Set<String>> tutorialNoMap = new HashMap<>();

	public static void main(String[] ar) throws Exception {
		BufferedWriter bw = Files
				.newBufferedWriter(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink-tutorial.txt"));
		BufferedReader br = Files
				.newBufferedReader(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink-work.txt"));
		BufferedReader gold = Files.newBufferedReader(Paths.get("data/gold/gold_standard_link.txt"));

		String input = null;
		while ((input = gold.readLine()) != null) {
			input = input.replace(" << ", " [[ ");
			input = input.replace(" >> ", " ]] ");
			goldSet.add(input);
		}

		while ((input = br.readLine()) != null) {

			StringTokenizer st = new StringTokenizer(input, "\t");
			st.nextToken();
			st.nextToken();
			String prd = st.nextToken();

			if (goldSet.contains(input)) {
				if (tutorialYesMap.containsKey(prd)) {
					Set<String> temp = tutorialYesMap.get(prd);
					temp.add(input + "\tyes\n");
					tutorialYesMap.put(prd, temp);
				} else {
					Set<String> temp = new HashSet<>();
					temp.add(input + "\tyes\n");
					tutorialYesMap.put(prd, temp);
				}

			} else {
				if (tutorialNoMap.containsKey(prd)) {
					Set<String> temp = tutorialNoMap.get(prd);
					temp.add(input + "\tno\n");
					tutorialNoMap.put(prd, temp);
				} else {
					Set<String> temp = new HashSet<>();
					temp.add(input + "\tno\n");
					tutorialNoMap.put(prd, temp);
				}
			}
		}

		for (String prd : tutorialYesMap.keySet()) {
			List<String> yesList = new ArrayList<>(tutorialYesMap.get(prd));
			for (int i = 0; (i < yesList.size() && (i < 3)); i++) {
				bw.write(yesList.get(i));
			}

			List<String> noList = new ArrayList<>(tutorialNoMap.get(prd));
			for (int i = 0; (i < noList.size() && (i < 3)); i++) {
				bw.write(noList.get(i));
			}
		}

		bw.close();
		br.close();
		gold.close();

	}
}
