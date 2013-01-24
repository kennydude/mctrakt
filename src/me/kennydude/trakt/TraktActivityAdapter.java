package me.kennydude.trakt;

import me.kennydude.trakt.TraktItemAdapter.ViewHolder;
import me.kennydude.trakt.data.TraktActivity;

import org.lucasr.smoothie.ItemLoader;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import com.novoda.imageloader.core.model.ImageTagFactory;

public class TraktActivityAdapter extends ArrayAdapter<TraktActivity> {
	ImageTagFactory imageTagFactory;
	
	public TraktActivityAdapter(Context context) {
		super(context, 0);
		imageTagFactory = ImageTagFactory.getInstance(context, R.drawable.ic_downloading);
	}
	
	@Override
	public View getView( int pos, View convertView, ViewGroup parent ){
		TraktActivity ti = getItem(pos);
		ViewHolder holder;
		
		if(convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_row, null);
			holder = new ViewHolder(convertView);
			
			convertView.setTag(holder);
		} else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		/*
		ImageView iv = (ImageView)convertView.findViewById(R.id.icon);
		ImageTag tag = imageTagFactory.build(ti.who_icon);
		iv.setBackgroundResource(R.drawable.ic_downloading);
	    iv.setTag(tag);
	    TraktApplication.getImageManager().getLoader().load(iv);
	    */
		
		// Maybe offload this?
		String title = "";
		if(ti.what.equals("watch")){
			title = getContext().getString(R.string.activity_watching).replace("{user}", ti.who).replace("{title}", ti.item.title);
		} else if(ti.what.equals("watchlist")){
			title = getContext().getString(R.string.activity_watchlist).replace("{user}", ti.who).replace("{title}", ti.item.title);
		}
		
		holder.title.setText(title);
		holder.title.setSingleLine(false);
		
		return convertView;	
	}
	
	public static class LoaderBitmaps{
		public CacheableBitmapDrawable icon, image;
	}
	
	public Loader getNewLoader(Activity a){
		return new Loader(a);
	}

	public class Loader extends ItemLoader<Integer, LoaderBitmaps>{
		public BitmapLruCache mCache;
		Activity mActivity;
		
		public Loader(Activity a){
			this.mCache = TraktApplication.getInstance(a).getLruCache();
			mActivity = a;
		}

		@Override
		public void displayItem(View convertView, LoaderBitmaps result, boolean fromMemory) {
			if(result == null) return;
			
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.image.setImageDrawable(result.image);
			holder.icon.setImageDrawable(result.icon);
		}

		@Override
		public Integer getItemParams(Adapter adapter, int pos) {
			return pos;
		}

		@Override
		public LoaderBitmaps loadItem(Integer pos) {
			LoaderBitmaps r = new LoaderBitmaps();
			
			String url = getItem(pos).item.getSizedBanner(mActivity);
			CacheableBitmapDrawable wrapper = mCache.get(url);
	        if (wrapper == null) {
	            wrapper = mCache.put(url, Utils.loadImage(url));
	        }
	        r.image = wrapper;
	        
	        url = getItem(pos).who_icon;
			wrapper = mCache.get(url);
	        if (wrapper == null) {
	            wrapper = mCache.put(url, Utils.loadImage(url));
	        }
	        r.icon = wrapper;
			
			return r;
		}

		@Override
		public LoaderBitmaps loadItemFromMemory(Integer pos) {
			LoaderBitmaps r = new LoaderBitmaps();
			r.image = mCache.getFromMemoryCache( getItem(pos).item.getSizedBanner(mActivity) );
			if(r.image == null) return null;
			
			r.icon = mCache.getFromMemoryCache( getItem(pos).who_icon );
			if(r.icon == null) return null;
			
			return r;
		}
		
	}
}
