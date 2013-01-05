package me.kennydude.trakt;


import me.kennydude.trakt.data.TraktActivity;

import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TraktActivityAdapter extends ArrayAdapter<TraktActivity> {
	ImageTagFactory imageTagFactory;
	
	public TraktActivityAdapter(Context context) {
		super(context, 0);
		imageTagFactory = ImageTagFactory.getInstance(context, R.drawable.ic_downloading);
	}
	
	@Override
	public View getView( int pos, View convertView, ViewGroup parent ){
		TraktActivity ti = getItem(pos);
		
		convertView = TraktItemAdapter.applyView(convertView, imageTagFactory, ti.item, getContext());
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.icon);
		ImageTag tag = imageTagFactory.build(ti.who_icon);
		iv.setBackgroundResource(R.drawable.ic_downloading);
	    iv.setTag(tag);
	    TraktApplication.getImageManager().getLoader().load(iv);
		
		String title = "";
		if(ti.what.equals("watch")){
			title = getContext().getString(R.string.activity_watching).replace("{user}", ti.who).replace("{title}", ti.item.title);
		} else if(ti.what.equals("watchlist")){
			title = getContext().getString(R.string.activity_watchlist).replace("{user}", ti.who).replace("{title}", ti.item.title);
		}
		
		TextView tv = ((TextView)convertView.findViewById(R.id.title));
		tv.setText(title);
		tv.setSingleLine(false);
		
		return convertView;	
	}

}
