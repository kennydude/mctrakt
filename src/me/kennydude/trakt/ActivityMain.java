package me.kennydude.trakt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActivityMain extends BaseViewPagerActivity {

	@Override
	public void onCreate( Bundle bis ){
		super.onCreate(bis);
		
		addItem(TrendingMoviesFragment.class, R.string.trending_movies);
		addItem(TrendingShowsFragment.class, R.string.trending_shows);
		addItem(CommunityActivityFragment.class, R.string.community_activity);
		addItem(FriendsActivityFragment.class, R.string.friends_activity);
		addItem(FavouriteMoviesFragment.class, R.string.favourite_movies);
		addItem(FavouriteShowsFragment.class, R.string.favourite_shows);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		vpa.setCurrentItem(prefs.getInt("activitylastopened", 0));
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putInt("activitylastopened", vpa.getCurrentItem()).commit();
	}
	
	void hideLoading(){
    	if(refreshItem == null) return;
    	if(refreshItem.getActionView() != null){
    		refreshItem.getActionView().clearAnimation();
    		refreshItem.setActionView(null);
    	}
    }
    MenuItem refreshItem;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		hideLoading();
		new MenuInflater(this).inflate(R.menu.activity_main, menu);
		
		try{
			if(((ITraktFragment)sfpa.getLiveItem( vpa.getCurrentItem() )).isRefreshing()){
				refreshItem = menu.findItem(R.id.refresh);
	        	
	        	ImageView iv = (ImageView) LayoutInflater.from(this).inflate(R.layout.refresh_view, null);
	        	iv.setImageResource(R.drawable.ic_action_refresh);
	        	menu.findItem(R.id.refresh).setActionView(iv);
	        	
	        	Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);
	            rotation.setRepeatCount(Animation.INFINITE);
	            rotation.setRepeatMode(Animation.RESTART);
	            iv.startAnimation(rotation);
			}
		} catch(Exception e){}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.about:
			startActivity(new Intent(this, ActivityAbout.class));
			return true;
		case R.id.settings:
			startActivity(new Intent(this, ActivitySettings.class).putExtra("return", getIntent()));
			return true;
		case R.id.refresh:
			((ITraktFragment)sfpa.getLiveItem(vpa.getCurrentItem())).onRefreshPressed();
			return true;
		case R.id.search:
			onSearchRequested();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static abstract class TrendingFragment extends TraktItemFragment{

		@Override
		public String getUrl() {
			return "trending/" + getSection();
		}
		
		public abstract String getSection();
	}
	
	public static class TrendingMoviesFragment extends TrendingFragment{

		@Override
		public String getSection() {
			return "movies";
		}
	}

	public static class TrendingShowsFragment extends TrendingFragment{

		@Override
		public String getSection() {
			return "shows";
		}
	}
	
	public static class CommunityActivityFragment extends TraktActivityFragment{

		@Override
		public String getURL() {
			return "http://api.trakt.tv/activity/community.json/" + TraktApplication.API_KEY;
		}
		
		public boolean needsAuth(){
			return false;
		}
	}
	
	public static class FriendsActivityFragment extends TraktActivityFragment{

		@Override
		public String getURL() {
			return "http://api.trakt.tv/activity/friends.json/" + TraktApplication.API_KEY;
		}

	}
	
	public static class FavouriteMoviesFragment extends TraktItemFragment{
		public boolean needsAuth(){
			return true;
		}

		@Override
		public String getUrl() {
			return "love/movies";
		}
		
	}
	
	public static class FavouriteShowsFragment extends TraktItemFragment{
		public boolean needsAuth(){
			return true;
		}

		@Override
		public String getUrl() {
			return "love/shows";
		}
		
	}
	
}
