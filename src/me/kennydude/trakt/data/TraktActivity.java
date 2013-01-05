package me.kennydude.trakt.data;

import java.util.Date;


import org.json.JSONException;
import org.json.JSONObject;

public class TraktActivity {

	public TraktItem item;
	public Date when;
	public String who, who_icon, what, type;
	
	public static TraktActivity fromJSON(JSONObject jo) throws JSONException{
		TraktActivity ta = new TraktActivity();
		
		ta.type = jo.optString("type");
		ta.when = new Date(jo.optInt("timestamp"));
		if( jo.has(ta.type) ){
			ta.item = TraktItem.fromJSON(jo.optJSONObject(ta.type));
		}
		ta.who = jo.optJSONObject("user").optString("username");
		ta.who_icon = jo.optJSONObject("user").optString("avatar");
		ta.what = jo.optString("action");
		
		return ta;
	}
	
}
