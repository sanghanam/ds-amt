package edu.kaist.mrlab.annotation.mturk;

import java.io.IOException;
import java.util.List;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.DeleteHITRequest;
import com.amazonaws.services.mturk.model.HIT;
import com.amazonaws.services.mturk.model.ListHITsRequest;
import com.amazonaws.services.mturk.model.ListHITsResult;

public class DeleteAllHITs {
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String PROD_ENDPOINT = "https://mturk-requester.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		/*
		 * Use the Amazon Mechanical Turk Sandbox to publish test Human Intelligence
		 * Tasks (HITs) without paying any money. Sign up for a Sandbox account at
		 * https://requestersandbox.mturk.com/ with the same credentials as your main
		 * MTurk account
		 * 
		 * Switch to getProdClient() in production. Uncomment line 60, 61, & 66 below to
		 * create your HIT in production.
		 * 
		 */

		final DeleteAllHITs sandboxApp = new DeleteAllHITs(getSandboxClient());
		sandboxApp.deleteAllHITs();

	}

	private final AmazonMTurk client;

	private DeleteAllHITs(final AmazonMTurk client) {
		this.client = client;
	}

	private static AmazonMTurk getSandboxClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(SANDBOX_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}

	private static AmazonMTurk getProdClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(PROD_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}


	private void deleteAllHITs() throws IOException {
		DeleteHITRequest deleteHIT = new DeleteHITRequest();
		
		ListHITsRequest listHITs = new ListHITsRequest();
		ListHITsResult resultHITs = client.listHITs(listHITs);
		List<HIT> hitList = resultHITs.getHITs();
		System.out.println(resultHITs.getNumResults());
		for(HIT hit : hitList) {
			System.out.println(hit.getHITId());
			if(hit.getHITStatus().equals("Reviewable")){
				deleteHIT.setHITId(hit.getHITId());
				client.deleteHIT(deleteHIT);
			}
			
		}
		
	}

}