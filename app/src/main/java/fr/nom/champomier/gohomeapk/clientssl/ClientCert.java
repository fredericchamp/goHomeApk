package fr.nom.champomier.gohomeapk.clientssl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Handle client certificate operations
 */
public class ClientCert {

	private static String certFileType = "PKCS12";
	private static String certFileName = "";
	private static String certPass = "";

	public static KeyStore getSSLKeyStore(String fileName, String type, String pass)
			throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		certFileType = type;
		certFileName = fileName;
		certPass = pass;
		KeyStore keyStore = KeyStore.getInstance(certFileType);
		keyStore.load(new FileInputStream(new File(certFileName)), certPass.toCharArray());
		return keyStore;
	}

	static KeyStore getSSLKeyStore()
			throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		return getSSLKeyStore(certFileName, certFileType, certPass);
	}


	private static SSLContext getSSLContext(KeyStore ks, char[] pass)
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, CertificateException, IOException {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
		kmf.init(ks, pass);
		KeyManager[] keyManagers = kmf.getKeyManagers();
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, null, null);
		return sslContext;
	}

	private static SSLContext getSSLContext()
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, CertificateException, IOException {
		return getSSLContext(getSSLKeyStore(), certPass.toCharArray());
	}


	private static InputStream getInputStreamFromURL(SSLContext sslContext, String strURL) throws IOException {
		URL requestedUrl = new URL(strURL);
		HttpsURLConnection urlConnection = (HttpsURLConnection) requestedUrl.openConnection();
		urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		urlConnection.setRequestMethod("GET");
		return new BufferedInputStream(urlConnection.getInputStream());
	}

	public static InputStream getInputStreamFromURL(String strURL) throws Exception {
		return getInputStreamFromURL(getSSLContext(), strURL);
	}

}
