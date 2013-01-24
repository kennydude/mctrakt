package me.kennydude.trakt;

import me.kennydude.trakt.data.TraktItem;
import me.kennydude.trakt.data.TraktItem.RATING;
import me.kennydude.trakt.data.TraktItemExtra;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.senab.bitmapcache.NetworkedCacheableImageView;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class ActivityItemView extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	TraktItem ti;
	
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_item);
		setEnabled(false);
		
		if(getIntent().hasExtra("value")){
			getLoaderManager().initLoader(0, null, this);
		} else{
			try{
				String url = getIntent().getDataString(), type = "", link = "";
				if(url.contains("/show")){
					type = "show";
				} else if(url.contains("/movie")){
					type = "movie";
				}
				
				// Get correct part
				String[] parts = url.split("/");
				int status = 0;
				for(String part : parts){
					if(status == 1){
						link = part;
						break;
					} else if(part.equals(type)){
						status = 1;
					}
				}
				
				getIntent().putExtra("value", type + "-" + link);
				getLoaderManager().initLoader(0, null, this);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		setRater(R.id.totallyNinja, RATING.TOTALLY_NINJA);
		setRater(R.id.weakSauce, RATING.WEAK_SAUCE);
		
		View cr = findViewById(R.id.currentlyRated);
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
	
	void setCheckbox(int id, final String what){
		CheckBox cb = (CheckBox) findViewById(id);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				setCheckboxEnabled(false);
				new Thread(new Runnable(){

					@Override
					public void run() {
						try{
							String prefix = "";
							if(what.equals("library") && ti.in_collection){
								prefix = "un";
							} else if(what.equals("watchlist") && ti.in_watchlist){
								prefix = "un";
							} else if(what.equals("seen") && ti.watched){
								prefix = "un";
							}
							
							String url = "http://api.trakt.tv/" + ti.type.toTraktString() + "/" + prefix + what + "/" + TraktApplication.API_KEY;
							Log.d("trakt", url);
							
							JSONObject jo = new JSONObject();
							
							JSONArray items = new JSONArray();
							JSONObject item = new JSONObject();
							item.put(ti.idType, ti.id);
							item.put("title", ti.title);
							items.put(item);
							
							jo.put(ti.type.toTraktString() + "s", items);
							
							jo = new JSONObject(Utils.postAuthedJSON(ActivityItemView.this, url, jo, false) );
							if(jo.optString("status").equals("success")){
								
								// Set it
								if(what.equals("library")){
									ti.in_collection = !ti.in_collection;
								} else if(what.equals("watchlist")){
									ti.in_watchlist = !ti.in_watchlist;
								} else if(what.equals("seen")){
									ti.watched = !ti.watched;
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
	
	private void setEnabled(boolean b) {
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
				if(TraktApplication.auth(ActivityItemView.this)){
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
					String url = "http://api.trakt.tv/rate/" + ti.type.toTraktString() + "/" + TraktApplication.API_KEY;
					JSONObject jo = new JSONObject();
					jo.put(ti.idType, ti.id);
					jo.put("rating", what.toTraktString());
				
					jo = new JSONObject( Utils.postAuthedJSON(ActivityItemView.this, url, jo, false) );
					if(!jo.optString("status").equals("success")){
						throw new Exception("Trakt says no");
					}
					
					ti.my_rating = what;
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
			cv.put("json", ti.toJSONObject().toString());
			Log.d("aiv", getContentResolver().update(Uri.parse("content://me.kennydude.trakt.data/item/" + getIntent().getStringExtra("value")), cv, null, null) + "");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		
		if(resultCode == RESULT_OK){
			
			final TextView tv = (TextView) findViewById(R.id.info);
			tv.setText(R.string.all_checked_in);
			tv.setAlpha(1);
			tv.setVisibility(View.VISIBLE);
			tv.animate().setStartDelay(3000).setListener(new AnimatorListener(){

				@Override public void onAnimationCancel(Animator arg0) {}
				@Override public void onAnimationRepeat(Animator arg0) {}
				@Override public void onAnimationStart(Animator arg0) {}

				@Override
				public void onAnimationEnd(Animator arg0) {
					tv.setVisibility(View.GONE);
				}
				
			}).alpha(0);
			
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case android.R.id.home:
			Intent x = new Intent(this, ActivityMain.class);
			x.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(x);
			return true;
		case R.id.refresh:
			getLoaderManager().restartLoader(0, new Bundle(), this);
			return true;
		case R.id.check_in:
			if(TraktApplication.auth(this)){
				Intent i = new Intent(this, ActivityCheckIn.class);
				i.putExtra("value", ti.toJSONObject().toString());
				startActivityForResult(i, 1);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	Intent getShareIntent(){
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ti.url);
        return shareIntent;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		if(ti != null){
			new MenuInflater(this).inflate(R.menu.activity_item, menu);
			
			MenuItem share = menu.findItem(R.id.share);
	        ShareActionProvider actionProvider = (ShareActionProvider) share.getActionProvider();
	        
	        Intent shareIntent = getShareIntent();
	        actionProvider.setShareIntent(shareIntent);
		}
        
		return true;
	}
	
	void setImage(int id, String url){
		NetworkedCacheableImageView iv = (NetworkedCacheableImageView)findViewById(id);
		iv.loadImage(url, false);
	}
	
	void renderView(){
		setEnabled(true);
		setTitle(ti.title);
		
		setImage(R.id.image, ti.getSizedBanner(this));
		setImage(R.id.poster, ti.images.get("poster"));
		
		TextView tv = (TextView) findViewById(R.id.description);
		tv.setText(ti.overview);
		
		tv = (TextView) findViewById(R.id.score);
		tv.setText(ti.rating + "%");
		
		if(ti.extra != null){
			findViewById(R.id.info).setVisibility(View.GONE);
			// render extra
			
			if(ti.extra.people != null){
				ViewGroup people = (ViewGroup) findViewById(R.id.people);
				for(TraktItemExtra.Person person : ti.extra.people){
					View peopleV = LayoutInflater.from(this).inflate(R.layout.item_person, null);
					
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
		invalidateOptionsMenu();
	}
	
	
	void renderRating(){
		Button currentlyRated = (Button)findViewById(R.id.currentlyRated);
		currentlyRated.setVisibility(ti.my_rating == RATING.NOT_RATED ? View.GONE : View.VISIBLE);
		
		ImageButton ib = (ImageButton)findViewById(R.id.totallyNinja);
		ib.setVisibility(ti.my_rating == RATING.NOT_RATED ? View.VISIBLE : View.GONE);
		ib = (ImageButton)findViewById(R.id.weakSauce);
		ib.setVisibility(ti.my_rating == RATING.NOT_RATED ? View.VISIBLE : View.GONE);
		
		if(ti.my_rating == RATING.TOTALLY_NINJA){
			currentlyRated.setText(R.string.currently_ninja);
		} else if(ti.my_rating == RATING.WEAK_SAUCE){
			currentlyRated.setText(R.string.currently_weak);
		}
		
		CheckBox cb = (CheckBox)findViewById(R.id.in_collection);
		cb.setChecked( ti.in_collection );
		
		cb = (CheckBox)findViewById(R.id.in_watchlist);
		cb.setChecked( ti.in_watchlist );
		
		cb = (CheckBox)findViewById(R.id.seen_it);
		cb.setChecked( ti.watched );
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		CursorLoader cl = new CursorLoader(this);
		cl.setUri(Uri.parse("content://me.kennydude.trakt.data/item/" + getIntent().getStringExtra("value")));
		if(bundle != null){
			cl.setSelection("fresh");
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		try{
			data.moveToFirst();
			ti = TraktItem.fromJSON( new JSONObject( data.getString( data.getColumnIndex("json") )  ));
			if(ti.extra == null){
				getLoaderManager().restartLoader(0, new Bundle(), this);
			}
			renderView();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
