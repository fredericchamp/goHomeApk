package fr.nom.champomier.gohomeapk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.security.KeyStore;

import fr.nom.champomier.gohomeapk.clientssl.ClientCert;
import fr.nom.champomier.gohomeapk.clientssl.ClientSSLWebViewClient;


public class FullscreenActivity extends AppCompatActivity {
	// Local file path to store certificate
	public static final String localFilepath = "/data/fr.nom.champomier.gohomeapk/";
	// Preferences file name
	public static final String prefFileName = "goHome.pref";
	// Log TAG
	private static final String TAG = "FullscreenActivity";
	// Certificate type
	private static final String certType = "PKCS12";
	private SharedPreferences pref;

	private View mPassView;
	private EditText mServerUrl;
	private EditText mCertificate;
	private EditText mPassInput;
	private WebView mMainWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		mPassView = findViewById(R.id.login_form_view);
		mMainWebView = (WebView) findViewById(R.id.main_web_view);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		pref = getSharedPreferences(prefFileName, Context.MODE_PRIVATE);

		mServerUrl = (EditText) findViewById(R.id.serverurl);
		mServerUrl.setText(pref.getString("ServerURL", null));
		if (mServerUrl.getText().toString().length() > 0) {
			mServerUrl.setVisibility(View.GONE);
		}

		mCertificate = (EditText) findViewById(R.id.certificate);
		mCertificate.setText(pref.getString("Certificate", null));
		if (mCertificate.getText().toString().length() > 0) {
			mCertificate.setVisibility(View.GONE);
		}

		mPassInput = (EditText) findViewById(R.id.password);
		mPassInput.getText().clear();
		mPassInput.setError(null);
	}

	public void showParameters(View view) {
		mServerUrl.setVisibility(View.VISIBLE);
		mCertificate.setVisibility(View.VISIBLE);
	}

	private String getCertFilePath() {
		return Environment.getDataDirectory().getPath() + localFilepath;
	}

	private String getCertFileName() {
		return getCertFilePath() + pref.getString("Certificate", null);
	}

	public void selectCertificate(View view) {
		new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener() {
			@Override
			public void fileSelected(final File file) {
				mCertificate.setText(file.getName());
				try {
					FileChannel inputChannel = new FileInputStream(file).getChannel();
					FileChannel outputChannel = new FileOutputStream(new File(getCertFilePath(), file.getName())).getChannel();
					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
					inputChannel.close();
					outputChannel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).showDialog();
	}

	public void checkInputPass(View view) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("ServerURL", mServerUrl.getText().toString());
		editor.putString("Certificate", mCertificate.getText().toString());
		editor.commit();

		if (checkPass(mPassInput.getText().toString())) {
			mPassView.setVisibility(View.GONE);
			InputMethodManager imm = (InputMethodManager) getSystemService(FullscreenActivity.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); //Show
			mPassInput.getText().clear();

			mMainWebView.setVisibility(View.VISIBLE);
			mMainWebView.setWebViewClient(new ClientSSLWebViewClient());
			mMainWebView.getSettings().setJavaScriptEnabled(true);
			mMainWebView.loadUrl(pref.getString("ServerURL", null));
		} else {
			mPassInput.getText().clear();
		}
	}

	private boolean checkPass(String pass) {
		if ( pass == null || "".equals( pass ) ) return false;
		try {
			KeyStore store = ClientCert.getSSLKeyStore(getCertFileName(), certType, pass);
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
