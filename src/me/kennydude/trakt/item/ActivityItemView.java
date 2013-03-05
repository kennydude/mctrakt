package me.kennydude.trakt.item;

import me.kennydude.trakt.ActivityCheckIn;
import me.kennydude.trakt.ActivityMain;
import me.kennydude.trakt.BaseViewPagerActivity;
import me.kennydude.trakt.CheatSheet;
import me.kennydude.trakt.R;
import me.kennydude.trakt.TraktApplication;
import me.kennydude.trakt.Utils;
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

public class ActivityItemView extends BaseViewPagerActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	TraktItem ti;
	
	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		addItem(DetailsFragment.class, R.string.details);
		// addItem(ShoutFragment.class, R.string.shouts);
	}
	@Override
	public void onStart(){
		super.onStart();
		
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
			
			setTitle(ti.title);
			((DetailsFragment)getLiveItem(0)).renderView();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
