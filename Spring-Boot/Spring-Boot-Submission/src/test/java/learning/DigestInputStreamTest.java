package learning;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Learn by testing.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DigestInputStreamTest {
    @Test
    void twoDifferentBytesShouldHaveDifferentHash() throws NoSuchAlgorithmException, IOException {
        byte[] a = "int plus(int a, int b) {return a + b;}".getBytes();
        byte[] b = "int minus(int a, int b) {return a - b;}".getBytes();
        var d1 = new DigestInputStream(new ByteArrayInputStream(a), MessageDigest.getInstance("MD5"));
        var d2 = new DigestInputStream(new ByteArrayInputStream(b), MessageDigest.getInstance("MD5"));
        IOUtils.toByteArray(d1);  // consume
        IOUtils.toByteArray(d2);

        byte[] digest1 = d1.getMessageDigest().digest();
        byte[] digest2 = d2.getMessageDigest().digest();
        assertThat(digest1, not(equalTo(digest2)));

        String hex1 = printHexBinary(digest1);
        String hex2 = printHexBinary(digest2);
        assertNotEquals(hex1, hex2);
    }
}
