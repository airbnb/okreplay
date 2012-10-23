package co.freeside.betamax.util

class Network {

	static Collection<String> getLocalAddresses() {
		def local = InetAddress.localHost
		[local.hostName, local.hostAddress, 'localhost', '127.0.0.1']
	}

	private Network() {}

}
