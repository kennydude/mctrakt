package me.kennydude.trakt;

import me.kennydude.trakt.data.TraktItem;

import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView;

import org.lucasr.smoothie.ItemLoader;
import uk.co.senab.bitmapcache.*;

public class TraktItemAdapter extends ArrayAdapter<TraktItem> implements AbsListView.OnItemClickListener {
	public String tickId = null;
	ImageTagFactory imageTagFactory;
	
	public TraktItemAdapter(Activity context) {
		super(context, 0);
		imageTagFactory = ImageTagFactory.getInstance(context, R.drawable.ic_downloading);
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		
		imageTagFactory.usePreviewImage(outMetrics.widthPixels, outMetrics.densityDpi * 100, true);
		imageTagFactory.setSaveThumbnail(true);
	}
	
	public static class ViewHolder{
		public ViewHolder(View convertView){
			icon = (ImageView)convertView.findViewById(R.id.icon);
			image = (ImageView)convertView.findViewById(R.id.image);
			title = (TextView) convertView.findViewById(R.id.title);
		}
		
		ImageView icon;
		TextView title;
		ImageView image;
	}
	
	@Override
	public View getView( int pos, View convertView, ViewGroup parent ){
		TraktItem ti = getItem(pos);
		ViewHolder holder;
		
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_row, null);
			holder = new ViewHolder(convertView);
			
			convertView.setTag(holder);
		} else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.title.setText( ti.title );
		
		holder.image.setImageDrawable(null);
		
		if(tickId == null || !tickId.equals(ti.id)){
			holder.icon.setVisibility(View.GONE);
		} else{
			Log.d("trakt", "showing glyph");
			holder.icon.setVisibility(View.VISIBLE);
		}
		
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
	
	public static class Loader extends ItemLoader<String, CacheableBitmapDrawable>{
		public BitmapLruCache mCache;
		Activity mActivity;
		
		public Loader(Activity a){
			this.mCache = TraktApplication.getInstance(a).getLruCache();
			mActivity = a;
		}

		@Override
		public void displayItem(View view, CacheableBitmapDrawable result,
				boolean fromMemory) {
			if(result == null) return;
			
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.image.setImageDrawable(result);
		}

		@Override
		public String getItemParams(Adapter ad, int pos) {
			return ((TraktItem)ad.getItem(pos)).getSizedBanner(mActivity);
		}

		@Override
		public CacheableBitmapDrawable loadItem(String url) {
			CacheableBitmapDrawable wrapper = mCache.get(url);
	        if (wrapper == null) {
	            wrapper = mCache.put(url, Utils.loadImage(url));
	        }

	        return wrapper;
		}

		@Override
		public CacheableBitmapDrawable loadItemFromMemory(String url) {
			return mCache.getFromMemoryCache(url);
		}
		
	}

}
