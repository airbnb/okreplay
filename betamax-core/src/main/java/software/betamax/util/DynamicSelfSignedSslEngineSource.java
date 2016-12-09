/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.littleshoot.proxy.SslEngineSource;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by sean on 4/14/16.
 */
public class DynamicSelfSignedSslEngineSource implements SslEngineSource {

    private static final String PASSWORD = "changeit";
    private static final String PROTOCOL = "TLS";
    private final File keyStoreFile;

    private String host;
    private int port;
    private SSLContext sslContext;

    public DynamicSelfSignedSslEngineSource(String host, int port) {
        this.host = host;
        this.port = port;
        this.keyStoreFile = new File(host + ".jks");
        initializeKeyStore();
        initializeSSLContext();
    }

    @Override
    public SSLEngine newSslEngine() {
        return sslContext.createSSLEngine(host, port);
    }

    @Override
    public SSLEngine newSslEngine(String peerHost, int peerPort) {
        return sslContext.createSSLEngine(peerHost, peerPort);
    }

    private void initializeKeyStore() {
        String localBetamaxKeystore = "betamax-local.jks";

        // we have to copy the original, immutable betamax.jks in order to sign a new cert
        if (!new File(localBetamaxKeystore).exists()) {
            try {
                InputStream betamaxKeystoreStream = getClass().getClassLoader().getResourceAsStream("betamax.jks");
                FileUtils.copyInputStreamToFile(betamaxKeystoreStream, new File(localBetamaxKeystore));
            } catch (IOException e) {
                e.printStackTrace(System.out);
                // TODO figure out logging when this happens
                return;
            }
        } else if (keyStoreFile.isFile()) {
            return;
        }

        // Generate a private key / cert for this site
        nativeCall("keytool", "-genkey", "-alias", this.host, "-keysize",
                "4096", "-validity", "36500", "-keyalg", "RSA", "-dname",
                "CN=" + this.host, "-keypass", PASSWORD, "-storepass",
                PASSWORD, "-keystore", keyStoreFile.getName());

        // Create a certificate signing request to send to the root authority
        nativeCall("keytool", "-certreq", "-file", this.host + ".csr", "-alias", this.host,
                "-keystore", keyStoreFile.getName(), "-storepass", PASSWORD);

        // Generate a certificate for the site signed by the root authority
        nativeCall("keytool", "-gencert", "-infile", this.host + ".csr", "-outfile", this.host + ".cert",
                "-alias", "betamax", "-keystore", localBetamaxKeystore, "-storepass", PASSWORD);

        // Bring the signed certificate into the keystore and trust it
        nativeCall("keytool", "-importcert", "-file", this.host + ".cert", "-noprompt", "-trustcacerts",
                "-alias", this.host, "-keystore", keyStoreFile.getName(), "-storepass", PASSWORD);
    }

    private void initializeSSLContext() {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        try {
            final KeyStore ks = KeyStore.getInstance("JKS");
            // ks.load(new FileInputStream("keystore.jks"),
            // "changeit".toCharArray());
            ks.load(new FileInputStream(keyStoreFile), PASSWORD.toCharArray());

            // Set up key manager factory to use our key store
            final KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, PASSWORD.toCharArray());

            // Set up a trust manager factory to use our key store
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(algorithm);
            tmf.init(ks);

            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                // TrustManager that trusts all servers
                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Initialize the SSLContext to work with our key managers.
            sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(keyManagers, trustManagers, null);
        } catch (final Exception e) {
            throw new Error(
                    "Failed to initialize the server-side SSLContext", e);
        }
    }

    private String nativeCall(final String... commands) {
        final ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            final Process process = pb.start();
            final InputStream is = process.getInputStream();
            return IOUtils.toString(is);
        } catch (final IOException e) {
            e.printStackTrace(System.out);
            // TODO figure out logging when this happens
            return "";
        }
    }
}

