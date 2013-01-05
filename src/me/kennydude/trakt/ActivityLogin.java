package me.kennydude.trakt;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Logs the user into Trakt when they want to access protected resources/activities
 * @author kennydude
 *
 */
public class ActivityLogin extends Activity {
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		setContentView(R.layout.activity_login);
		
		findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO: Validation
				
				final ProgressDialog pd = new ProgressDialog(ActivityLogin.this);
				pd.setMessage(getString(R.string.please_wait));
				pd.show();
				
				new Thread(new Runnable(){

					@Override
					public void run() {
						try{
							JSONObject p = new JSONObject();
							String username = ((EditText)findViewById(R.id.email)).getText().toString();
							p.put("username", username);
							
							String password = Utils.SHA1( ((EditText)findViewById(R.id.password)).getText().toString());
							p.put("password", password );
							
							p = new JSONObject( Utils.postRemoteJSON("http://api.trakt.tv/account/test/" + TraktApplication.API_KEY, p, false, ActivityLogin.this) );
							if(p.optString("status").equals("success")){
								SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityLogin.this);
								sp.edit().putString("trakt-username", username).putString("trakt-password", password).commit();
								runOnUiThread(new Runnable(){

									@Override
									public void run() {
										Toast.makeText(ActivityLogin.this, getString(R.string.logged_in), Toast.LENGTH_LONG).show();
										if(getIntent().hasExtra("return")){
											Intent r = getIntent().getParcelableExtra("return");
											r.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											startActivity(r);
										}
										finish();
										pd.dismiss();
									}
									
								});
							}
							
						} catch(Exception e){
							e.printStackTrace();
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									Toast.makeText(ActivityLogin.this, getString(R.string.failure), Toast.LENGTH_LONG).show();
									pd.dismiss();
								}
								
							});
						}
					}
					
				}).start();
				
			}
		});
	}
}
