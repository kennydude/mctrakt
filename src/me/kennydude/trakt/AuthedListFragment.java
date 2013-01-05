package me.kennydude.trakt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class AuthedListFragment extends AuthedFragment {
	@Override
	public void onActivityCreated(Bundle bis){
		super.onActivityCreated(bis);
		if(getListView() == null) return;
		
		getListView().setEmptyView( getView().findViewById(R.id.empty) );
		setListShown(false);
	}
	
	public ListAdapter getListAdapter(){
		ListView lv = getListView();
		if(lv == null) return null;
		return lv.getAdapter();
	}
	
	public void setListAdapter(ListAdapter a){
		getListView().setAdapter(a);
	}
	
	
	@Override
	public View makeView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.list_content, null);
	}
	
	public void showError(String error){
		setListShown(true);
	}
	
	public ListView getListView(){
		if(getView() == null) return null;
		return (ListView) getView().findViewById(android.R.id.list);
	}
	
	public void setListShown(boolean shown) {
		boolean animate = true;
		if(getView() == null) return;
		
		View mProgressContainer = getView().findViewById(R.id.progressContainer);
        View mListContainer = getView().findViewById(R.id.listContainer);
		
		if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
	}
	
}
