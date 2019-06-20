package hmh;

import com.hmhco.hmh.avro.OrderNotificationMessage;
import com.opencsv.CSVReader;
import io.codearte.jfairy.Fairy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DataProducer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProducer.class);
    private final KafkaTemplate<String, OrderNotificationMessage> kafka;
    private final Fairy fairy = Fairy.create(Locale.GERMANY);

    @Value("${topic}")
    private String topic;

    public DataProducer(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") KafkaTemplate<String, OrderNotificationMessage> kafka) {
        this.kafka = kafka;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader(args.getSourceArgs()[0]));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        }

        for (List<String> values : records) {
            for (String value : values) {
                String key = String.valueOf(new Random().nextLong());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String dateStr = formatter.format(new Date());

                OrderNotificationMessage orderNotificationMessage =
                        OrderNotificationMessage.newBuilder()
                                .setType("salesOrderNumber")
                                .setValue(value)
                                .setTargetDate(dateStr)
                                .build();

                LOGGER.info("producing {}, {}", key, orderNotificationMessage);
                kafka.send(topic, key, orderNotificationMessage);
            }
        }
    }
}
