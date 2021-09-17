package com.benz.aes;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.benz.proto.CustomerOuterClass.Customer;
import com.google.protobuf.InvalidProtocolBufferException;

public class AESDeserializer implements Deserializer<Customer> {

	public static final String AES_SECRET_KEY = "aes.deserializer.secret.keys";
	public static final String AES_SALT_KEY = "aes.deserializer.salt.keys";

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
	public Customer deserialize(String topic, byte[] data) {
		try {
			return Customer.parseFrom(AES.decrypt(data, secret, salt));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return null;
	}
}
