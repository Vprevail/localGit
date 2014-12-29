package ca.beseenbesafelimited;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class IdleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_idle);
		findViewById(R.id.btnDone).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent startMain = new Intent(Intent.ACTION_MAIN);
				//		startMain.addCategory(Intent.CATEGORY_HOME);
						startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(startMain);
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_idle, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		// don't go back to the main login screen
		// 'minimize' this application
		Intent startMain = new Intent(Intent.ACTION_MAIN);
	//	startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
	
}
