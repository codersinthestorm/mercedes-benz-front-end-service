package com.benz.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.benz.aes.AES;
import com.benz.aes.AESDeserializer;
import com.benz.aes.AESSerializer;

@Configuration
@EnableKafka
public class KafkaConfiguration {

	/**
	 * kafka servers address
	 */
	@Value("${spring.kafka.bootstrap-servers}")
	public String bootstrapServers;

	/**
	 * creates a topic named <b>merc-benz-topic</b> with 1 partition and 1 replica
	 * for single node kafka cluster, if not already created
	 * 
	 * @return
	 */
	@Bean
	public NewTopic topicExample() {
		return TopicBuilder.name("merc-benz-topic").partitions(1).replicas(1).build();
	}

	/**
	 * producer configuration
	 * 
	 * @return
	 */
	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AESSerializer.class);
		props.put(AESSerializer.AES_SECRET_KEY, AES.SECRET);
		props.put(AESSerializer.AES_SALT_KEY, AES.SALT);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "cid1");
		return new DefaultKafkaProducerFactory<>(props);
	}

	/**
	 * consumer configuration
	 * 
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, Object> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "merc-benz-consumer-group");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AESDeserializer.class);
		props.put(AESSerializer.AES_SECRET_KEY, AES.SECRET);
		props.put(AESSerializer.AES_SALT_KEY, AES.SALT);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(this.producerFactory());
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		final ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(this.consumerFactory());
		return factory;
	}
}
