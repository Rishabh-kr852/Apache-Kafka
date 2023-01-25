package guru.learningjournal.kafka.examples;

import guru.learningjournal.kafka.examples.serde.JsonSerializer;
import guru.learningjournal.kafka.examples.types.PosInvoice;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PosSimulator {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args){
        if(args.length < 3){
            System.out.println("Please provide command line arguments: topicName noOfProducers produceSpeed");
            System.exit(-1);
        }

        String topicName = args[0];
        int noOfProducer = new Integer(args[1]);
        int producerSpeed = new Integer(args[2]);

        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, AppConfigs.applicationId);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfigs.bootstrap_server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        KafkaProducer<String, PosInvoice> kafkaProducer = new KafkaProducer<>(props);
        ExecutorService executor = Executors.newFixedThreadPool(noOfProducer);
        final List<RunnableProducer> runnableProducers = new ArrayList<>();

        for (int i=0; i<noOfProducer; i++){
            RunnableProducer runnableProducer = new RunnableProducer(i, kafkaProducer, topicName, producerSpeed);
            runnableProducers.add(runnableProducer);
            executor.submit(runnableProducer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (RunnableProducer p : runnableProducers) p.shutdown();
            executor.shutdown();
            logger.info("Closing executor service");
            try {
                executor.awaitTermination(producerSpeed*2, TimeUnit.MILLISECONDS);
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
