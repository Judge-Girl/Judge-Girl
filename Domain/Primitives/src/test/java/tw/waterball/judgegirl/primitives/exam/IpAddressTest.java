package tw.waterball.judgegirl.primitives.exam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IpAddressTest {

    @Test
    public void testCreateIpAddress() {
        assertThrows(IllegalArgumentException.class, () -> new IpAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress(""));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("127.0.1"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("127.0.O.0"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("127.0.0.1.0"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("1000.100.10.0"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("256.256.256.256"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddress("localhost"));
        assertDoesNotThrow(IpAddress::localhost);
    }
}