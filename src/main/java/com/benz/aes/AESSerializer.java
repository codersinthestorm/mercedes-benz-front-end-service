package com.benz.aes;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.benz.proto.CustomerOuterClass.Customer;

public class AESSerializer implements Serializer<Customer> {

	public static final String AES_SECRET_KEY = "aes.serializer.secret.keys";
	public static final String AES_SALT_KEY = "aes.serializer.salt.keys";

	private String secret = null;
	private String salt = null;

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		secret = (String) configs.get(AES_SECRET_KEY);
		if (secret == null) {
			throw new SerializationException(AES_SECRET_KEY + " cannot be null.");
		}

		salt = (String) configs.get(AES_SALT_KEY);
		if (salt == null) {
			throw new SerializationException(AES_SALT_KEY + " cannot be null.");
		}
	}

	@Override
	public byte[] serialize(String topic, Customer data) {
		return AES.encrypt(data.toByteArray(), secret, salt);
	}
}
