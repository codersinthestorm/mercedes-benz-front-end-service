package com.benz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@TestPropertySource("classpath:test.properties")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class FrontEndServiceApplicationTests {

	private final static String FRONTEND_URL = "http://localhost:8081/api/v1/benz/customer";

	@Test
	void contextLoads() {
	}

	private static RestTemplate rest = new RestTemplate();

	@Test
	public void testStoreCustomerSuccess() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// valid customer data
		node.put("username", "shivank" + ThreadLocalRandom.current().nextInt(1, 10000));
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> result = rest.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<String>(node.toString(), headers), String.class);

		assertEquals(200, result.getStatusCodeValue());
	}

	@Test
	public void testStoreCustomerFailure() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// invalid customer data
		node.put("username", "shivank" + ThreadLocalRandom.current().nextInt(1, 10000));
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
//		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		try {
			rest.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<String>(node.toString(), headers),
					String.class);

		} catch (HttpClientErrorException e) {
			assertEquals(400, e.getRawStatusCode());
		}
	}

	@Test
	public void testUpdateCustomerSuccess() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// valid customer data
		node.put("username", "shivank" + ThreadLocalRandom.current().nextInt(1, 10000));
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> result = rest.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<String>(node.toString(), headers), String.class);

		assertEquals(200, result.getStatusCodeValue());

	}

	@Test
	public void testUpdateCustomerFailure() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// invalid customer data
		node.put("username", "shivank" + ThreadLocalRandom.current().nextInt(1, 10000));
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
//		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		try {
			rest.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<String>(node.toString(), headers),
					String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			assertEquals(400, e.getRawStatusCode());
		}
	}

	@Test
	public void readCustomerSuccess() throws InterruptedException {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// create a valid customer
		int t = +ThreadLocalRandom.current().nextInt(1, 10000);
		node.put("username", "shivank" + t);
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> result = rest.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<String>(node.toString(), headers), String.class);

		assertEquals(200, result.getStatusCodeValue());

		// wait for backend to write to file
		TimeUnit.SECONDS.sleep(1L);

		// get same customer
		builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("username", "shivank" + t);
		headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = rest.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);

		assertEquals(200, result.getStatusCodeValue());
		assertEquals(node.toString(), result.getBody());
	}

	@Test
	public void readCustomerFailure() throws InterruptedException {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("fileType", "XML");
		ObjectNode node = JsonNodeFactory.instance.objectNode();

		// create a valid customer
		int t = +ThreadLocalRandom.current().nextInt(1, 10000);
		node.put("username", "shivank" + t);
		node.put("firstname", "shivank");
		node.put("lastname", "mishra");
		node.put("age", 25);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		ResponseEntity<String> result = rest.exchange(builder.toUriString(), HttpMethod.POST,
				new HttpEntity<String>(node.toString(), headers), String.class);

		assertEquals(200, result.getStatusCodeValue());

		// wait for backend to write to file
		TimeUnit.SECONDS.sleep(1L);

		// get different customer
		builder = UriComponentsBuilder.fromHttpUrl(FRONTEND_URL).queryParam("username", "shivank" + t + 1);
		headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		try {
			rest.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			assertEquals(404, e.getRawStatusCode());
		}
	}
}
