package com.benz.proto;

import com.benz.api.exceptions.InvalidRequestException;
import com.benz.proto.CustomerOuterClass.Customer;
import com.fasterxml.jackson.databind.JsonNode;

public class CustomerHelper {

	/**
	 * throws exception if validation fails, else returns customer object
	 * 
	 * @param raw
	 * @return
	 */
	public static Customer buildCustomer(JsonNode raw, String fileType) {

		// some basic validation, can be more extensive if required

		if (!("xml").equalsIgnoreCase(fileType) && !("csv").equalsIgnoreCase(fileType))
			throw new InvalidRequestException("Only XML/CSV files currently supported");

		if (!raw.hasNonNull("age") || !raw.get("age").isInt())
			throw new InvalidRequestException("Age of customer has to be non-null integer value");

		if (!raw.hasNonNull("username"))
			throw new InvalidRequestException("Username of customer has to be non-null string value");

		if (!raw.hasNonNull("firstname"))
			throw new InvalidRequestException("First Name of customer has to be non-null string value");

		if (!raw.hasNonNull("lastname"))
			throw new InvalidRequestException("Last Name of customer has to be non-null string value");

		return Customer.newBuilder().setAge(raw.get("age").asInt()).setUsername(raw.get("username").asText())
				.setFirstname(raw.get("firstname").asText()).setLastname(raw.get("lastname").asText()).build();
	}
}
