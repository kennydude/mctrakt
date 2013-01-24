package me.kennydude.trakt;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class TraktApplication extends Application {
	public static final String API_KEY = "ff974702264203a11481ce0c501f501b2dc508bd";
	public static final String CHECKIN_BROADCAST_ACTION = "me.kennydude.trakt.CHECKIN_SHARE";
	public static final String VERSION = "0.0.1";
	private BitmapLruCache mCache;
	
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    
	    File cacheDir = new File(getCacheDir(), "smoothie");
  		cacheDir.mkdirs();

  		BitmapLruCache.Builder builder = new BitmapLruCache.Builder();
  		builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
  		builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheDir);

  		mCache = builder.build();
	}
	
	public final BitmapLruCache getLruCache(){
		return mCache;
	}
	
	public static TraktApplication getInstance(Context context) {
		return (TraktApplication) context.getApplicationContext();
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

	public static int sWidth = -1;
	public static int getScreenWidth(Activity a) {
		if(sWidth == -1){
			Point outSize = new Point();
			a.getWindowManager().getDefaultDisplay().getSize(outSize);
			sWidth = outSize.x;
		}
		
		return sWidth;
	}
	
}
