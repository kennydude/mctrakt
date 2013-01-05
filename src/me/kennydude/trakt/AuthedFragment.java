package me.kennydude.trakt;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * I make sure you are logged in
 * 
 * @author kennydude
 *
 */
public abstract class AuthedFragment extends Fragment {

	public abstract View makeView(LayoutInflater inflater);
	public boolean needsAuth(){
		return true;
	}
	
	@Override
	public final View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		if(TraktApplication.canUseAuthenticatedMethods(this.getActivity()) || !needsAuth()){
			return makeView(inflater);
		} else{
			return inflater.inflate(R.layout.fragment_auth_req, null);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle bis){
		super.onActivityCreated(bis);
		if(!TraktApplication.canUseAuthenticatedMethods(getActivity()) && needsAuth()){
			if(getView().findViewById(R.id.login) != null){
				getView().findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						TraktApplication.auth(getActivity());
					}
					
				});
			}
		}
	}
}
