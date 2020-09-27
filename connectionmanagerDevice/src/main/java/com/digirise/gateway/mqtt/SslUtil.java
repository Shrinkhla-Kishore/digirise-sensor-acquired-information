package com.digirise.gateway.mqtt;


import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class SslUtil {
    private static final Logger s_logger = LoggerFactory.getLogger(SslUtil.class);

    //   static SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile) throws Exception
//    {
//        Security.addProvider(new BouncyCastleProvider());
//
//        //===========加载 ca 证书==================================
//        // load CA certificate
//        PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
//        X509Certificate caCert = (X509Certificate)reader.readObject();
//        reader.close();
//
//        // CA certificate is used to authenticate server
//        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
//        caKs.load(null, null);
//        caKs.setCertificateEntry("ca-certificate", caCert);
//
//
//        //如果本地存在 server certificate 可以不从服务器下载，直接加载使用 ca 来验证
//        reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(crtFile)))));
//        X509Certificate cert = (X509Certificate)reader.readObject();
//        reader.close();
//
//
//        //=========使用 java 默认的信任cas(如果你的证书是比较大的ca 发的那么可以使用默认的)=========
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        tmf.init((KeyStore) null);
//
//        // finally, create SSL socket factory
//        SSLContext context = SSLContext.getInstance("TLSv1");
//        context.init(null,tmf.getTrustManagers(), null);
//
//        return context.getSocketFactory();
//    }

    public static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                     final String crtFile, final String keyFile, final String password)
            throws Exception {
        s_logger.info("Inside SSLSocketFactory getSocketFactory() method");
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

        FileInputStream fis = new FileInputStream(caCrtFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
            // System.out.println(caCert.toString());
            s_logger.info("CaCert is {}", caCert.toString());
        }

        // load client certificate
        bis = new BufferedInputStream(new FileInputStream(crtFile));
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
            // System.out.println(caCert.toString());
            s_logger.info("Client certificate is {}", cert.toString());
        }

        // load client private key
        s_logger.info("Loading client private key");
        PEMParser pemParser = new PEMParser(new FileReader(keyFile));
        Object object = pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                .build(password.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                .setProvider("BC");

        //New code
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(keyFile)));
//        File file = new File(keyFile);
//        FileInputStream fileInputStream = new FileInputStream(file);
//        DataInputStream dis = new DataInputStream(fileInputStream);
//        dis.readFully(fileInputStream.readAllBytes());

        s_logger.info("private key is {}", privateKeyContent);
        privateKeyContent = privateKeyContent.replaceAll("\\n", "");
        int index = privateKeyContent.indexOf("-----END EC PARAMETERS-----");
        privateKeyContent = privateKeyContent.substring(index);
        privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN EC PARAMETERS-----", "").replace("-----END EC PARAMETERS-----", "");
        privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN EC PRIVATE KEY-----", "").replace("-----END EC PRIVATE KEY-----", "");
        s_logger.info("private key is after stripping unnecessary information {}", privateKeyContent);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
  //      PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(privateKeyContent.getBytes());


        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(keySpecPKCS8.getEncoded()));
        PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(asn1InputStream.readObject());
        String algorithm = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId();
        s_logger.info("Algorithm used is {}", algorithm);
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        // End new code
//        KeyPair key;
//        if (object instanceof PEMEncryptedKeyPair) {
//            s_logger.info("Encrypted key - we will use provided password");
//            key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
//                    .decryptKeyPair(decProv));
//        } else {
//            s_logger.info("Unencrypted key - no password needed");
//            key = converter.getKeyPair((PEMKeyPair) object);
//        }
        pemParser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
//        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
//                new java.security.cert.Certificate[] { cert });
        ks.setKeyEntry("private-key", privKey, password.toCharArray(),
                new java.security.cert.Certificate[] { cert });
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

}
