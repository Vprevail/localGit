package ca.beseenbesafelimited;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import ca.beseenbesafelimited.esri.GeotriggerHelper;

import com.esri.android.geotrigger.GeotriggerApiClient;
import com.esri.android.geotrigger.GeotriggerApiListener;
import com.esri.android.geotrigger.GeotriggerBroadcastReceiver;
import com.esri.android.geotrigger.GeotriggerService;
import com.esri.android.geotrigger.TriggerBuilder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;





/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
//	public static final String[] LAYER_ID = {"7Z28"}; // layer id from Geoloqui
//	public static final String[] GROUP_ID = {"QWMD7WW_9"}; // group id from Geoloqui

	public static String Geoloqui_user_id; // user_id from Geoloqui
	public static String macAddr; // MAC address from Wifi
	
	public static final String PREFS_NAME = "preferences";
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	
	
//esri
    
    private static final String TAG = "GeotriggerActivity";

    // Create a new application at https://developers.arcgis.com/en/applications
    private static final String AGO_CLIENT_ID = "8bJ602xgroTXe0Ya";

    // The project number from https://cloud.google.com/console
    private static final String GCM_SENDER_ID = "33245749094";

    // A list of initial tags to apply to the device.
    // Triggers created on the server for this application, with at least one of these same tags,
    // will be active for the device.
    private static final String[] TAGS = new String[] {"Research Park", "Be Seen Be Safe"};
    private GeotriggerBroadcastReceiver mGeotriggerBroadcastReceiver;

    private boolean mShouldCreateTrigger;
    
    
	
	private Handler mHandler = new Handler();
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		// start Geoloqi service
//		Intent intent = new Intent(this, LQService.class);
//		startService(intent);
		
		// get email/password from stored preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String prefEmail = settings.getString("email", "");
        String prefPassword = settings.getString("password", "");
        String geoloqui_user_id = settings.getString("user_id", "");
        if(!TextUtils.isEmpty(geoloqui_user_id)) {
        	this.Geoloqui_user_id = geoloqui_user_id;
        }

        // get MAC address
        try {
			WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInf = wifiMan.getConnectionInfo();
			macAddr = wifiInf.getMacAddress();
        }
        catch (Exception e) {
			e.printStackTrace();
		}
        
		setContentView(R.layout.activity_login);

		// Set up the login form
		if(!TextUtils.isEmpty(prefEmail)) {
			mEmail = prefEmail;
		}
		else {
			mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		}
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

        // check stored email/password
        if(!TextUtils.isEmpty(prefEmail) && !TextUtils.isEmpty(prefPassword)) {
        	mEmail = prefEmail;
        	mPassword = prefPassword;
        	
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
        }
	
	
        mGeotriggerBroadcastReceiver = new GeotriggerBroadcastReceiver();

	}
	@Override
    public void onStart() {
        super.onStart();

        GeotriggerHelper.startGeotriggerService(this, AGO_CLIENT_ID, GCM_SENDER_ID, TAGS,
                GeotriggerService.TRACKING_PROFILE_ADAPTIVE);
    }
	/////////////////////
	@Override
	public void onResume(){
			super.onResume();
//			// Bind to the tracking service so we can call public methods on it
//	        Intent intent = new Intent(this, LQService.class);
//	        bindService(intent, mConnection, 0);
			registerReceiver(mGeotriggerBroadcastReceiver,
	                GeotriggerBroadcastReceiver.getDefaultIntentFilter());
	}
	///////////////////////
	@Override
    public void onPause() {
        super.onPause();

        // Unregister the broadcast receiver
        unregisterReceiver(mGeotriggerBroadcastReceiver);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		// check regex instead of just an @ symbol
		//} else if (!mEmail.contains("@")) {
		} else if (!VALID_EMAIL_ADDRESS_REGEX.matcher(mEmail).find()) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
			
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		private String message;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			String s = JSONLogin(mEmail, mPassword);
			boolean error = false;
			int error_code = 0;
			message = "";

			try {
				JSONObject json = new JSONObject(s);
				error = json.getBoolean("error");
				message = json.getString("message");
				error_code = json.getInt("error_code");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!error) {
				// server returned a good login
				return true;
			}
			else {
				Log.d("JSON ERROR", message);
				if(error_code > 100) {
					// TODO: server error
				}
				else if(error_code == 2) {
					// will prompt for password
					return false;
				}
				else {
					// store the email address
			        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			        SharedPreferences.Editor editor = settings.edit();
			        editor.putString("email", mEmail);
			        editor.commit();
					
					// go to registration page
			        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
			        startActivity(intent);

				}
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
			
			if (success) {
				// they have successfully logged in, save username and password
		        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putString("email", mEmail);
		        editor.putString("password", mPassword);
		        if(!TextUtils.isEmpty(LoginActivity.Geoloqui_user_id)) {
		        	editor.putString("user_id", LoginActivity.Geoloqui_user_id);
		        }
		        editor.commit();

		        // show everything is okay screen
		        Intent intent = new Intent(LoginActivity.this, IdleActivity.class);
		        startActivity(intent);

			} else {
				// login failed, but email exists
		        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putString("password", ""); // clear stored password
		        editor.commit();				
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
		
		private String JSONLogin(String strEmail, String strPassword) {
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://beseenbesafe.ca/json/login.php");
           
            try {
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
                nvps.add(new BasicNameValuePair("username", strEmail));
                nvps.add(new BasicNameValuePair("password", strPassword));

                // extra fields 
                if(!TextUtils.isEmpty(LoginActivity.macAddr)) {
                	nvps.add(new BasicNameValuePair("mac_address", LoginActivity.macAddr));
                }
                else {
                	nvps.add(new BasicNameValuePair("mac_address", "Unknown"));
                }
                nvps.add(new BasicNameValuePair("user_id", LoginActivity.Geoloqui_user_id));

                httppost.setEntity(new UrlEncodedFormEntity(nvps));

            	HttpResponse response = client.execute(httppost);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if(statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				} else {
					JSONObject object = new JSONObject();
					try {
						object.put("error", true);
						object.put("error_code", statusCode);
						object.put("message", String.valueOf(statusCode) + " Error contacting logingin server. Please try again later");
					 } catch (JSONException e) {
						  e.printStackTrace();
					 }
					return object.toString();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();			
		}

	}
	/////////////////////
//    private LQService mService;
//    private boolean mBound;

	/** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            try {
//                // We've bound to LocalService, cast the IBinder and get LocalService instance.
//                LQBinder binder = (LQBinder) service;
//                mService = binder.getService();
//                mBound = true;
//                Log.d("Corrie", "success onServiceConnected");
//                //added this stuff today
//                Log.d("C", mService.toString());
//               
//            } catch (ClassCastException e) {
//                // Pass
//            	Log.d("Corrie", "error onServiceConnected");
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mBound = false;
//        }
//    };
	/////////////////////
}
