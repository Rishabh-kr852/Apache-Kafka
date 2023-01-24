package guru.learningjournal.kafka.examples;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;


public class helloProducer {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args){
        logger.info("Creating Kafka producer...");
        // step 1 - controlling behaviour by specifying the properties
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG,AppConfigs.applicationID);  // track source of message
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrapServers); // producer uses initially to establish the connection
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // step 2 - creating a producer object
        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);

        logger.info("Start sending messages...");
        for (int i=0; i<AppConfigs.numEvents; i++){
            // step 3 - sending all the messages
            producer.send(new ProducerRecord<>(AppConfigs.topicName, i, "simple message-" + i));
        }
        logger.info("Finished sending messages. Closing Producer");
        // step 4 - closing producer
        producer.close();  // closing is necessary to save data form leaking.
    }
}
