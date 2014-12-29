package ca.beseenbesafelimited;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

import com.esri.android.geotrigger.GeotriggerApiClient;
import com.esri.android.geotrigger.GeotriggerApiListener;
import com.esri.android.geotrigger.GeotriggerBroadcastReceiver;
import com.esri.android.geotrigger.GeotriggerService;
import com.esri.android.geotrigger.TriggerBuilder;
//import com.esri.android.geotrigger.sample.GeotriggerActivity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;





public class RegistrationActivity extends Activity implements
GeotriggerBroadcastReceiver.LocationUpdateListener,
GeotriggerBroadcastReceiver.ReadyListener{	
	private UserRegistrationTask mAuthTask = null;
	
	// registration field values;
	private String mFirstName;
	private String mLastName;
	private String mCompany;
	//private String mOccupation;
	private String mEmail;
	private String mPassword;
	
	// UI reference
	private EditText mFirstNameView;
	private EditText mLastNameView;
	private EditText mCompanyView;
	//private EditText mOccupationView;
	private EditText mEmailView;
	private EditText mPasswordView;

	private View mRegistrationFormView;
	private View mRegistrationStatusView;
	//private TextView mRegistrationStatusMessageView;
	
    private Handler mHandler = new Handler();
    
    
//////////////////////////////////////
//    private LQService mService;
//    private boolean mBound;
    //esri
    
    private static final String TAG = "GeotriggerActivity";

    // Create a new application at https://developers.arcgis.com/en/applications
    private static final String AGO_CLIENT_ID = "8bJ602xgroTXe0Ya";

    // The project number from https://cloud.google.com/console
    private static final String GCM_SENDER_ID = "33245749094";

    // A list of initial tags to apply to the device.
    // Triggers created on the server for this application, with at least one of these same tags,
    // will be active for the device.
    private static final String[] TAGS = new String[] {"Research Park"};
    private GeotriggerBroadcastReceiver mGeotriggerBroadcastReceiver;

    private boolean mShouldCreateTrigger;

//	/** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            try {
//                // We've bound to LocalService, cast the IBinder and get LocalService instance.
//                LQBinder binder = (LQBinder) service;
//                mService = binder.getService();
//                mBound = true;
//                //Log.d("Corrie", "success onServiceConnected");
//                Log.d("C", mService.toString());
//               
//            } catch (ClassCastException e) {
//                // Failed FUBAR
//            	//Log.d("Corrie", "error onServiceConnected");
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mBound = false;
//        }
//    };
    //////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.activity_registration);

		// check for an email address stored in preferences
        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String prefEmail = settings.getString("email", "");
		if(!TextUtils.isEmpty(prefEmail)) {
			// set the email if it is stored
			mEmail = prefEmail;
			mEmailView = (EditText) findViewById(R.id.reg_email);
			mEmailView.setText(mEmail);
		}

		
		mFirstNameView = (EditText) findViewById(R.id.reg_firstname);
		mLastNameView = (EditText) findViewById(R.id.reg_lastname);
		mCompanyView = (EditText) findViewById(R.id.reg_company);
		//mOccupationView = (EditText) findViewById(R.id.reg_occupation);
		mEmailView = (EditText) findViewById(R.id.reg_email);
		mPasswordView = (EditText) findViewById(R.id.reg_password);
		
		mRegistrationFormView = findViewById(R.id.registration_form);
		mRegistrationStatusView = findViewById(R.id.registration_status);
		//mRegistrationStatusMessageView = (TextView) findViewById(R.id.registration_status_message);

		findViewById(R.id.btnRegister).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Log.d("Corrie", GeotriggerService.getDeviceId(RegistrationActivity.this.getApplicationContext()));
						registerUser();
					}
				});

		
		
		mGeotriggerBroadcastReceiver = new GeotriggerBroadcastReceiver();

        
	}
	
	@Override
	public void onResume(){
			super.onResume();
//			// Bind to the tracking service so we can call public methods on it
//	        Intent intent = new Intent(RegistrationActivity.this, LQService.class);
//	        bindService(intent, mConnection, 0);
			registerReceiver(mGeotriggerBroadcastReceiver,
	                GeotriggerBroadcastReceiver.getDefaultIntentFilter());
	}

	@Override
	public void onBackPressed() {
		// don't allow to go back
	}
	
	public void registerUser() {
		Log.d("Corrie", "registering user...");
		// Reset errors
		mFirstNameView.setError(null);
		mLastNameView.setError(null);
		mCompanyView.setError(null);
		//mOccupationView.setError(null);
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the registration
		mFirstName = mFirstNameView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		mCompany = mCompanyView.getText().toString();
		//mOccupation = mOccupationView.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// check validation
		if (TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setError(getString(R.string.error_field_required));
			focusView = mFirstNameView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mCompany)) {
			mCompanyView.setError(getString(R.string.error_field_required));
			focusView = mCompanyView;
			cancel = true;
		}

		// Check for a valid password
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		// check regex instead of just an @ symbol
		//} else if (!mEmail.contains("@")) {
		} else if (!LoginActivity.VALID_EMAIL_ADDRESS_REGEX.matcher(mEmail).find()) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt registration and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.			
			showProgress(true);
			// store the registration in our database
			
			mAuthTask = new UserRegistrationTask(this.getApplicationContext());
			mAuthTask.execute((Void) null);		
	        // create Geoloqi user   LoginActivity.LAYER_ID, LoginActivity.GROUP_ID,
