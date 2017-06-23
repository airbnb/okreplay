package okreplay;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

class Network {
  static Collection<String> getLocalAddresses() {
    try {
      InetAddress local = InetAddress.getLocalHost();
      return Arrays.asList(local.getHostName(), local.getHostAddress(), "localhost",
          "127.0.0.1");
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
