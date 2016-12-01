package fr.nom.champomier.gohomeapk;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import java.security.KeyStore;

import fr.nom.champomier.gohomeapk.clientssl.ClientCert;
import fr.nom.champomier.gohomeapk.clientssl.ClientSSLWebViewClient;


public class FullscreenActivity extends AppCompatActivity {
	// Log TAG
	private static final String TAG = "FullscreenActivity";

	/*
		private String  serverUrl = "https://www.champomier.nom.fr/";
		private String  certFileName = "client_app.p12";
	*/
	private String  serverUrl = "https://192.168.1.50:5100/";
	private String  certFileName = "client.p12";

	private String  certType = "PKCS12";

	private String certPass = "";


	private View mPassView;
	private EditText mPassInput;

	private WebView mMainWebView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		ClientCert.init(getAssets());

		mPassView = findViewById(R.id.login_form_view);
		mMainWebView = (WebView) findViewById(R.id.main_web_view);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		mPassInput = (EditText) findViewById(R.id.password);
		mPassInput.getText().clear();
		mPassInput.setError(null);

	}


	public void checkInputPass(View view) {
		String pass = mPassInput.getText().toString();
		if ( checkPass(pass) ) {
			certPass = pass;
			mPassView.setVisibility(View.GONE);
			InputMethodManager imm = (InputMethodManager) getSystemService(FullscreenActivity.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); //Show
			mPassInput.getText().clear();

			mMainWebView.setVisibility(View.VISIBLE);
			mMainWebView.setWebViewClient(new ClientSSLWebViewClient());
			mMainWebView.getSettings().setJavaScriptEnabled(true);
//			mMainWebView.clearCache(true);
			mMainWebView.loadUrl(serverUrl);
		} else {
			mPassInput.getText().clear();
		}
	}

	private boolean checkPass(String pass) {
		if ( pass == null || "".equals( pass ) ) return false;
		try {
			KeyStore store = ClientCert.getSSLKeyStore(certFileName, certType, pass);
			if (store == null) {
				Log.e(TAG, "ClientCert.getSSLKeyStore() returned null");
				return false;
			}
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}
}
