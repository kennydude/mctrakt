package me.kennydude.trakt;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class SimpleFragmentPagerAdapter extends FragmentStatePagerAdapter {
	List<Class<? extends Fragment>> items;
	FragmentManager mFragmentManager;
	Context mContext;

	public SimpleFragmentPagerAdapter(Activity fm) {
		super(fm.getFragmentManager());
		mFragmentManager = fm.getFragmentManager();
		items = new ArrayList<Class<? extends Fragment>>();
		mContext = fm;
	}
	
	public void addItem( Class<? extends Fragment> cls ){
		items.add(cls);
	}

	@Override
	public Fragment getItem(int position) {
		try {
			return items.get(position).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
	}
	
	public Fragment getLiveItem(int position){
		return mFragmentManager.findFragmentByTag( makeFragmentName(0, position) );
	}

	@Override
	public int getCount() {
		return items.size();
	}

	
	public String makeFragmentName(int viewId, long id) {
		return "page:" + id;
	}
}
