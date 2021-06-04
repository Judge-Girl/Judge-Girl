package tw.waterball.judgegirl.springboot.student.broker;

import tw.waterball.judgegirl.primitives.submission.Bag;

import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.springboot.student.broker.BrokerProperties.BAG_KEY_ADDITIONAL_DESTINATIONS;
import static tw.waterball.judgegirl.springboot.student.broker.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractBroker {
    protected List<String> getAdditionalDestinationsFromBag(Bag bag, String eventName) {
        return mapToList(bag.getAsString(BAG_KEY_ADDITIONAL_DESTINATIONS)
                        .map(s -> s.split("\\s*,\\s*"))
                        .orElseGet(() -> new String[0]),
                destination -> STOMP_ROOT_DESTINATION_PREFIX + destination + "/" + eventName);
    }
}
