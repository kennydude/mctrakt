package me.kennydude.trakt;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;

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
	public static ImageManager imageLoader;
	private BitmapLruCache mCache;
	
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    
	    int PERCENTAGE_OF_CACHE = 60;
	    LoaderSettings settings = new SettingsBuilder()
	      .withCacheManager(new LruBitmapCache(this, PERCENTAGE_OF_CACHE)).build(this);
	    imageLoader = new ImageManager(this, settings);
	    
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
	
	@Deprecated
	public static final ImageManager getImageManager() {
	    return imageLoader;
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
