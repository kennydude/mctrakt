package me.kennydude.trakt.data;

import java.util.ArrayList;

import me.kennydude.trakt.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extra data that is only given with a full summary call
 * 
 * @author kennydude
 *
 */
public class TraktItemExtra {

	public ArrayList<Person> people;
	
	public static TraktItemExtra fromJSON(JSONObject jo){
		TraktItemExtra tia = new TraktItemExtra();
		
		if(jo.has("people")){
			tia.people = new ArrayList<Person>();
			JSONObject p = jo.optJSONObject("people");
			
			if(p.has("directors")){
				JSONArray a = p.optJSONArray("directors");
				for(int i = 0; i < a.length(); i++){
					Person director = new Person();
					director.job = R.string.job_director;
					director.name = a.optJSONObject(i).optString("name", "");
					tia.people.add(director);
				}
			}
			if(p.has("writers")){
				JSONArray a = p.optJSONArray("writers");
				for(int i = 0; i < a.length(); i++){
					Person writer = new Person();
					writer.job = R.string.job_writer;
					writer.name = a.optJSONObject(i).optString("name", "");
					writer.job_extra = a.optJSONObject(i).optString("job", "");
					tia.people.add(writer);
				}
			}
			if(p.has("producers")){
				JSONArray a = p.optJSONArray("producers");
				for(int i = 0; i < a.length(); i++){
					Person producer = new Person();
					if(a.optJSONObject(i).optBoolean("executive", false)){
						producer.job = R.string.job_executive_producer;
					} else{
						producer.job = R.string.job_producer;
					}
					producer.name = a.optJSONObject(i).optString("name", "");
					tia.people.add(producer);
				}
			}
			if(p.has("actors")){
				JSONArray a = p.optJSONArray("actors");
				for(int i = 0; i < a.length(); i++){
					Person actor = new Person();
					actor.job = R.string.job_actor;
					actor.job_extra = a.optJSONObject(i).optString("character", "");
					actor.name = a.optJSONObject(i).optString("name", "");
					tia.people.add(actor);
				}
			}
		}
		
		return tia;
	}
	
	public static class Person{
		public String name;
		public int job; // String
		public String job_extra = ""; 
	}
	
	void add(JSONObject in, String key, JSONObject item) throws JSONException{
		if(!in.has(key)){
			in.put(key, new JSONArray());
		}
		in.getJSONArray(key).put(item);
	}

	public void applyToJSON(JSONObject jo) throws JSONException {
		if(people != null){
			JSONObject po = new JSONObject();
			for(Person p : people){
				JSONObject a = new JSONObject();
				a.put("name", p.name);
				switch(p.job){
				case R.string.job_actor:
					a.put("character", p.job_extra);
					add(po, "actors", a);
					break;
				case R.string.job_executive_producer:
					a.put("executive", true);
				case R.string.job_producer:
					add(po, "producers", a);
					break;
				case R.string.job_director:
					add(po, "directors", a);
					break;
				case R.string.job_writer:
					a.put("job", p.job_extra);
					add(po, "writers", a);
					break;
				}
			}
			jo.put("people", po);
		}
	}
}
