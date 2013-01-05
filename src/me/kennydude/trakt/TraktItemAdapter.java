package me.kennydude.trakt;

import me.kennydude.trakt.data.TraktItem;

import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView;

public class TraktItemAdapter extends ArrayAdapter<TraktItem> implements AbsListView.OnItemClickListener {
	public String tickId = null;
	ImageTagFactory imageTagFactory;
	
	public TraktItemAdapter(Context context) {
		super(context, 0);
		imageTagFactory = ImageTagFactory.getInstance(context, R.drawable.ic_downloading);
	}
	
	@Override
	public View getView( int pos, View convertView, ViewGroup parent ){
		TraktItem ti = getItem(pos);
		
		convertView = applyView(convertView, imageTagFactory, ti, getContext());
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.icon);
		if(tickId == null || !tickId.equals(ti.id)){
			iv.setVisibility(View.GONE);
		} else{
			Log.d("trakt", "showing glyph");
			iv.setVisibility(View.VISIBLE);
			//iv.setImageResource(ti.glyph);
		}
		
		return convertView;	
	}
	
	public static View applyView(View convertView, ImageTagFactory itf, TraktItem ti, Context c){
		if(convertView == null){
			convertView = LayoutInflater.from(c).inflate(R.layout.item_row, null);
		}
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.image);
		ImageTag tag = itf.build(ti.getSizedBanner((Activity) c));
	    iv.setTag(tag);
	    TraktApplication.getImageManager().getLoader().load(iv);
		
		((TextView)convertView.findViewById(R.id.title)).setText(ti.title);
		
		return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> a, View arg1, int pos, long arg3) {
		TraktItem ti = getItem(pos);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse( ti.url ) );
		i.putExtra("value", ti.type.toTraktString() + "-" + ti.id);
		i.setPackage("me.kennydude.trakt"); // Don't push to web browser
		
		getContext().startActivity(i);
	}

}
