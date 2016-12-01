package fr.nom.champomier.gohomeapk.clientssl;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import android.content.res.AssetManager;

/**
 * Handle client certificate operations
 */
public class ClientCert {

	private static String certFileType = "PKCS12";
	private static String certFileName = "";
	private static String certPass = "";
	private static AssetManager assetManager = null;

	public static void init(AssetManager assMgt) {
		if (assMgt != null) assetManager = assMgt;
	}


	public static KeyStore getSSLKeyStore(String fileName, String type, String pass)
			throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		certFileType = type;
		certFileName = fileName;
		certPass = pass;
		KeyStore keyStore = KeyStore.getInstance(certFileType);
		keyStore.load(assetManager.open(certFileName), certPass.toCharArray());
		return keyStore;
	}

	static KeyStore getSSLKeyStore()
			throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		if (assetManager == null) return null;
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
