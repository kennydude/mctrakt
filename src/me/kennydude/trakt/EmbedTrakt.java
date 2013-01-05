package me.kennydude.trakt;

import java.net.HttpURLConnection;
import java.net.URL;

import me.kennydude.trakt.data.TraktItem;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.novoda.imageloader.core.util.DirectLoader;
import com.teamboid.twitter.tweetwidgets.TweetWidget;

public class EmbedTrakt extends TweetWidget {
	
	public void makeWidget(final Context cntxt, final Intent ointent, String url, String type){
		// Get correct part
		String[] parts = url.split("/");
		int status = 0;
		for(String part : parts){
			if(status == 1){
				url = part;
				break;
			} else if(part.equals(type)){
				status = 1;
			}
		}
		
		final TraktItem ti = TraktItem.getFromNetwork(type, url, cntxt);
		if(ti == null){
			sendErrorView(cntxt, ointent);
			return;
		}
		
		Bitmap bitmap = null;
		try{
			bitmap = new DirectLoader().download(ti.getBanner());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		RemoteViews rv = new RemoteViews(cntxt.getPackageName(), R.layout.item_embed);
		if(bitmap != null)
			rv.setImageViewBitmap(R.id.image, bitmap);
		rv.setTextViewText(R.id.title, ti.title);
		
		Intent intent = new Intent(cntxt, ActivityCheckIn.class);
		intent.putExtra("value", ti.toJSONObject().toString());
		rv.setOnClickPendingIntent(R.id.check_in, PendingIntent.getActivity(cntxt, 0, intent, 0));
		
		Intent vIntent = new Intent(cntxt, ActivityItemView.class);
		vIntent.putExtra("value", ti.toJSONObject().toString());
		rv.setOnClickPendingIntent(R.id.details, PendingIntent.getActivity(cntxt, 0, vIntent, 0));
		
		sendRemoteViews(rv, cntxt, ointent);
	}

	@Override
	public void onReceive(Context cntxt, Intent intent) {
		String url = intent.getDataString();
		if(url.contains("go.trakt.tv")){
			url = url.replace("www.", "");
			try{
				URL go = new URL(url);
				HttpURLConnection urlConnection = (HttpURLConnection) go.openConnection();
				urlConnection.setInstanceFollowRedirects(false);
				urlConnection.connect();
				
				url = urlConnection.getHeaderField("Location");
				Log.d("u", url);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// Now we make the widget
		if(url.contains("/show")){
			makeWidget(cntxt, intent, url, "show");
		} else if(url.contains("/movie")){
			makeWidget(cntxt, intent, url, "movie");
		} else{
			sendErrorView(cntxt, intent);
		}
	}

}
