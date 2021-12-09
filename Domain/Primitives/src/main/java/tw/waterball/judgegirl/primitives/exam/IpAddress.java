package tw.waterball.judgegirl.primitives.exam;

import lombok.Getter;

import java.util.Objects;

import static java.util.Objects.requireNonNullElseGet;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class IpAddress {
    private static final IpAddress UNSPECIFIED_ADDRESS = new IpAddress("0.0.0.0");
    private static final IpAddress LOCALHOST = new IpAddress("127.0.0.1");
    private final String ipAddress;

    public IpAddress(String ipAddress) {
        validateIpAddress(ipAddress);
        this.ipAddress = ipAddress;
    }

    public static IpAddress unspecifiedAddress() {
        return UNSPECIFIED_ADDRESS;
    }

    public static IpAddress localhost() {
        return LOCALHOST;
    }

    private void validateIpAddress(String ipAddress) {
        if (!requireNonNullElseGet(ipAddress, String::new)
                .matches("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")) {
            throw new IllegalArgumentException(String.format("Ip address: %s is incorrect", ipAddress));
        }
    }

    @Override
    public String toString() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpAddress ipAddress = (IpAddress) o;
        return Objects.equals(this.ipAddress, ipAddress.ipAddress)
                || UNSPECIFIED_ADDRESS.ipAddress.equals(this.ipAddress)
                || UNSPECIFIED_ADDRESS.ipAddress.equals(ipAddress.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress);
    }
}
