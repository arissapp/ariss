package com.jala.ariss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView{
	private Paint _paint = new Paint();
	private int xpos;
	private int ypos;
 
	public DrawView(Context context, int x, int y) {
		super(context);
		// Create out paint to use for drawing
		_paint.setARGB(255, 255, 0, 0);
		_paint.setStyle(Style.STROKE);
		_paint.setStrokeWidth(6);
		
		this.xpos = x;
		this.ypos = y;		
		
		// This call is necessary, or else the 
		// draw method will not be called. 
		setWillNotDraw(false);
		
	 }
		
	public void updateColor(int alpha, int r, int g, int b){
		int currentColor = _paint.getColor();
		int newColor = Color.argb(alpha, r, g, b);
		
		if(currentColor != newColor){
			_paint.setColor(newColor);
			this.invalidate();
		}
		
	}
	
	public void updatePosition(int x, int y){
		
		if(x != xpos || y != ypos){
			xpos = x;
			ypos = y;
			this.invalidate();
		}
	}
	
	public int getXPos(){
		return this.xpos;
	}
	
	public int getYPos(){
		return this.ypos;
	}
	
      @Override
	  protected void onDraw(Canvas canvas){
		 // A Simple Text Render to test the display
	     
	     canvas.drawCircle(this.xpos, this.ypos, 100, _paint);
	 }		

}
