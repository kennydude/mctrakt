package me.kennydude.trakt;

import me.kennydude.trakt.data.TraktActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lucasr.smoothie.ItemManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public abstract class TraktActivityFragment extends AuthedListFragment implements ITraktFragment {
	public boolean refreshing = false;
	private TraktActivityAdapter tia;
	
	public void setRefreshing(boolean is){
		refreshing = is;
		getActivity().invalidateOptionsMenu();
	}
	
	public boolean isRefreshing(){
		return refreshing;
	}


	
	@Override
	public void onActivityCreated(Bundle bis){
		super.onActivityCreated(bis);
		if(getListView() == null) return;
		if(getListView() != null){
			if(getListView().getAdapter() != null){
				return;
			}
		}
		
		tia = new TraktActivityAdapter(getActivity());
		getListView().setAdapter(tia);
		// getListView().setOnItemClickListener(tia);
		
		ItemManager.Builder builder = new ItemManager.Builder(tia.getNewLoader(getActivity()));
		
		builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
		builder.setThreadPoolSize(4);
		ItemManager itemManager = builder.build();
		getListView().setItemManager(itemManager);
		
		refresh(true);
	}
	
	public void refresh(){
		refresh(false);
	}
	
	protected void refresh(final boolean startR){
		if(tia == null) return;
		tia.clear();
		tia.notifyDataSetInvalidated();
		setListShown(false);
		setRefreshing(true);
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					
					JSONObject jo = new JSONObject(Utils.postAuthedJSON(getActivity(), getURL(), null, startR));
					JSONArray ja = jo.getJSONArray("activity");
					for( int i = 0; i < ja.length(); i++){
						final TraktActivity ti =  TraktActivity.fromJSON(ja.getJSONObject(i));
						getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								tia.add( ti );
							}
							
						});
					}
					
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							notifyRefreshComplete(startR);
							setRefreshing(false);
							setListShown(true);
							tia.notifyDataSetInvalidated();
						}
						
					});
					
					
				} catch(final Exception e){
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							notifyRefreshComplete(startR);
							setRefreshing(false);
							showError(e.getMessage());
						}
					});
				}
			}
			
		}).start();
	}
	
	public void notifyRefreshComplete(boolean startR) {
		// TODO Auto-generated method stub
		
	}
	
	public abstract String getURL();
	
	public void onRefreshPressed(){
		refresh(false);
	}

}
