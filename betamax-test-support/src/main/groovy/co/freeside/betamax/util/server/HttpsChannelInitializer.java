package co.freeside.betamax.util.server;

import java.io.*;
import java.security.*;
import javax.net.ssl.*;
import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.ssl.*;

public class HttpsChannelInitializer extends HttpChannelInitializer {

    public HttpsChannelInitializer(int workerThreads, ChannelHandler handler) {
        super(workerThreads, handler);
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        super.initChannel(channel);

        ChannelPipeline pipeline = channel.pipeline();

        SSLEngine engine = sslContext().createSSLEngine();
        engine.setUseClientMode(false);
        pipeline.addFirst("ssl", new SslHandler(engine));

    }

    private static SSLContext sslContext() throws GeneralSecurityException, IOException {
        InputStream keystoreStream = HttpsChannelInitializer.class.getResourceAsStream("/betamax.keystore");
        char[] password = "password".toCharArray();

        SSLContext sslContext = SSLContext.getInstance("TLS");

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(keystoreStream, password);

        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keyStore, password);

        sslContext.init(factory.getKeyManagers(), null, null);

        return sslContext;
    }
}
