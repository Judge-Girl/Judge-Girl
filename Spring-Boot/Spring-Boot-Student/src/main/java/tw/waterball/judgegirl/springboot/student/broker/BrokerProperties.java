package tw.waterball.judgegirl.springboot.student.broker;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface BrokerProperties {
    // broadcast to additional destinations (split by commas)
    String BAG_KEY_ADDITIONAL_DESTINATIONS = "broker-additional-destinations";
}
