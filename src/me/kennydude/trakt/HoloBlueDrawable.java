package me.kennydude.trakt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class HoloBlueDrawable extends Drawable {
	Paint p;
	
	public HoloBlueDrawable(Context c){
		p = new Paint();
		p.setColor(c.getResources().getColor(android.R.color.holo_blue_dark));
		p.setAlpha(150);
	}

	@Override
	public void draw(Canvas cnv) {
		cnv.drawRect(0, 0, cnv.getWidth(), cnv.getHeight(), p);
	}

	@Override
	public int getOpacity() {
		return 80;
	}

	@Override
	public void setAlpha(int arg0) {}
	@Override
	public void setColorFilter(ColorFilter arg0) {}

}
