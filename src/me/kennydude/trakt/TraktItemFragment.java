package me.kennydude.trakt;

import me.kennydude.trakt.data.TraktItem;

import org.json.JSONObject;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public abstract class TraktItemFragment extends AuthedListFragment implements ITraktFragment, LoaderManager.LoaderCallbacks<Cursor> {
	public boolean needsAuth(){
		return false;
	}

	public abstract String getUrl();
	
	protected TraktItemAdapter tia;
	boolean refreshing = false;
	
	public boolean isRefreshing(){
		return refreshing;
	}
	
	public void notifyRefreshComplete(boolean startR){};
	
	public void setRefreshing(boolean is){
		refreshing = is;
		getActivity().invalidateOptionsMenu();
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		if(getListView() == null) return;
		if(getListAdapter() != null){
			return;
		}
		
		tia = new TraktItemAdapter(getActivity());
		setListAdapter(tia);
		getListView().setOnItemClickListener(tia);
		
		refresh(true);
	}
	
	public void onRefreshPressed(){
		refresh(false);
	}
	
	public void refresh(boolean startP){
		setRefreshing(true);
		if(startP){
			getLoaderManager().initLoader(0, null, this);
		} else{
			getLoaderManager().restartLoader(0, new Bundle(), this);
		}
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int item, Bundle args) {
		CursorLoader cl = new CursorLoader(getActivity());
		cl.setUri(Uri.parse("content://me.kennydude.trakt.data/" + getUrl()));
		if(args != null){
			cl.setSelection("fresh");
		}
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, final Cursor data) {
		try{
			data.moveToPosition(-1);
			Log.d("tif", "Loader finished " + data.getCount());
			tia.clear();
			new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						while(data.moveToNext()){
							Log.d("tif", "+");
							final TraktItem ti = TraktItem.fromJSON( new JSONObject( data.getString( data.getColumnIndex("json") )  ));
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
								setListShown(true);
								notifyRefreshComplete(true);
								setRefreshing(false);
								tia.notifyDataSetInvalidated();
							}
							
						});
						
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
