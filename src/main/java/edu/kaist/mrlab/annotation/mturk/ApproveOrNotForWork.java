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
import java.util.StringTokenizer;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.ApproveAssignmentRequest;
import com.amazonaws.services.mturk.model.RejectAssignmentRequest;
import com.amazonaws.services.mturk.model.SendBonusRequest;

/* 
 * Before connecting to MTurk, set up your AWS account and IAM settings as described here:
 * https://blog.mturk.com/how-to-use-iam-to-control-api-access-to-your-mturk-account-76fe2c2e66e2
 * 
 * Configure your AWS credentials as described here:
 * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 *
 */

public class ApproveOrNotForWork {

	// TODO Change this to your HIT ID - see CreateHITSample.java for generating a
	// HIT
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		final ApproveOrNotForWork sandboxApp = new ApproveOrNotForWork(getSandboxClient());
		sandboxApp.approveORNOT();
	}

	private final AmazonMTurk client;

	private ApproveOrNotForWork(final AmazonMTurk client) {
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

	private void approveORNOT() throws Exception {

		BufferedReader br = Files.newBufferedReader(Paths.get("data/result/worker_assignment.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String workerID = st.nextToken();
			String assignmentID = st.nextToken();
			double score = Double.parseDouble(st.nextToken());

			if (score > 0.85) {
				// Approve the assignment
				ApproveAssignmentRequest approveRequest = new ApproveAssignmentRequest();
				approveRequest.setAssignmentId(assignmentID);
				approveRequest.setRequesterFeedback("감사합니다. 당신의 점수는 " + score + "  입니다.");
				approveRequest.setOverrideRejection(false);
				client.approveAssignment(approveRequest);
				System.out.println("Assignment has been approved: " + assignmentID + ", who: " + workerID);
			} else {
				RejectAssignmentRequest rejectRequest = new RejectAssignmentRequest();
				rejectRequest.setAssignmentId(assignmentID);
				rejectRequest.setRequesterFeedback("안타깝네요. 당신의 점수는 " + score
						+ "  입니다. 다른 작업자들과 비교했을 때 동일한 문제에 다른 답변을 많이 하였기 때문에 이것을 reject합니다. 조금만 더 분발해주세요!");
				client.rejectAssignment(rejectRequest);
				System.out.println("Assignment has been rejected: " + assignmentID + ", who: " + workerID);
			}
		}
	}

//	public void sendBonus() {
//		SendBonusRequest bonus = new SendBonusRequest();
//		bonus.setBonusAmount("1");
//		bonus.setWorkerId("");
//		bonus.setAssignmentId("");
//		bonus.setReason("20개 HIT 달성 기념으로 보너스 1$를 지급해드립니다.");
//	}
}
