package edu.kaist.mrlab.annotation.paragraph.anaphora;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Test {
	private static ArrayList<String> fileList;
	private static ArrayList<Path> filePathList;

	public static void loadCorpus() throws Exception {
		fileList = new ArrayList<>();
		filePathList = Files.walk(Paths.get("data/paragraph/corpus/")).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("Number of file paths: " + filePathList.size());
		fileList = new ArrayList<>(new HashSet<>(fileList));
	}

	private static synchronized Path extractInputPath() {
		if (filePathList.isEmpty()) {
			return null;
		} else {
			return filePathList.remove(filePathList.size() - 1);
		}
	}

	public static void main(String[] ar) throws Exception {
		loadCorpus();

		Path paragraphPath;

		int paragraphCount = 0;
		int documentCount = 0;

		while ((paragraphPath = extractInputPath()) != null) {
			if (paragraphPath.toString().contains("DS_Store")) {
				continue;
			}
			documentCount++;
			BufferedReader br = Files.newBufferedReader(paragraphPath);
			String input = null;
			while ((input = br.readLine()) != null) {
				if (input.length() > 0) {
					paragraphCount++;
				}
			}
		}

		System.out.println(
				documentCount + "\t" + paragraphCount + "\t" + (double) paragraphCount / (double) documentCount);
	}

}
