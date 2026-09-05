package gateway.middlewarewebservice.webservice;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class HttpClientPostTest {

	public static void main(String[] args) {
		//postHttpPost("http://localhost:1002/javahonkJetty/postdata/applicationOctetStream", new String("Java Honk").getBytes());
		postHttpPost("http://192.168.0.105:8023/_WebServiceDemo", new String("Java Honk").getBytes());
	}

	private static void postHttpPost(String url, byte[] message) {

		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost postRequest = new HttpPost(url);

			ByteArrayEntity entity = new ByteArrayEntity(message);
			entity.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			postRequest.setEntity(entity);

			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() != Response.Status.OK
					.getStatusCode()) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

}
