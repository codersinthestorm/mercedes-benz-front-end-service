package com.benz.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.benz.api.exceptions.MessageBusException;
import com.benz.proto.CustomerOuterClass.Customer;

@Component
public class KafkaProducer {

	private static final Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	/**
	 * 
	 * @param payload
	 * @param messageKey
	 */
	public void pushToKafka(Customer payload, String fileType) {

		LOG.info("Pushing message to Kafka Topic: merc-benz-topic");
		final Message<Customer> message = MessageBuilder.withPayload(payload).setHeader("fileType", fileType)
				.setHeader(KafkaHeaders.TOPIC, "merc-benz-topic")
				.setHeader(KafkaHeaders.MESSAGE_KEY, payload.getUsername()).build();

		try {
			// send message synchronously
			kafkaTemplate.send(message).get();

		} catch (Exception e) {
			LOG.error("Unable to push to kafka successfully: " + e.getMessage());
			throw new MessageBusException("Kafka down or topic unable to accept more requests");
		}
	}
}
