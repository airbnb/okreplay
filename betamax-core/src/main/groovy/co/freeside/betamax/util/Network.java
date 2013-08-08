/*
 * Converted from Groovy to Java by Sean Freitag
 */

package co.freeside.betamax.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Network {
    public static Collection<String> getLocalAddresses() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            return new ArrayList<String>(Arrays.asList(local.getHostName(), local.getHostAddress(), "localhost", "127.0.0.1"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
