package tw.waterball.judgegirl.springboot.submission.controllers;

import org.springframework.http.HttpHeaders;
import tw.waterball.judgegirl.primitives.submission.Bag;

import java.util.HashMap;
import java.util.Map;

import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.HEADER_BAG_KEY_PREFIX;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class BagExtractor {
    public static Bag extractBagsFromHeaders(HttpHeaders headers) {
        // 'bag' is only supported for admins
        Map<String, String> messages = new HashMap<>();
        headers.forEach((key, val) -> {
            key = key.trim();
            if (key.startsWith(HEADER_BAG_KEY_PREFIX)) {  // for example: "BAG_KEY_helloKitty"
                String bagKey = key.substring(HEADER_BAG_KEY_PREFIX.length());  // the bagKey will be "helloKitty"
                messages.put(bagKey, val.get(0));
            }
        });
        return new Bag(messages);
    }
}
