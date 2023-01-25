package guru.learningjournal.kafka.examples;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, AppConfigs.transaction_id);

        // step 2 - creating a producer object
        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);
        producer.initTransactions();  //makes sure no other transaction started by instance of same producer is running

        logger.info("Start first transaction...");
        producer.beginTransaction();
        try {
            for (int i=0; i<AppConfigs.numEvents; i++){
                // step 3 - sending all the messages
                producer.send(new ProducerRecord<>(AppConfigs.topicName1, i, "simple message1-" + i));
                producer.send(new ProducerRecord<>(AppConfigs.topicName2, i, "simple message1-" + i));
            }
            logger.info("Committing first transaction");
            producer.commitTransaction();
        } catch (Exception e){
            logger.error("Exception in first transaction. Aborting........");
            producer.abortTransaction();
            producer.close();
            throw new RuntimeException(e);
        }

        logger.info("Start second transaction...");
        producer.beginTransaction();
        try {
            for (int i=0; i<AppConfigs.numEvents; i++){
                // step 3 - sending all the messages
                producer.send(new ProducerRecord<>(AppConfigs.topicName1, i, "simple message2-" + i));
                producer.send(new ProducerRecord<>(AppConfigs.topicName2, i, "simple message2-" + i));
            }
            logger.info("Committing first transaction");
            producer.abortTransaction();
        } catch (Exception e){
            logger.error("Exception in first transaction. Aborting........");
            producer.abortTransaction();
            producer.close();
            throw new RuntimeException(e);
        }

        logger.info("Finished sending messages. Closing Producer");
        // step 4 - closing producer
        producer.close();  // closing is necessary to save data form leaking.
    }
}
