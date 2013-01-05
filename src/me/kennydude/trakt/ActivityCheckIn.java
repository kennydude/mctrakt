package me.kennydude.trakt;

import java.util.ArrayList;

import me.kennydude.trakt.data.TraktItem;
import me.kennydude.trakt.data.TraktItem.TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class ActivityCheckIn extends BaseViewPagerActivity {
	TraktCheckin checkin;

	@Override
	public void onCreate(Bundle bis){
		super.onCreate(bis);
		checkin = new TraktCheckin();
		
		if(getIntent().hasExtra("value")){
			try {
				checkin.item = TraktItem.fromJSON( new JSONObject( getIntent().getStringExtra("value") ) );
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else{
			finish();
		}
		
		TraktApplication.auth(this);
		
		// Shows!
		if(checkin.item.type == TYPE.TV_SHOW){
			addItem(SelectEpisodeFragment.class, R.string.select_episode);
		} else{
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		getActionBar().setSubtitle(checkin.item.title);
		
		addItem(CheckInDetailsFragment.class, R.string.details);
		
		vpa.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if(position == 1 && checkin.item.type == TYPE.TV_SHOW && checkin.episode == -1){
					vpa.setCurrentItem(0);
					((SelectEpisodeFragment)sfpa.getLiveItem(0)).showError();
					return;
				}
				
				getActionBar().setSelectedNavigationItem(position);
				invalidateOptionsMenu();
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
			
		});
	}
	
	void checkIn(){
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage(getString(R.string.please_wait));
		pd.show();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					JSONObject ci = new JSONObject();
					ci.put(checkin.item.idType, checkin.item.id);
					
					JSONObject share = new JSONObject();
					share.put("twitter", ((CheckBox)findViewById(R.id.shareTwitter)).isChecked());
					share.put("facebook", ((CheckBox)findViewById(R.id.shareFacebook)).isChecked());
					share.put("tumblr", ((CheckBox)findViewById(R.id.shareTumblr)).isChecked());
					ci.put("share", share);
					
					ci.put("app_version", TraktApplication.VERSION);
					
					String checkinItem = "show";
					if(checkin.item.type == TYPE.MOVIE){
						checkinItem = "movie";
					} else if(checkin.item.type == TYPE.TV_SHOW){
						ci.put("season", checkin.season);
						ci.put("episode", checkin.episode);
					}
					
					String url = "http://api.trakt.tv/" + checkinItem + "/checkin/" + TraktApplication.API_KEY;
					
					ci = new JSONObject( Utils.postAuthedJSON(ActivityCheckIn.this, url, ci, false) );
					if(!ci.optString("status").equals("success")){
						throw new Exception(ci.optString("error"));
					}
					
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							pd.dismiss();
							setResult(RESULT_OK);
							finish();
						}
						
					});
					
				} catch(final Exception e){
					e.printStackTrace();
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							pd.dismiss();
							TextView tv = (TextView) findViewById(R.id.errorCheckin);
							tv.setText(e.getMessage());
							tv.setVisibility(View.VISIBLE);
						}
						
					});
				}
			}
			
		}).start();
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch(item.getItemId()){
		case R.id.check_in:
			// Check in here!
			checkIn();
			return true;
		case R.id.refresh:
			((ITraktFragment)sfpa.getLiveItem(0)).onRefreshPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu u){
		new MenuInflater(this).inflate(R.menu.activity_check_in, u);
		
		if(checkin.item.type == TYPE.TV_SHOW){
			if(vpa.getCurrentItem() == 0){
				u.findItem(R.id.check_in).setVisible(false);
				u.findItem(R.id.refresh).setVisible(true);
				try{
					if(((ITraktFragment)sfpa.getLiveItem(0)).isRefreshing()){
						ProgressBar pv = new ProgressBar(this, null,
								android.R.attr.progressBarStyleSmall);
						u.findItem(R.id.refresh).setEnabled(false).setActionView(pv);
					}
				} catch(Exception e){}
			}
		}
		
		return true;
	}
	
	public static class SelectEpisodeFragment extends Fragment implements ITraktFragment{
		public class Season{
			public int number;
			public int episodes;
			
			public String toString(){
				if(number == 0){
					return getString(R.string.season_specials).replace("{eps}", episodes+"");
				}
				return getString(R.string.season).replace("{season}", number+"").replace("{eps}", episodes+"");
			}
		}
		private ArrayAdapter<Season> seasonAdapter;
		TraktItemAdapter episodeAdapter;
		
		public ActivityCheckIn getA(){
			return (ActivityCheckIn) getActivity();
		}
		boolean loading = false;
		
		public void setRefreshing(boolean is){
			getView().findViewById(R.id.progress).setVisibility(is ? View.VISIBLE : View.GONE);
			loading = true;
			getA().invalidateOptionsMenu();
		}
		
		public void refresh() {
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						JSONArray ja = new JSONArray(Utils.getRemote("http://api.trakt.tv/show/seasons.json/" + TraktApplication.API_KEY + "/" + getA().checkin.item.id, true, getActivity()));
						final ArrayList<Season> seasons = new ArrayList<Season>();
						for(int i = 0; i < ja.length(); i++){
							Season s = new Season();
							JSONObject jo = ja.getJSONObject(i);
							s.number = jo.getInt("season");
							s.episodes = jo.getInt("episodes");
							seasons.add(s);
						}
						
						getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								Spinner s = (Spinner) getView().findViewById(R.id.spinner);
								seasonAdapter = new ArrayAdapter<Season>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1, seasons  );
								seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								s.setAdapter(seasonAdapter);
							}
							
						});
						
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
		}

		public void showError() {
			getView().findViewById(R.id.error).setVisibility(View.VISIBLE);
		}

		@Override
		public final View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			return inflater.inflate(R.layout.fragment_select_episode, null);
		}
		
		void loadSeason(final int season){
			episodeAdapter.clear();
			getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
			
			getA().checkin.season = season;
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						JSONArray ja = new JSONArray(
								Utils.getRemote("http://api.trakt.tv/show/season.json/" +
								TraktApplication.API_KEY + "/" +
								getA().checkin.item.id + "/" + season, true, getActivity()));
						for(int i = 0; i < ja.length(); i++ ){
							final TraktItem ti = TraktItem.fromEpisodeJSON(ja.getJSONObject(i));
							getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									episodeAdapter.add( ti );
								}
								
							});
						}
						getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								getView().findViewById(R.id.progress).setVisibility(View.GONE);
							}
							
						});
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
		}
		
		public void onStart(){
			super.onStart();
			
			Spinner s = (Spinner) getView().findViewById(R.id.spinner);
			s.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {
					loadSeason(seasonAdapter.getItem(pos).number);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {}
				
			});
			
			ListView lv = (ListView) getView().findViewById(R.id.list);
			episodeAdapter = new TraktItemAdapter(getActivity());
			lv.setAdapter(episodeAdapter);
			lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {
					episodeAdapter.tickId = episodeAdapter.getItem(pos).id;
					episodeAdapter.notifyDataSetChanged();
					
					getView().findViewById(R.id.error).setVisibility(View.GONE);
					getA().checkin.episode = Integer.parseInt(episodeAdapter.getItem(pos).id);
					getA().checkin.episodeTitle = episodeAdapter.getItem(pos).title;
					
					((CheckInDetailsFragment)getA().sfpa.getLiveItem(1)).setMessage();
					getA().vpa.setCurrentItem(1);
				}
				
			});
			
			refresh();
			
		}

		@Override
		public void onRefreshPressed() {
			refresh();
		}

		@Override
		public boolean isRefreshing() {
			return loading;
		}
		
	}
	
	public static class CheckInDetailsFragment extends Fragment{
		public ActivityCheckIn getA(){
			return (ActivityCheckIn) getActivity();
		}
		
		@Override
		public final View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
			return inflater.inflate(R.layout.fragment_check_in, null);
		}
		
		public void setMessage(){
			EditText ed = (EditText)getView().findViewById(R.id.text);
			String s = getString(R.string.default_checkin_message);
			if(getA().checkin.item.type == TYPE.TV_SHOW){
				s = getString(R.string.default_checkin_message_show);
				s = s.replace("{epNo}", getA().checkin.episode+ "").replace("{seasonNo}", getA().checkin.season + "");
				s = s.replace("{episode}", getA().checkin.episodeTitle);
			}
			ed.setText(s.replace("{show}", getA().checkin.item.title));
		}
		
		public void onStart(){
			super.onStart();
			
			// Add custom "share to" services. These will be told when
			// we have checked in
			Intent i = new Intent( TraktApplication.CHECKIN_BROADCAST_ACTION );
			PackageManager pm = getActivity().getPackageManager();
			LinearLayout l = (LinearLayout) getView().findViewById(R.id.layout);
			
			for(ResolveInfo ri : pm.queryBroadcastReceivers(i, 0)){
				CheckBox cb = new CheckBox(getActivity());
				cb.setText(getString(R.string.share_custom).replace("{app}", ri.loadLabel(pm)));
				// TODO: Set method of finding this again
				
				l.addView(cb, 1, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				));
			}
			
			setMessage();
		}
		
	}
	
}
