package com.benz.api;

import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.benz.aes.AES;
import com.benz.api.exceptions.CustomerNotFoundException;
import com.benz.api.exceptions.InvalidRequestException;
import com.benz.api.exceptions.MessageBusException;
import com.benz.kafka.KafkaProducer;
import com.benz.proto.CustomerHelper;
import com.benz.proto.CustomerOuterClass.Customer;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/benz/customer")
public class CustomerApiController {

	private static final Logger LOG = LoggerFactory.getLogger(CustomerApiController.class);

	private final static String BACKEND_URL = "http://localhost:8082/api/v1/benz/customer";

	@Autowired
	private RestTemplate rest;

	@Autowired
	private KafkaProducer kafka;

	@GetMapping
	public String read(@RequestParam(name = "username", required = true) String username) {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BACKEND_URL).queryParam("username", username);
		LOG.debug("Connecting to URL: [" + BACKEND_URL + "]");

		ResponseEntity<byte[]> response = rest.exchange(builder.toUriString(), HttpMethod.GET,
				new HttpEntity<String>(headers), byte[].class);

		byte[] tmp = response.getBody();
		if (tmp != null)
			return new String(AES.decrypt(tmp, AES.SECRET, AES.SALT));

		throw new CustomerNotFoundException("Customer does not exist for given username: " + username);
	}

	@PostMapping
	public void store(@RequestParam(name = "fileType", required = true) String fileType,
			@RequestBody(required = true) JsonNode body) {

		LOG.debug("POST call received; fileType=" + fileType + ", " + body.toString());
		Customer customer = CustomerHelper.buildCustomer(body, fileType);
		kafka.pushToKafka(customer, fileType);
	}

	@PutMapping
	public void update(@RequestParam(name = "fileType", required = true) String fileType,
			@RequestBody(required = true) JsonNode body) {

		LOG.debug("PUT call received; fileType=" + fileType + ", " + body.toString());
		Customer customer = CustomerHelper.buildCustomer(body, fileType);
		kafka.pushToKafka(customer, fileType);
	}

	/**
	 * exception handler for {@link CustomerNotFoundException}; sets error message
	 * and http code as 404
	 * 
	 * @param exception
	 * @param request
	 * @return
	 */
	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleMessageBusException(CustomerNotFoundException exception,
			WebRequest request) {

		LOG.error("Customer not found: ", exception);
		return buildErrorResponse(exception, exception.getMessage(), HttpStatus.NOT_FOUND, request);
	}

	/**
	 * exception handler for {@link MessageBusException}; sets error message and
	 * http code as 500
	 * 
	 * @param exception
	 * @param request
	 * @return
	 */
	@ExceptionHandler(MessageBusException.class)
	public ResponseEntity<ErrorResponse> handleMessageBusException(MessageBusException exception, WebRequest request) {

		LOG.error("Kafka down: ", exception);
		return buildErrorResponse(exception, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	/**
	 * exception handler for {@link InvalidRequestException}; sets error message and
	 * http code as 400
	 * 
	 * @param exception
	 * @param request
	 * @return
	 */
	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException exception, WebRequest request) {

		LOG.error("Invalid request received: ", exception);
		return buildErrorResponse(exception, exception.getMessage(), HttpStatus.BAD_REQUEST, request);
	}

	/**
	 * helper method to build complicated error responses if necessary later on
	 * 
	 * @param exception
	 * @param message
	 * @param httpStatus
	 * @param request
	 * @return
	 */
	private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exception, String message, HttpStatus httpStatus,
			WebRequest request) {

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessage(exception.getMessage());
		errorResponse.setStatus(httpStatus.value());

		if (isTraceOn(request)) {
			errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
		}
		return ResponseEntity.status(httpStatus).body(errorResponse);
	}

	/**
	 * 
	 * @param request
	 * @return true if webrequest has trace query parameter set as true
	 */
	private boolean isTraceOn(WebRequest request) {
		String[] value = request.getParameterValues("trace");
		return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
	}
}
