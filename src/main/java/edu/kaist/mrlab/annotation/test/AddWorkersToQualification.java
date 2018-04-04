package edu.kaist.mrlab.annotation.test;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.AssociateQualificationWithWorkerRequest;

public class AddWorkersToQualification {

	// TODO Change this to your HIT ID - see CreateHITSample.java for generating a
	// HIT
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(String[] ar) {
		final AddWorkersToQualification sandboxApp = new AddWorkersToQualification(getSandboxClient());
		sandboxApp.addWorkers();
	}

	private final AmazonMTurk client;

	private AddWorkersToQualification(final AmazonMTurk client) {
		this.client = client;
	}

	private static AmazonMTurk getSandboxClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(SANDBOX_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}
	
	private void addWorkers() {
		
		AssociateQualificationWithWorkerRequest addWorkerRequest = new AssociateQualificationWithWorkerRequest();
		addWorkerRequest.setWorkerId("A3HQFU4D5DYUZ0");
		addWorkerRequest.setQualificationTypeId("3T04ZEB6XQEVOHL8KZMD9S7KFB15AW");
		client.associateQualificationWithWorker(addWorkerRequest);
		
	}
}
