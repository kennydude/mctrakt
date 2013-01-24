package me.kennydude.trakt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Utils {
	
	public static final String[] BANNED_TERMS = {
		"the", "a"
	};

	public static String encode(String query) {
		return Uri.encode(query.replace(" ", "+"), "+");
	}
	
	// how similar is needle to haystack
	public static int percentSimilar( String needle, String haystack ){
		if(needle.equals(haystack)){
			return 100;
		}
		if(haystack.contains(needle)){
			return (haystack.length() - haystack.indexOf(needle)) / haystack.length() * 100;
		}
		
		int r = 0;
		
		String[] words = needle.split(" ");
		int bonusPoints = 0, i = 0;
		for(String word : words){
			boolean banned = false;
			for(String ban : BANNED_TERMS){
				if(ban.equals(word)){
					banned = true;
				}
			}
			
			if( haystack.contains(word) && !banned ){
				if(i < words.length - 1){
					if( haystack.indexOf(words[i+1]) == haystack.indexOf(word) + word.length() + 1 ){
						if(bonusPoints == 0){ bonusPoints = 1; }
						bonusPoints *= 2;
					} else{
						bonusPoints = 0;
					}
				}
				r += 1 + bonusPoints;
			}
			i++;
		}
		
		if(r >= 100){
			r = 90;
		}
		
		return r;
	}
	
	public static String getRemote(String url, boolean cached, Context c) throws Exception{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		if(sp.contains("trakt-username")){
			return postAuthedJSON(c, url, null, cached);
		}
		
		if(cached){
			String ca = CacheManager.getCacheItem(c, url, 60 * 60 * 12);
			if(ca != null){
				return ca;
			}
		}
		
		HttpGet p = new HttpGet(url);
		
		HttpResponse res = new DefaultHttpClient().execute(p);
		String r = EntityUtils.toString( res.getEntity());
		if(res.getStatusLine().getStatusCode() == 200)
			CacheManager.saveCache(c, url, r);
		return r;
	}
	
	public static String postAuthedJSON(Context c, String url, JSONObject post, boolean cached) throws Exception{
		if(cached){
			String ca = CacheManager.getCacheItem(c, url, 60 * 60 * 12);
			if(ca != null){
				return ca;
			}
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		if(post == null) post = new JSONObject();
		post.put("username", sp.getString("trakt-username", "INVALID"));
		post.put("password", sp.getString("trakt-password", "INVALID"));
		
		return postRemoteJSON(url, post, cached, c);
	}
	
	public static String postRemoteJSON(String url, JSONObject post, boolean cached, Context c) throws Exception{
		if(cached){
			String ca = CacheManager.getCacheItem(c, url, 60 * 60 * 12);
			if(ca != null){
				return ca;
			}
		}
		
		HttpPost p = new HttpPost(url);
		p.setEntity(new StringEntity(post.toString()));
		
		HttpResponse res = new DefaultHttpClient().execute(p);
		String r = EntityUtils.toString( res.getEntity());
		if(res.getStatusLine().getStatusCode() == 200)
			CacheManager.saveCache(c, url, r);
		return r;
	}
	
    private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 
 
    public static String SHA1(String text) 
	    throws Exception  { 
	    MessageDigest md;
	    md = MessageDigest.getInstance("SHA-1");
	    byte[] sha1hash = new byte[40];
	    md.update(text.getBytes("iso-8859-1"), 0, text.length());
	    sha1hash = md.digest();
	    return convertToHex(sha1hash);
    }

	public static InputStream loadImage(String url) {
		HttpURLConnection connection = null;
        InputStream is = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(15000);

            is = new BufferedInputStream(connection.getInputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return is;
	} 

}
