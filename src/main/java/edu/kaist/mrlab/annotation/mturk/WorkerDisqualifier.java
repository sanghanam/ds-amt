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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.amazonaws.services.mturk.model.ApproveAssignmentRequest;
import com.amazonaws.services.mturk.model.Assignment;
import com.amazonaws.services.mturk.model.AssignmentStatus;
import com.amazonaws.services.mturk.model.HIT;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITRequest;
import com.amazonaws.services.mturk.model.ListAssignmentsForHITResult;
import com.amazonaws.services.mturk.model.ListReviewableHITsRequest;
import com.amazonaws.services.mturk.model.ListReviewableHITsResult;
import com.amazonaws.services.mturk.model.SendBonusRequest;

/* 
 * Before connecting to MTurk, set up your AWS account and IAM settings as described here:
 * https://blog.mturk.com/how-to-use-iam-to-control-api-access-to-your-mturk-account-76fe2c2e66e2
 * 
 * Configure your AWS credentials as described here:
 * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
 *
 */

public class WorkerDisqualifier {

	// TODO Change this to your HIT ID - see CreateHITSample.java for generating a
	// HIT
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		final WorkerDisqualifier sandboxApp = new WorkerDisqualifier(getSandboxClient());
		sandboxApp.checkAssignment();
	}

	private final AmazonMTurk client;

	private WorkerDisqualifier(final AmazonMTurk client) {
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

		ListReviewableHITsRequest listReviewableHITRequest = new ListReviewableHITsRequest();
		ListReviewableHITsResult listReviewableHITResult = client.listReviewableHITs(listReviewableHITRequest);
		List<HIT> reviewableList = listReviewableHITResult.getHITs();

		for (HIT hit : reviewableList) {

			String hitId = hit.getHITId();

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

				List<String> identifierList = new ArrayList<String>();
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
					identifierList.add(relation);
				}

				NodeList texts = (NodeList) xpath.evaluate("//Answer/FreeText", document, XPathConstants.NODESET);
				for (int i = 0; i < texts.getLength(); i++) {
					Node text = texts.item(i);
					String answer = text.getTextContent();
					answerList.add(answer);
				}

				for (int i = 0; i < identifierList.size(); i++) {
					System.out.println(asn.getWorkerId() + "\t" + asn.getAssignmentId() + "\t" + identifierList.get(i)
							+ "\t" + answerList.get(i));
				}

				System.out.println();

				// Approve the assignment
				// ApproveAssignmentRequest approveRequest = new ApproveAssignmentRequest();
				// approveRequest.setAssignmentId(asn.getAssignmentId());
				// approveRequest.setRequesterFeedback("Good work, thank you!");
				// approveRequest.setOverrideRejection(false);
				// client.approveAssignment(approveRequest);
				// System.out.println("Assignment has been approved: " + asn.getAssignmentId());
			}

		}

	}

	public void sendBonus() {
		SendBonusRequest bonus = new SendBonusRequest();
		bonus.setBonusAmount("1");
		bonus.setWorkerId("");
		bonus.setAssignmentId("");
		bonus.setReason("20개 HIT 달성 기념으로 보너스 1$를 지급해드립니다.");
	}
}
