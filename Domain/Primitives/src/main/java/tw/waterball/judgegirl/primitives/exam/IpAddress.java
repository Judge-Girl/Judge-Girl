package tw.waterball.judgegirl.primitives.exam;

import lombok.Getter;

import static java.util.Objects.requireNonNullElseGet;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class IpAddress {

    private final String ipAddress;

    public IpAddress(String ipAddress) {
        validateIpAddress(ipAddress);
        this.ipAddress = ipAddress;
    }

    public static IpAddress localhost() {
        return new IpAddress("127.0.0.1");
    }

    private void validateIpAddress(String ipAddress) {
        if (!requireNonNullElseGet(ipAddress, String::new)
                .matches("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")) {
            throw new IllegalArgumentException(String.format("Ip address: %s is incorrect", ipAddress));
        }
    }
}