package com.nxr.tpad.hapticcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

public class ColorButton extends Button {

	public int buttonId;
	public int color;
	private Boolean selected;
	private Paint paint;
	private Bitmap check;

	public ColorButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ColorButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.WHITE);
		selected = false;
		check = BitmapFactory.decodeResource(getResources(), R.drawable.icon_checkmark);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas c) {
		if (selected){
			c.drawBitmap(check, 0, 0, paint);
		}
		else{
			c.drawARGB(0, 0, 0, 0);
		}
		super.onDraw(c);

	}

	public void setSelect(Boolean bool) {

		selected = bool;
		invalidate();
	}

}
