/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.kaist.mrlab.annotation.mturk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.AssignmentStatus;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;

import edu.kaist.mrlab.annotation.data.Pair;

/* 
 * Before connecting to MTurk, set up your AWS account and IAM settings as described here:
 * https://blog.mturk.com/how-to-use-iam-to-control-api-access-to-your-mturk-account-76fe2c2e66e2
 * 
 * Configure your AWS credentials as described here:
 * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 *
 */

public class CorrelationChecker {

	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		final CorrelationChecker sandboxApp = new CorrelationChecker(getSandboxClient());
//		sandboxApp.getAssignmentAnswer();
		sandboxApp.checkCorrelation();
	}

	private final AmazonMTurk client;

	private CorrelationChecker(final AmazonMTurk client) {
		this.client = client;
	}

	/*
	 * Use the Amazon Mechanical Turk Sandbox to publish test Human Intelligence
	 * Tasks (HITs) without paying any money. Make sure to sign up for a Sanbox
	 * account at https://requestersandbox.mturk.com/ with the same credentials as
	 * your main MTurk account.
	 */
	private static AmazonMTurk getSandboxClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(SANDBOX_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}

	private static Date date = new Date();
	private static Path resultFolder = Paths.get("data/result/" + date);
	private static Path checkFolder = Paths.get("data/result/Mon Apr 09 15:56:49 KST 2018");
	
	private static ArrayList<String> fileList;
	private static ArrayList<Path> filePathList;

	public static void loadCorpus() throws Exception {
		fileList = new ArrayList<>();
		filePathList = Files.walk(checkFolder).filter(p -> Files.isRegularFile(p))
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

	private void checkCorrelation() throws Exception {
		
		Path hitPath;
		while ((hitPath = extractInputPath()) != null) {
			if (hitPath.toString().contains("DS_Store")) {
				continue;
			}

			BufferedReader br = Files.newBufferedReader(hitPath);

			Map<String, Set<Pair>> whoWorksWhat = new HashMap<>(); 
			String input = null;
			while ((input = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(input, "\t");
				st.nextToken(); // HITID
				String workerID = st.nextToken();
				String assignmentID = st.nextToken();
				String relationID = st.nextToken();
				String answer = st.nextToken();
				
				String key = workerID + "\t" + assignmentID;
				
				Pair pair = new Pair(relationID, answer);
				
				if(whoWorksWhat.containsKey(key)) {
					Set<Pair> pairSet = whoWorksWhat.get(key);
					pairSet.add(pair);
					whoWorksWhat.put(key, pairSet);
				} else {
					Set<Pair> pairSet = new HashSet<>();
					pairSet.add(pair);
					whoWorksWhat.put(key, pairSet);
				}
			}
			
			List<List<Pair>> pairsList = new ArrayList<>();
			for(String key : whoWorksWhat.keySet()) {
				Set<Pair> pairSet = whoWorksWhat.get(key);
				List<Pair> pairList = new ArrayList<>();
				pairList.addAll(pairSet);
				Collections.<Pair>sort(pairList);
				pairsList.add(pairList);
			}
			
			int numOfWorker = pairsList.size();
			
			for(int i = 0; i < numOfWorker; i++) {
				
			}
			
			
		}
		
	}

	private void getAssignmentAnswer() throws Exception {

		
		File f = new File(resultFolder.toString());
		if(!f.exists()) {
			f.mkdirs();
		}
		
		BufferedReader br = Files.newBufferedReader(Paths.get("data/hit/innerwork_hit_urls_ids.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			st.nextToken();
			String hitId = st.nextToken();
			
			
			System.out.println("HITID : " + hitId);

			ListAssignmentsForHITRequest listHITRequest = new ListAssignmentsForHITRequest();
			listHITRequest.setHITId(hitId);
			listHITRequest.setAssignmentStatuses(Collections.singletonList(AssignmentStatus.Submitted.name()));

			listHITRequest.setMaxResults(10);
			ListAssignmentsForHITResult listHITResult = client.listAssignmentsForHIT(listHITRequest);
			List<Assignment> assignmentList = listHITResult.getAssignments();
			System.out.println("The number of submitted assignments is " + assignmentList.size());

			if (assignmentList.size() == 2) {
				
				BufferedWriter bw = Files.newBufferedWriter(Paths.get(resultFolder.toString(), hitId));
				
				// Iterate through all the assignments received
				for (Assignment asn : assignmentList) {

					List<String> relationList = new ArrayList<String>();
					List<String> answerList = new ArrayList<String>();

					System.out.println("The worker with ID " + asn.getWorkerId() + " submitted assignment "
							+ asn.getAssignmentId() + " and gave the answer " + asn.getAnswer());

					String answerXML = asn.getAnswer();
					InputSource is = new InputSource(new StringReader(answerXML));
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
					XPath xpath = XPathFactory.newInstance().newXPath();
					NodeList identifiers = (NodeList) xpath.evaluate("//Answer/QuestionIdentifier", document,
							XPathConstants.NODESET);
					for (int i = 0; i < identifiers.getLength(); i++) {
						Node identifier = identifiers.item(i);
						String relation = identifier.getTextContent();
						relationList.add(relation);
					}

					NodeList texts = (NodeList) xpath.evaluate("//Answer/FreeText", document, XPathConstants.NODESET);
					for (int i = 0; i < texts.getLength(); i++) {
						Node text = texts.item(i);
						String answer = text.getTextContent();
						answerList.add(answer);
					}

					for (int i = 0; i < relationList.size(); i++) {
						System.out.println(hitId + "\t" + asn.getWorkerId() + "\t" + asn.getAssignmentId() + "\t"
								+ relationList.get(i) + "\t" + answerList.get(i));
						bw.write(hitId + "\t" + asn.getWorkerId() + "\t" + asn.getAssignmentId() + "\t"
								+ relationList.get(i) + "\t" + answerList.get(i) + "\n");
					}

					System.out.println();

				}
				bw.close();

			}
			
		}
	}
}
