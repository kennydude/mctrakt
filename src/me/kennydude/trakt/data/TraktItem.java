package me.kennydude.trakt.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import me.kennydude.trakt.TraktApplication;
import me.kennydude.trakt.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;

public class TraktItem {
	public static final String[] ID_TYPES = {
		"tvdb_id",
		"imdb_id",
		"tmdb_id"
	};
	public static enum TYPE{
		TV_SHOW, MOVIE;
		
		public String toTraktString(){
			if(this == TV_SHOW){
				return "show";
			} else if(this == MOVIE){
				return "movie";
			}
			return "";
		}
	}
	public static enum RATING{
		TOTALLY_NINJA, WEAK_SAUCE, NOT_RATED;
		
		public static RATING fromString(String i){
			if(i.equals("love")){
				return TOTALLY_NINJA;
			} else if(i.equals("hate")){
				return WEAK_SAUCE;
			} else{
				return NOT_RATED;
			}
		}
		public String toTraktString(){
			if(this == TOTALLY_NINJA){
				return "love";
			} else if(this == WEAK_SAUCE){
				return "hate";
			} else{
				return "false";
			}
		}
	};
	
	public TYPE type = TYPE.MOVIE;
	public String title, tagline, overview, url, id, idType;
	public HashMap<String, String> images;
	public int rating;
	public RATING my_rating = RATING.NOT_RATED;
	
	public boolean in_collection, in_watchlist, watched;
	
	public TraktItemExtra extra = null;
	
	public static TraktItem fromEpisodeJSON(JSONObject jo) throws JSONException{
		TraktItem ti = new TraktItem();
		ti.id = jo.optString("episode");
		ti.title = ti.id + " - " +  jo.optString("title");
		
		ti.images = new HashMap<String, String>();
		JSONObject im = jo.getJSONObject("images");
		@SuppressWarnings("unchecked")
		Iterator<String> key = im.keys();
		String k = "";
		while( key.hasNext() ){
			k = (String) key.next();
			ti.images.put( k, im.getString(k));
		}
		
		return ti;
	}
	
	public static TraktItem fromJSON(JSONObject jo) throws JSONException{
		return fromJSON(jo, false);
	}
	
	@SuppressWarnings("unchecked")
	public static TraktItem fromJSON(JSONObject jo, boolean fast) throws JSONException{
		TraktItem ti = new TraktItem();
		ti.title  = jo.getString("title");
		ti.tagline = jo.optString("tagline", "");
		ti.overview = jo.optString("overview", "");
		ti.url = jo.getString("url");
		if(ti.url.contains("/show")){
			ti.type = TYPE.TV_SHOW;
		}
		if(jo.has("ratings")){
			ti.rating = jo.getJSONObject("ratings").getInt("percentage");
		}
		
		ti.images = new HashMap<String, String>();
		
		if(!fast){
			JSONObject im = jo.getJSONObject("images");
			Iterator<String> key = im.keys();
			String k = "";
			while( key.hasNext() ){
				k = (String) key.next();
				ti.images.put( k, im.getString(k));
			}
		}
		
		if(jo.has("id_type")){
			ti.idType = jo.optString("id_type");
			ti.id = jo.optString("id");
		} else{
			for(String idType : ID_TYPES){
				if(jo.has(idType)){
					ti.idType = idType;
					ti.id = jo.optString(idType);
				}
			}
		}
		
		if(!fast){
			if(jo.has("type")){
				ti.type = TYPE.valueOf( jo.optString("type") );
			}
			if(jo.has("rating")){
				ti.my_rating = RATING.fromString(jo.optString("rating"));
			}
			
			if(jo.has("people")){
				ti.extra = TraktItemExtra.fromJSON(jo);
			}
		}
		
		ti.in_collection = jo.optBoolean("in_collection");
		ti.in_watchlist = jo.optBoolean("in_watchlist");
		ti.watched = jo.optBoolean("watched");
			
		return ti;
	}

	public String getBanner() {
		if(images.containsKey("banner")){
			return images.get("banner");
		} else if(images.containsKey("fanart")){
			return images.get("fanart");
		} else if(images.containsKey("poster")){
			return images.get("poster");
		} else if(images.size() > 0){
			return images.values().iterator().next();
		}
		return null;
	}
	
	public String getSizedBanner(Activity a){
		int width = TraktApplication.getScreenWidth(a);
		
		if(images.containsKey("banner")){
			return images.get("banner");
		} else if(images.containsKey("fanart")){
			String fArt = images.get("fanart");
			if(width <= 218){
				fArt = fArt.replace(".jpg", "-218.jpg");
			} else if(width <= 940){
				fArt = fArt.replace(".jpg", "-940.jpg");
			}
			return fArt;
		} else if(images.containsKey("poster")){
			return images.get("poster").replace(".jpg", "-300.jpg");
		} else if(images.size() > 0){
			return images.values().iterator().next();
		}
		return null;
	}

	public JSONObject toJSONObject() {
		try{
			JSONObject jo = new JSONObject();
			jo.put("title", title);
			jo.put("tagline", tagline);
			jo.put("overview", overview);
			jo.put("url", url);
			
			JSONObject im = new JSONObject();
			for(Entry<String, String> set : images.entrySet() ){
				im.put(set.getKey(), set.getValue());
			}
			jo.put("images", im);
			
			JSONObject r = new JSONObject();
			r.put("percentage", rating);
			jo.put("ratings", r);
			
			jo.put("id_type", idType);
			jo.put("id", id);
		
			jo.put("type", type.toString());
			jo.put("rating", my_rating.toTraktString());
			
			jo.put("in_watchlist", in_watchlist);
			jo.put("in_collection", in_collection);
			jo.put("watched", watched);
			
			if(extra != null){
				extra.applyToJSON(jo);
			}
			
			return jo;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static TraktItem getFromNetwork(String type, String link, Context c){
		try{
			String url = "http://api.trakt.tv/"+type+"/summary.json/5008de3b21fbdcdc5a5560502c424ec9/" + link;
			return TraktItem.fromJSON( new JSONObject( Utils.getRemote(url, true, c) ) );
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
