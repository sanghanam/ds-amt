package edu.kaist.mrlab.annotation.mturk;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.GetHITRequest;
import com.amazonaws.services.mturk.model.GetHITResult;
import com.amazonaws.services.mturk.model.UpdateExpirationForHITRequest;

public class UpdateHITs {
	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		final UpdateHITs sandboxApp = new UpdateHITs(getSandboxClient());
		sandboxApp.updateExpiration();
	}

	private final AmazonMTurk client;

	private UpdateHITs(final AmazonMTurk client) {
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

	private void updateExpiration() throws Exception {

		BufferedReader br = Files.newBufferedReader(Paths.get("data/hit/innerwork_hit_urls_ids.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			st.nextToken();
			String hitId = st.nextToken();

			GetHITRequest getHITRequest = new GetHITRequest();
			getHITRequest.setHITId(hitId);
			GetHITResult getHITResult = client.getHIT(getHITRequest);
			System.out.println("HIT " + hitId + " status: " + getHITResult.getHIT().getExpiration());
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 6);
			Date expireAt = new Date(cal.getTimeInMillis());
			System.out.println(expireAt);
			
			UpdateExpirationForHITRequest updateExpiration = new UpdateExpirationForHITRequest();
			updateExpiration.setHITId(hitId);
			updateExpiration.setExpireAt(expireAt);
			client.updateExpirationForHIT(updateExpiration);
			
		}
	}
}
