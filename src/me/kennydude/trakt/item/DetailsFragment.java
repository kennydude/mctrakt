package me.kennydude.trakt.item;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.senab.bitmapcache.NetworkedCacheableImageView;

import me.kennydude.trakt.CheatSheet;
import me.kennydude.trakt.R;
import me.kennydude.trakt.TraktApplication;
import me.kennydude.trakt.Utils;
import me.kennydude.trakt.data.TraktItem;
import me.kennydude.trakt.data.TraktItemExtra;
import me.kennydude.trakt.data.TraktItem.RATING;
import android.app.Fragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DetailsFragment extends Fragment {
	public TraktItem getTraktItem(){
		return ((ActivityItemView)getActivity()).ti;
	}
	
	boolean is_automating = false;

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		return inflater.inflate(R.layout.activity_item, null);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		setEnabled(false);
		
		setRater(R.id.totallyNinja, RATING.TOTALLY_NINJA);
		setRater(R.id.weakSauce, RATING.WEAK_SAUCE);
		
		View cr = getView().findViewById(R.id.currentlyRated);
		cr.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRating(RATING.NOT_RATED);
			}
			
		});
		CheatSheet.setup(cr, getString(R.string.reset_rating));
		
		setCheckbox(R.id.in_collection, "library");
		setCheckbox(R.id.seen_it, "seen");
		setCheckbox(R.id.in_watchlist, "watchlist");
	}
	
	View findViewById(int i){ return getView().findViewById(i); }
	void runOnUiThread(Runnable r){ getActivity().runOnUiThread(r); }
	
	void setCheckbox(int id, final String what){
		CheckBox cb = (CheckBox) findViewById(id);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(is_automating) return; // Automated sets fire this
				
				setCheckboxEnabled(false);
				new Thread(new Runnable(){

					@Override
					public void run() {
						try{
							String prefix = "";
							if(what.equals("library") && getTraktItem().in_collection){
								prefix = "un";
							} else if(what.equals("watchlist") && getTraktItem().in_watchlist){
								prefix = "un";
							} else if(what.equals("seen") && getTraktItem().watched){
								prefix = "un";
							}
							
							String url = "http://api.trakt.tv/" + getTraktItem().type.toTraktString() + "/" + prefix + what + "/" + TraktApplication.API_KEY;
							Log.d("trakt", url);
							
							JSONObject jo = new JSONObject();
							
							JSONArray items = new JSONArray();
							JSONObject item = new JSONObject();
							item.put(getTraktItem().idType, getTraktItem().id);
							item.put("title", getTraktItem().title);
							items.put(item);
							
							jo.put(getTraktItem().type.toTraktString() + "s", items);
							
							jo = new JSONObject(Utils.postAuthedJSON(getActivity(), url, jo, false) );
							if(jo.optString("status").equals("success")){
								
								// Set it
								if(what.equals("library")){
									getTraktItem().in_collection = !getTraktItem().in_collection;
								} else if(what.equals("watchlist")){
									getTraktItem().in_watchlist = !getTraktItem().in_watchlist;
								} else if(what.equals("seen")){
									getTraktItem().watched = !getTraktItem().watched;
								}
								
								updateTraktItem();
							}
						} catch(Exception e){
							e.printStackTrace();
						}
						
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								setCheckboxEnabled(true);
								renderView();
							}
							
						});
					}
					
				}).start();
			}
			
		});
	}
	
	void setCheckboxEnabled(boolean b){
		View v = findViewById(R.id.in_collection);
		v.setEnabled(b);
		v = findViewById(R.id.seen_it);
		v.setEnabled(b);
		v = findViewById(R.id.in_watchlist);
		v.setEnabled(b);
	}
	
	private static final int[] EN_IDS = {
		R.id.totallyNinja, R.id.weakSauce, R.id.currentlyRated
	};
	
	public void setEnabled(boolean b) {
		for(int i : EN_IDS){
			findViewById(i).setEnabled(b);
		}
	}

	void setRater(int id, final RATING what){
		ImageButton ib = (ImageButton) findViewById(id);
		CheatSheet.setup(ib);
		ib.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(TraktApplication.auth(getActivity())){
					setRating(what);
				}
			}
			
		});
	}
	
	public void setRating(final RATING what){
		View v =findViewById(R.id.currentlyRated);
		v.setEnabled(false);
		v = findViewById(R.id.totallyNinja);
		v.setEnabled(false);
		v = findViewById(R.id.totallyNinja);
		v.setEnabled(false);
		v = findViewById(R.id.rating);
		v.setVisibility(View.VISIBLE);
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					String url = "http://api.trakt.tv/rate/" + getTraktItem().type.toTraktString() + "/" + TraktApplication.API_KEY;
					JSONObject jo = new JSONObject();
					jo.put(getTraktItem().idType, getTraktItem().id);
					jo.put("rating", what.toTraktString());
				
					jo = new JSONObject( Utils.postAuthedJSON(getActivity(), url, jo, false) );
					if(!jo.optString("status").equals("success")){
						throw new Exception("Trakt says no");
					}
					
					getTraktItem().my_rating = what;
					updateTraktItem();
					
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							View v =findViewById(R.id.currentlyRated);
							v.setEnabled(true);
							v = findViewById(R.id.totallyNinja);
							v.setEnabled(true);
							v = findViewById(R.id.totallyNinja);
							v.setEnabled(true);
							v = findViewById(R.id.rating);
							v.setVisibility(View.GONE);
							
							renderRating();
						}
						
					});
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}).start();
	}
	
	private void updateTraktItem() {
		try{
			ContentValues cv = new ContentValues();
			cv.put("json", getTraktItem().toJSONObject().toString());
			Log.d("aiv", getActivity().getContentResolver().update(Uri.parse("content://me.kennydude.trakt.data/item/" + getActivity().getIntent().getStringExtra("value")), cv, null, null) + "");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	void setImage(int id, String url){
		NetworkedCacheableImageView iv = (NetworkedCacheableImageView)findViewById(id);
		iv.loadImage(url, false);
	}
	
	void renderView(){
		setEnabled(true);
		
		setImage(R.id.image, getTraktItem().getSizedBanner(getActivity()));
		setImage(R.id.poster, getTraktItem().images.get("poster"));
		
		TextView tv = (TextView) findViewById(R.id.description);
		tv.setText(getTraktItem().overview);
		
		tv = (TextView) findViewById(R.id.score);
		tv.setText(getTraktItem().rating + "%");
		
		if(getTraktItem().extra != null){
			findViewById(R.id.info).setVisibility(View.GONE);
			// render extra
			
			if(getTraktItem().extra.people != null){
				ViewGroup people = (ViewGroup) findViewById(R.id.people);
				for(TraktItemExtra.Person person : getTraktItem().extra.people){
					View peopleV = LayoutInflater.from(getActivity()).inflate(R.layout.item_person, null);
					
					tv = (TextView) peopleV.findViewById(R.id.title);
					tv.setText(person.name);
					tv = (TextView) peopleV.findViewById(R.id.text);
					tv.setText( getString(person.job).replace("{extra}", person.job_extra) );
					
					people.addView(peopleV);
				}
			}
			
		} else{
			tv = (TextView) findViewById(R.id.info);
			tv.setText(R.string.few_bits_remain);
		}
		
		renderRating();
		getActivity().invalidateOptionsMenu();
	}
	
	
	void renderRating(){
		Button currentlyRated = (Button)findViewById(R.id.currentlyRated);
		currentlyRated.setVisibility(getTraktItem().my_rating == RATING.NOT_RATED ? View.GONE : View.VISIBLE);
		
		ImageButton ib = (ImageButton)findViewById(R.id.totallyNinja);
		ib.setVisibility(getTraktItem().my_rating == RATING.NOT_RATED ? View.VISIBLE : View.GONE);
		ib = (ImageButton)findViewById(R.id.weakSauce);
		ib.setVisibility(getTraktItem().my_rating == RATING.NOT_RATED ? View.VISIBLE : View.GONE);
		
		if(getTraktItem().my_rating == RATING.TOTALLY_NINJA){
			currentlyRated.setText(R.string.currently_ninja);
		} else if(getTraktItem().my_rating == RATING.WEAK_SAUCE){
			currentlyRated.setText(R.string.currently_weak);
		}
		
		is_automating = true;
		
		CheckBox cb = (CheckBox)findViewById(R.id.in_collection);
		cb.setChecked( getTraktItem().in_collection );
		
		cb = (CheckBox)findViewById(R.id.in_watchlist);
		cb.setChecked( getTraktItem().in_watchlist );
		
		cb = (CheckBox)findViewById(R.id.seen_it);
		cb.setChecked( getTraktItem().watched );
		
		is_automating = false;
	}
	
	public void itemReady(){
		
	}
	
}
