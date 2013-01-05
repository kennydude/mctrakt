package me.kennydude.trakt;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TraktApplication extends Application {
	public static final String API_KEY = "ff974702264203a11481ce0c501f501b2dc508bd";
	public static final String CHECKIN_BROADCAST_ACTION = "me.kennydude.trakt.CHECKIN_SHARE";
	public static final String VERSION = "0.0.1";
	public static ImageManager imageLoader;
	
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    
	    int PERCENTAGE_OF_CACHE = 50;
	    LoaderSettings settings = new SettingsBuilder()
	      .withCacheManager(new LruBitmapCache(this, PERCENTAGE_OF_CACHE)).build(this);
	    imageLoader = new ImageManager(this, settings);
	}
	
	public static final ImageManager getImageManager() {
	    return imageLoader;
	}
	
	public static boolean canUseAuthenticatedMethods(Context c){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		return sp.contains("trakt-username");
	}
	
	public static boolean auth(Activity c){
		boolean r = canUseAuthenticatedMethods(c);
		if(!r){
			Intent i = new Intent(c, ActivityLogin.class);
			i.putExtra("return", c.getIntent());
			c.startActivity(i);
		}
		return r;
	}
	
}
