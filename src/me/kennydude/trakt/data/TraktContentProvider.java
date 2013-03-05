package me.kennydude.trakt.data;

import java.util.Date;
import java.util.List;

import android.os.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import me.kennydude.trakt.TraktApplication;
import me.kennydude.trakt.Utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * I keep the app's data under control and happy
 * 
 * @author kennydude
 *
 */
public class TraktContentProvider extends ContentProvider {
	private TraktDatabaseHelper mOpenHelper;
	SQLiteDatabase db;
	public static final String DB_NAME = "traktdb";
	
	private static final UriMatcher sUriMatcher = new UriMatcher(0);
	
	static{
		sUriMatcher.addURI("me.kennydude.trakt.data", "item/*", 1);
		sUriMatcher.addURI("me.kennydude.trakt.data", "trending/*", 2);
		sUriMatcher.addURI("me.kennydude.trakt.data", "search/*/*", 3);
		sUriMatcher.addURI("me.kennydude.trakt.data", "love/*", 4);
	};
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new TraktDatabaseHelper(
            getContext(),						// the application context
            DB_NAME,							// the name of the database)
            null,								// uses the default SQLite cursor
            TraktDatabaseHelper.DB_VERSION		// the version number
        );
		db = mOpenHelper.getWritableDatabase();
		return true;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Cursor fetchSummaryOfItem(String item){
		try{
			String[] parts = item.split("-");
			
			TraktItem ti = TraktItem.getFromNetwork(parts[0], parts[1], getContext());
			addItemToDb(ti.toJSONObject(), ti);
			
			MatrixCursor mx = new MatrixCursor(new String[]{
				"json", "id"
			});
			mx.addRow(new Object[]{
				ti.toJSONObject().toString(),
				ti.type.toTraktString() + "-" + ti.id
			});
			return mx;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void addItemToDb(JSONObject jo, TraktItem ti){
		try{
			String id = ti.type.toTraktString() + "-" + ti.id;
			
			try{
				db.execSQL("DELETE FROM `items` WHERE `id` = ? AND `type`= 'i' ", new String[]{ id });
			} catch(Exception e){e.printStackTrace();}
			
			ContentValues cv = new ContentValues();
			cv.put("json", jo.toString());
			cv.put("url", ti.url);
			cv.put("id", id);
			cv.put("type", "i");
			cv.put("time", new Date().getTime() / 1000);
			db.insert("items", null, cv);
		} catch(Exception e){}
	}
	
	public Cursor fetchList(String item){
		MatrixCursor mx = new MatrixCursor(new String[]{
			"json", "id"
		});
		try{
			try{
				db.delete("items", "id = ? AND type = \"l\"", new String[]{ item });
			} catch(Exception e){e.printStackTrace();}
			
			String urlItem = item.replace("{key}", TraktApplication.API_KEY);
			String url = "http://api.trakt.tv/" + urlItem;
			JSONArray ja = new JSONArray( Utils.getRemote(url, false, getContext()) );
			
			Log.d("trakt", "Return from network");
			
			JSONArray items = new JSONArray();
			
			// This is why it was slow. Trakt was sending 117 entries :|
			int max = Math.min(ja.length(), 20);
			for(int i = 0; i < max; i++){
				JSONObject jo = ja.getJSONObject(i);
				TraktItem ti = TraktItem.fromJSON(jo, true);
				
				addItemToDb(jo, ti);
				
				String id = ti.type.toTraktString() + "-" + ti.id;
				mx.addRow(new Object[]{
					ti.toJSONObject().toString(),
					id
				});
				
				items.put(id);
			}
			
			ContentValues cv = new ContentValues();
			cv.put("type", "l");
			cv.put("id", item);
			cv.put("json", items.toString());
			cv.put("time", new Date().getTime() / 1000);
			cv.put("url", "");
			db.insert("items", null, cv);
			
			Log.d("tcp", "List finisehd");
			
			return mx;
		} catch(Exception e){
			e.printStackTrace();
			return mx;
		}
	}
	
	public Cursor handleList(String url, String selection){
		Log.d("tcp", url);
		
		if(selection.equals("fresh")){
			return fetchList(url);
		}
		
		Cursor dX = db.rawQuery("SELECT * FROM `items` WHERE `id` = ? AND `type` = 'l'", new String[]{ url });
		if(dX.moveToFirst()){
			try{
				if(dX.getInt(dX.getColumnIndex("time")) < (new Date().getTime() / 1000) - (60 * 60 * 4)){
					Log.d("tcp", "New");
					return fetchList(url);
				}
				Log.d("tcp", "Returning cache");
				
				JSONArray ja = new JSONArray(dX.getString(dX.getColumnIndex("json")));
				String items = "";
				for(int i = 0; i < ja.length();  i++){
					items += " \"" + ja.getString(i) + "\" ,";
				}
				if(items.length() > 0){
					items = items.substring(0, items.length() - 2);
				}
				
				Cursor iX = db.rawQuery("SELECT * FROM `items` WHERE `id` IN (" + items + ") AND `type` = 'i'", null);
				return iX;
			} catch(Exception e){
				e.printStackTrace();
				return fetchList(url);
			}
		} else{
			Log.d("tcp", "New - db nothing");
			return fetchList(url);
		}
	}

	@Override
	public Cursor query(
	        Uri uri,
	        String[] projection,
	        String selection,
	        String[] selectionArgs,
	        String sortOrder) {
		if(selection == null){
			selection = "";
		}
		
		switch(sUriMatcher.match(uri)){
		case 4: // content://../love/movies
			String u = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("trakt-username", "INVALID");
			return handleList("user/ratings/"+uri.getLastPathSegment()+".json/{key}/"+u+"/love/full", selection);
		case 3: // content://../search/movies/the+world+ends+now
			List<String> path = uri.getPathSegments();
			Log.d("tcp", uri.getPath());
			return handleList("search/" + path.get(path.size() - 2) + ".json/{key}/" + path.get(path.size() - 1), selection);
		case 2: // content://../trending/movies
			return handleList(uri.getLastPathSegment() + "/trending.json/{key}", selection);
		case 1: // content://../item/show-tt1520211
			// Get a specific item
			if(selection.equals("fresh")){
				// Need a fresh one!
				return fetchSummaryOfItem(uri.getLastPathSegment());
			}
			
			Cursor dX = db.rawQuery("SELECT * FROM `items` WHERE `id` =  ? AND `type`='i'", new String[]{ uri.getLastPathSegment() });
			if(dX.moveToFirst()){
				if(dX.getInt(dX.getColumnIndex("time")) < (new Date().getTime() / 1000) - (60 * 60 * 4)){
					// Need a fresh one
					Log.d("tcp-i", "fresh");
					return fetchSummaryOfItem(uri.getLastPathSegment());
				}
				
				Log.d("tcp-i", "old");
				return dX;
			} else{
				Log.d("tcp-i", "fresh");
				return fetchSummaryOfItem(uri.getLastPathSegment());
			}
		}
		
		return null;
	}

	@Override
	public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch(sUriMatcher.match(uri)){
		case 1: // content://../item/show-t2892u843
			db.update("items", values, "`id` = ?", new String[]{ uri.getLastPathSegment() });
			return 1;
		}
		return 0;
	}
	
	@Override
	public void shutdown (){
		db.close();
	}

}
