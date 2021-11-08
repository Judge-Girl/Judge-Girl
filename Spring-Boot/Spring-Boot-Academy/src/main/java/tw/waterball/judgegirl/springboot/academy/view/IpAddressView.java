package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.IpAddress;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class IpAddressView {

    private String ipAddress;

    public static IpAddressView toViewModel(IpAddress ipAddress) {
        return new IpAddressView(ipAddress.getIpAddress());
    }
}
