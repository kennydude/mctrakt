package me.kennydude.trakt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import me.kennydude.trakt.data.TraktItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActivitySearch extends BaseViewPagerActivity {
	public String query;
	
	int waitingForRefresh = 2;
	public void notifyRefreshComplete(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				waitingForRefresh--;
				if( waitingForRefresh == 0){
					((SearchBestMatchFragment)sfpa.getLiveItem( 0 )).refresh(false);
				}
			}
			
		});
	}
	
	public ArrayList<TraktItem> getList(int p){
		TraktItemAdapter tia =  ((SearchFragment)sfpa.getLiveItem( p )).tia;
		ArrayList<TraktItem> r = new ArrayList<TraktItem>();
		for(int i = 0; i < tia.getCount(); i++){
			r.add( tia.getItem(i) );
		}
		return r;
	}

	@Override
	public void onCreate( Bundle bis ){
		super.onCreate(bis);
		
		if(getIntent().getAction().equals(Intent.ACTION_VIEW)){
			query = getIntent().getData().getQueryParameter("q");
		} else if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			query = getIntent().getStringExtra(SearchManager.QUERY);
		}
		
		setTitle( getString(R.string.search_title).replace("{query}", query) );
		// TODO: getActionBar().setCustomView(arg0)
		
		addItem(SearchBestMatchFragment.class, R.string.best_match);
		addItem(SearchMoviesFragment.class, R.string.movies);
		addItem(SearchShowsFragment.class, R.string.shows);
		
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.refresh:
			((ITraktFragment)sfpa.getLiveItem( vpa.getCurrentItem() )).onRefreshPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		new MenuInflater(this).inflate(R.menu.activity_search, menu);
		
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
	
	public abstract static class SearchFragment extends TraktItemFragment{
		
		public void notifyRefreshComplete(boolean startR){
			if(startR)
				((ActivitySearch)getActivity()).notifyRefreshComplete();
		}

		@Override
		public String getUrl(){
			String query = Utils.encode( ((ActivitySearch)getActivity()).query );
			return "search/" + getSection() + "/" + query;
		}
		
		public abstract String getSection();
		
	}
	
	public static class SearchBestMatchFragment extends SearchFragment{
		String query;
		
		class BestMatchComparator implements Comparator<TraktItem>{
			
			@Override
			public int compare(TraktItem lhs, TraktItem rhs) {
				
				int l = Utils.percentSimilar(query.toLowerCase(Locale.US), lhs.title.toLowerCase(Locale.US));
				int r = Utils.percentSimilar(query.toLowerCase(Locale.US), rhs.title.toLowerCase(Locale.US));
				
				System.out.println(query + ":- " + lhs.title + ": " + l + " vs " + r + ": " + rhs.title);
				
				if(l == r){
					return 0;
				} else if(l > r){
					return 1;
				} else{
					return -1;
				}
			}
			
		}
		
		@Override
		public void refresh(boolean startP){
			if(startP) return;
			if(getActivity() == null) return;
			
			query = ((ActivitySearch)getActivity()).query;
			
			tia.clear();
			tia.notifyDataSetInvalidated();
			setListShown(false);
			setRefreshing(true);
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					
					// Get lists
					try{
						ActivitySearch a = (ActivitySearch)getActivity();
						final ArrayList<TraktItem> ti = new ArrayList<TraktItem>();
						
						ti.addAll( a.getList(1) );
						ti.addAll( a.getList(2) );
						
						// Now sort
						Collections.sort(ti, new BestMatchComparator());
						Collections.reverse(ti);
						
						getActivity().runOnUiThread(new Runnable(){
							
							@Override
							public void run() {
								if(getActivity() == null) return;
								tia.addAll(ti);
								tia.notifyDataSetInvalidated();
								setListShown(true);
								setRefreshing(false);
							}
							
						});
						
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
		}

		@Override
		public String getSection() {
			return null;
		}
		
	}
	
	public static class SearchShowsFragment extends SearchFragment{

		@Override
		public String getSection() {
			return "shows";
		}
		
	}
	
	public static class SearchMoviesFragment extends SearchFragment{

		@Override
		public String getSection() {
			return "movies";
		}
		
	}
	
}
