package me.kennydude.trakt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class BaseViewPagerActivity extends Activity implements ActionBar.TabListener  {
	public SimpleFragmentPagerAdapter sfpa;
	public ViewPager vpa;
	
	@Override
	public void onCreate( Bundle bis ){
		super.onCreate(bis);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		vpa = new ViewPager(this);
		vpa.setId(999);
		setContentView(vpa);
		
		sfpa = new SimpleFragmentPagerAdapter(this);
		vpa.setOffscreenPageLimit(3);
		vpa.setAdapter(sfpa);
		vpa.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
				invalidateOptionsMenu();
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
		sfpa.notifyDataSetChanged();
	}
	
	public Fragment getLiveItem(int it){
		return sfpa.getLiveItem(it);
	}
		
	public void addItem(Class<? extends Fragment> c, int s){
		sfpa.addItem(c); sfpa.notifyDataSetChanged();
		getActionBar().addTab( getActionBar().newTab().setText(s).setTabListener(this));
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		vpa.setCurrentItem(tab.getPosition());
		invalidateOptionsMenu();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
