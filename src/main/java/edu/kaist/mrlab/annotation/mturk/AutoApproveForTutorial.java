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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.AssignmentStatus;
import com.amazonaws.services.mturk.model.AssociateQualificationWithWorkerRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;

/* 
 * Before connecting to MTurk, set up your AWS account and IAM settings as described here:
 * https://blog.mturk.com/how-to-use-iam-to-control-api-access-to-your-mturk-account-76fe2c2e66e2
 * 
 * Configure your AWS credentials as described here:
 * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 *
 */

public class AutoApproveForTutorial {

	// TODO Change this to your HIT ID - see CreateHITSample.java for generating a
	// HIT
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		final AutoApproveForTutorial sandboxApp = new AutoApproveForTutorial(getSandboxClient());
		sandboxApp.checkAssignment();
	}

	private final AmazonMTurk client;

	private AutoApproveForTutorial(final AmazonMTurk client) {
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

	private void checkAssignment() throws Exception {

		List<String> reviewableHITList = new ArrayList<String>();
		Map<String, Set<String>> workerHITMap = new HashMap<>();

		BufferedReader br = Files.newBufferedReader(Paths.get("data/hit/tutorial_hit_urls_ids.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			st.nextToken();
			reviewableHITList.add(st.nextToken());
		}
		
		int tutorialHITCount = reviewableHITList.size();

		for (String hitId : reviewableHITList) {

			System.out.println("HITID : " + hitId);

			ListAssignmentsForHITRequest listHITRequest = new ListAssignmentsForHITRequest();
			listHITRequest.setHITId(hitId);
			listHITRequest.setAssignmentStatuses(Collections.singletonList(AssignmentStatus.Submitted.name()));

			// Get a maximum of 10 completed assignments for this HIT
			listHITRequest.setMaxResults(100);
			ListAssignmentsForHITResult listHITResult = client.listAssignmentsForHIT(listHITRequest);
			List<Assignment> assignmentList = listHITResult.getAssignments();
			System.out.println("The number of submitted assignments is " + assignmentList.size());

			// Iterate through all the assignments received
			for (Assignment asn : assignmentList) {

				String workerID = asn.getWorkerId();
				String assignmentID = asn.getAssignmentId();
				String answer = asn.getAnswer();

				if (workerHITMap.containsKey(workerID)) {
					Set<String> HITSet = workerHITMap.get(workerID);
					HITSet.add(hitId);
					workerHITMap.put(workerID, HITSet);
				} else {
					Set<String> HITSet = new HashSet<>();
					HITSet.add(hitId);
					workerHITMap.put(workerID, HITSet);
				}

				// System.out.println("The worker with ID " + workerID + " submitted assignment
				// "
				// + assignmentID + " and gave the answer " + answer);

			}

		}
		
		BufferedReader br1 = Files.newBufferedReader(Paths.get("data/qual/qualification_type_ids.txt"));
		String workerQualificationID = null;
		String input1 = null;
		while((input1 = br1.readLine()) != null) {
			if(input1.contains("workerQual")) {
				StringTokenizer st = new StringTokenizer(input1, "\t");
				st.nextToken(); // name;
				workerQualificationID = st.nextToken();
			}
		}

		for (String workerID : workerHITMap.keySet()) {
			int tutorialComplete = workerHITMap.get(workerID).size();
			System.out.println(workerID + "\t" + tutorialComplete);
			if (tutorialComplete == tutorialHITCount) {
				AssociateQualificationWithWorkerRequest addWorkerRequest = new AssociateQualificationWithWorkerRequest();
				addWorkerRequest.setWorkerId(workerID);
				addWorkerRequest.setQualificationTypeId(workerQualificationID);
				System.out.println("This worker : " + workerID + "\t" + workerQualificationID + " get worker qualification.");
				client.associateQualificationWithWorker(addWorkerRequest);
			}
		}

	}

}
