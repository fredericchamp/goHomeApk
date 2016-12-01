package fr.nom.champomier.gohomeapk.clientssl;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Array;
import java.util.Enumeration;

/**
 * Used for webView with client cert auth
 */

public class ClientSSLWebViewClient extends WebViewClient {
	// Log TAG
	private static final String TAG = "ClientSSLWebViewClient";

	private static PrivateKey privateKey = null;
	private static X509Certificate[] certificates = null;
	private static KeyStore store = null;

	/*
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	*/

	@Override
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error){
		Log.e(TAG, error.toString());
		handler.proceed();
	}


	@Override
	public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
		try {
			store = ClientCert.getSSLKeyStore();
			if (store == null) {
				Log.e(TAG, "ClientCert.getSSLKeyStore() returned null");
				return;
			}
			Enumeration<String> e = store.aliases();
			for (; e.hasMoreElements(); ) {
				String alias = e.nextElement();
				if (store.isKeyEntry(alias)) {
					KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) store.getEntry(alias, null);
					privateKey = entry.getPrivateKey();
					certificates = (X509Certificate[]) entry.getCertificateChain();
				}
			}
			request.proceed(privateKey, certificates);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			request.ignore();
		}
	}
}
