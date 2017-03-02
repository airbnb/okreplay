package walkman;

import com.google.common.collect.ImmutableList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

class Network {
  static Collection<String> getLocalAddresses() {
    try {
      InetAddress local = InetAddress.getLocalHost();
      return ImmutableList.of(local.getHostName(), local.getHostAddress(), "localhost",
          "127.0.0.1");
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
