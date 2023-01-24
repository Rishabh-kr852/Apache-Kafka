package guru.learningjournal.kafka.examples;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class dispatcherDemo{
    private static final Logger logger = LogManager.getLogger();
    public static void main(String[] args){
        Properties props = new Properties();
        try {
            InputStream inputStream = new FileInputStream(AppConfigs.kafkaConfigFileLocation);
            props.load(inputStream);
            props.put(ProducerConfig.CLIENT_ID_CONFIG,AppConfigs.applicationID);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }

        KafkaProducer<Integer, String> producer = new KafkaProducer<Integer, String>(props);
        Thread[] dispatchers = new Thread[AppConfigs.eventFiles.length];
        logger.info("Starting dispatcher threads...");
        for(int i=0; i<AppConfigs.eventFiles.length; i++){
            dispatchers[i] = new Thread(new dispatcher(producer,AppConfigs.topicName,AppConfigs.eventFiles[i]));
            dispatchers[i].start();
        }
        try {
            for (Thread t:dispatchers) t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producer.close();
            logger.info("Finished dispatcher demo");
        }
    }
}
