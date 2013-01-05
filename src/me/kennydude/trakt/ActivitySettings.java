package me.kennydude.trakt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class ActivitySettings extends PreferenceActivity {
	SharedPreferences prefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.addPreferencesFromResource(R.xml.preferences);
		prefs = PreferenceManager.getDefaultSharedPreferences(ActivitySettings.this);
		
		Preference pref = findPreference("logout");
		if(!prefs.contains("trakt-username")){
			pref.setEnabled(false);
		}
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder db = new AlertDialog.Builder(ActivitySettings.this);
				db.setTitle(R.string.logout);
				db.setMessage(R.string.confirm_logout);
				db.setNegativeButton(R.string.no, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
					
				});
				
				db.setPositiveButton(R.string.yes, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						prefs.edit().remove("trakt-username").remove("trakt-password").commit();
						findPreference("logout").setEnabled(false);
					}
					
				});
				db.show();
				
				return true;
			}
			
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	onBackPressed();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		// While this is **very** naughty, we need to make things reset!
		
		Intent r = getIntent().getParcelableExtra("return");
		r.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(r);
	}
	
}
