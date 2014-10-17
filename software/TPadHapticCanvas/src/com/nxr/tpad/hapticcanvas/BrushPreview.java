package com.nxr.tpad.hapticcanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BrushPreview extends View {

	private int height, width;
	private float rad;
	private Paint brush;

	public BrushPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		brush = new Paint();
		rad = 0;
		brush.setStrokeCap(Paint.Cap.ROUND);
		brush.setAntiAlias(true);
		setColor(Color.WHITE);
		brush.setAlpha(255);
		setWidth(10);

	}

	public void setColor(int color) {
		if (color == Color.TRANSPARENT) {

			brush.setColor(0xffdab9);
			brush.setAlpha(150);
		} else {
			brush.setAlpha(255);
			brush.setColor(color);
		}
		invalidate();

	}

	public void setWidth(int w) {
		rad = w;
		invalidate();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);

		Log.i("BrushPreview", "onMeasure called: " + String.valueOf(width) + " " + String.valueOf(height));
		this.setMeasuredDimension(width, height);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawARGB(0, 255, 255, 255);
		canvas.drawCircle(width / 2f, height / 2f, rad / 2, brush);

	}

}