//	        LQSession.createUserAccount(mEmail, mPassword, LoginActivity.LAYER_ID, LoginActivity.GROUP_ID, new OnRunApiRequestListener() {
//
//				@Override
//				public void onComplete(LQSession arg0, JSONObject arg1,
//						org.apache.http.Header[] arg2, StatusLine arg3) {
//					// TODO Auto-generated method stub
//					
//				}
//
//				@Override
//				public void onFailure(LQSession arg0, LQException arg1) {
//					// TODO Auto-generated method stub
//					Log.d("Corrie", "failed to register geoloqi user");
//					mAuthTask = new UserRegistrationTask();
//					mAuthTask.execute((Void) null);					
//				}
//			
//				@Override
//				public void onSuccess(LQSession session, JSONObject arg1,
//						org.apache.http.Header[] arg2) {
//					
//	                //mService.startService(intent);
//			        //mService.getTracker();
//			        //mService.getSession();
//					if (mService != null) {
//						//reset push token
//						LQSharedPreferences.removePushToken(RegistrationActivity.this);
//						//authenticate the session
//						
//						LQTracker tracker = mService.getTracker();
//						if (tracker != null) {
//							mService.setSavedSession(session);
//							tracker.setSession(session);
//							mService.getTracker().setSession(session);
//							Log.d("Corrie", "new push notification sent to server!");
//							//finish();
//						}
//						else {
//							Log.d("Corrie", "tracker is null");
//						}
//						
//					} else {
//						Log.d("Corrie", "mSerivce is null");
//					}
//					
//					////// mService is null and crashes, FUBAR
//					// set the push token, so we can send text messages
//					String token = LQSharedPreferences.getPushToken(RegistrationActivity.this);
//					if(token != null) {
//						session.sendGcmPushToken(token, new OnRunApiRequestListener() {
//							@Override
//							public void onSuccess(LQSession lqSession, JSONObject jsonObject, org.apache.http.Header[] headers) {
//								// handle success
//							}
//
//							@Override
//							public void onFailure(LQSession lqSession, LQException e) {
//								// handle error
//							}
//
//							@Override
//							public void onComplete(LQSession lqSession, JSONObject jsonObject, org.apache.http.Header[] headers, StatusLine statusLine) {
//							}
//						});
//
//					}
//					else {
//						//Log.d("Corrie", "token is null");
//					}
//
//					// get the Geoloqi user_id
//					try {
//						LoginActivity.Geoloqui_user_id = arg1.getString("user_id");
//						//Log.d("Corrie", "Got user_id = " + LoginActivity.Geoloqui_user_id);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//					// store the registration in our database
//					mAuthTask = new UserRegistrationTask();
//					mAuthTask.execute((Void) null);									
//				}
//			}, mHandler, RegistrationActivity.this);
		}
		
	}
	
	/**
	 * Shows the progress UI and hides the registration form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegistrationStatusView.setVisibility(View.VISIBLE);
			mRegistrationStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegistrationStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mRegistrationFormView.setVisibility(View.VISIBLE);
			mRegistrationFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegistrationFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegistrationStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegistrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {
		private String message;
		private Context context;
	    public UserRegistrationTask(Context context) {
	    	super();
			// TODO Auto-generated constructor stub
	    	this.context=context;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			String s = JSONRegistration(mFirstName, mLastName, mCompany, mEmail, mPassword,context);
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
				if(error_code > 100) {
					Log.d("JSON ERROR", message);
					return false;
				}
				else if(error_code == 2) {
					// will prompt for password
					Log.d("JSON ERROR", message);
					return false;
				}
				else {
					return true;					
				}
			}
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
			
			Log.d("Corrie", "post execute...");
			
			if(success) {
				// they have successfully registered, store username and password
		        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putString("email", mEmail);
		        editor.putString("password", mPassword);
		        editor.putString("deviceId", GeotriggerService.getDeviceId(context.getApplicationContext()));
		        editor.commit();
		        
		        // show everything is okay screen
		        Intent intent = new Intent(RegistrationActivity.this, IdleActivity.class);
		        startActivity(intent);
			} else {
				// registration failed, but email exists
				Log.d("Corrie", "registration failed, but email exists...");
		        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putString("password", ""); // clear stored password
		        editor.commit();
			}
			
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
		
		private String JSONRegistration(String strFirstName, String strLastName, String strCompany, String strEmail, String strPassword, Context context) {
			Log.d("Corrie", "doing JSON reg...");
			
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();

			HttpPost httppost = new HttpPost("https://beseenbesafe.ca/json/registration.php");
			
            try {
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>(2);
    			
    			nvps.add(new BasicNameValuePair("firstname", strFirstName));
    			nvps.add(new BasicNameValuePair("lastname", strLastName));
    			nvps.add(new BasicNameValuePair("company", strCompany));
    			//nvps.add(new BasicNameValuePair("occupation", strOccupation));
    			nvps.add(new BasicNameValuePair("email", strEmail));
    			nvps.add(new BasicNameValuePair("username", strEmail));
                nvps.add(new BasicNameValuePair("password", strPassword));			

                // extra fields
                if(!TextUtils.isEmpty(LoginActivity.macAddr)) {
                	nvps.add(new BasicNameValuePair("mac_address", LoginActivity.macAddr));
                }
                else {
                	nvps.add(new BasicNameValuePair("mac_address", "Unknown"));
                }
                nvps.add(new BasicNameValuePair("deviceId",GeotriggerService.getDeviceId(context.getApplicationContext())));
//                nvps.add(new BasicNameValuePair("deviceId","joel"));

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
					Log.d("Corrie", "reg happened");
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

	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the receiver. Activity will no longer respond to
        // GeotriggerService intents. Tracking and push notification handling
        // will continue in the background.
        unregisterReceiver(mGeotriggerBroadcastReceiver);
    }
	@Override
	public void onReady() {
		// TODO Auto-generated method stub
		
	}

	 @Override
	    public void onLocationUpdate(Location loc, boolean isOnDemand) {
	        // Called with the GeotriggerService obtains a new location update from
	        // Android's native location services. The isOnDemand parameter lets you
	        // determine if this location update was a result of calling
	        // GeotriggerService.requestOnDemandUpdate()
	        Toast.makeText(this, "Location Update Received!",
	                Toast.LENGTH_SHORT).show();
	        Log.d(TAG, String.format("Location update received: (%f, %f)",
	                loc.getLatitude(), loc.getLongitude()));

	        

	         
	           
	        
	    }
	
}
