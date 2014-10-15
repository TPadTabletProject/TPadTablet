package nxr.tpad.comet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxr.tpadioio.lib.TPadTexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class DrawviewFragment extends Fragment {
	public OptionsFragment optionView;
	public ServerFragment serverView;
	public MainActivity mainActivity;
	
	public DrawView drawView;
    int feelLow = 0;
    int drawLow = 0;
    Boolean updateFeel = false;
    Boolean updateDraw = false;
	Boolean imtouching = false; 
	Boolean theyretouching = false;
	int time = 0;
	
	public List<Point> pointsPaint= new ArrayList<Point>();
    public List<Point> pointsFeel= new ArrayList<Point>();
    public double z = 0;
    
    public double zlast = (float)0.5;
    public double frictionlast = 0.5;
    TimeDecay decayThread;
	private static final int PREDICT_HORIZON = 30;//(int) (TPad.TextureSampleRate * (.020f)); // 10,000Hz
	private static float[] predictedPixels = new float[PREDICT_HORIZON];
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
          drawView = new DrawView(getActivity());
          
          //drawView.setBackgroundColor(Color.WHITE);
          
        return drawView;
    }
    public void addPoint(int a, int b) {
    	if(a == 0){
    		theyretouching = false; 
    	}else{
    		theyretouching = true;
    		Point p = new Point();
	    	p.x = a;
	    	p.y = b;
	    	p.color = 150;
	    	p.radius = drawView.otherRadius;
    		synchronized(pointsFeel){
    			pointsFeel.add(p);
    		}
    		Date date = new Date();
    		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss:SSS a");
    		String formattedDate = sdf.format(date);
	        mainActivity.writeToLog(formattedDate + "; othertouch; " + String.valueOf(p.x) + "; " + String.valueOf(p.y) + ";");

	    	updateFeel = true;
    	}
    	
    	drawView.invalidate();
    }
	public void setSelfDecay(int i) {
		drawView.selfDecay = i;
		drawView.calculateColorDecay();
	}
	public void setSelfRadius(int i) {
		drawView.selfRadius = i;
		drawView.calculateColorDecay();
	}
	public void setOtherDecay(int i) {
		drawView.otherDecay = i;
		drawView.calculateColorDecay();
	}
	public void setOtherRadius(int i) {
		drawView.otherRadius = i;
		drawView.calculateColorDecay();
	}
	public void toggleOwn(Boolean b) {
		drawView.showOwn = b;
		drawView.invalidate();
	}
	public void toggleOther(Boolean b) {
		drawView.showOther = b;
		drawView.invalidate();
	}
	public void toggleDecay(Boolean b) {
		decayThread.toggle(b);
	}
	public void clean() {
		pointsPaint= new ArrayList<Point>();
        pointsFeel= new ArrayList<Point>();
    	drawView.invalidate();
	}
    private class DrawView extends View implements OnTouchListener {
		Paint paint = new Paint();
		Paint blur = new Paint();
		
		private volatile Bitmap fingerPrint = null;
		private volatile Bitmap background = null;
		Point point = new Point();
		Point pp = new Point();
		Point ppoint = new Point();

	    Boolean showOwn = false;
	    Boolean showOther = false;
        double sendval = 0;
        
	    int selfDecay = 2;
	    int selfColorDecay;
	    int otherDecay = 2;
	    int otherColorDecay;
	    int selfRadius = 50; //This is the radius of the dot that it draws
	    int otherRadius = 50;
	    int size = 0;
	    
		private VelocityTracker vTracker;
	    
		public DrawView(Context context) {
			super(context);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inScaled = false;
			fingerPrint = BitmapFactory.decodeResource(getResources(),R.drawable.fing); 
			background  = BitmapFactory.decodeResource(getResources(),R.drawable.burst, opts); 

						
	        setFocusable(true);
	        setFocusableInTouchMode(true);
	        
	        this.setOnTouchListener(this);
	        
	        calculateColorDecay();
	        
	        showOwn = true;
	        showOther = true;
	        paint.setColor(Color.WHITE);
	        paint.setAntiAlias(true);
    		paint.setStrokeCap(Paint.Cap.ROUND);

	        pointsPaint= new ArrayList<Point>(); //This is a list of Point objects
	        pointsFeel= new ArrayList<Point>();
	        
	        decayThread = new TimeDecay(); //
	        decayThread.start();
		}
		
		public void calculateColorDecay() {
			float f = (float) selfRadius / (float) selfDecay;
			f = 255 / f;
			selfColorDecay = (int) (1.5*f);
			f = (float) otherRadius / (float) otherDecay;
			f = 255 / f;
			otherColorDecay = (int) (1.5*f);
		}
		
		public void Decay(){
			//go through the array, decrease radius by selfDecay
			int rad = 0;
    		synchronized(pointsFeel){			
				if(pointsFeel.size()>1){
	    			for (int i = 0; i<pointsFeel.size(); i++){
						rad = pointsFeel.get(i).radius;
						rad = rad - otherDecay;
						if(rad < 1 && pointsFeel.size() > 1){
								pointsFeel.remove(i);
						}else{
							if(rad < 1 && !theyretouching && pointsFeel.size() == 1){pointsFeel.remove(i);}else{
								pointsFeel.get(i).radius= rad;
							}
						}
					}
				}
    		}
    		synchronized(pointsPaint){
				for (int i = 0; i<pointsPaint.size(); i++){
					rad = pointsPaint.get(i).radius;
					rad = rad - selfDecay;
					if(rad < 1 && pointsPaint.size() > 1){
						pointsPaint.remove(i);
					}else{
						if(rad < 1 && !imtouching && pointsPaint.size() == 1){pointsPaint.remove(i);}else{
							pointsPaint.get(i).radius= rad;
						}
						
					}
				}			
    		}
		}
		
	    @Override
	    public void onDraw(Canvas canvas) {
	    	Point pointd;
	    	canvas.drawBitmap(background, 0,0, blur);
	    	if (showOther) {
	    		synchronized(pointsFeel){

		    		for (int i = pointsFeel.size(); i > 1; i--) {
		    			pointd = pointsFeel.get(i-1);
			    		paint.setColor(Color.rgb(0, 0, 255));
			    		paint.setStrokeWidth((float) pointsFeel.get(i-1).radius);			    		
//			    		canvas.drawCircle((float) pointd.x, (float) pointd.y, (float) pointd.radius, paint);
		       			canvas.drawLine((float) pointd.x, (float) pointd.y,(float) pointsFeel.get(i-2).x, (float) pointsFeel.get(i-2).y, paint);

		    		}//end point array for loop
	    			
		    		if(theyretouching){
	       				size = pointsFeel.size();
	       				if(size>0){
	       					canvas.drawBitmap(fingerPrint,  (float) pointsFeel.get(size-1).x-fingerPrint.getWidth()/2, (float) pointsFeel.get(size-1).y-fingerPrint.getHeight()/2, blur);
	       				}
		    		}
       			}//end if they're touching
	    	}//end if showOther
	    	
	    	if (showOwn) { //Showown is the menu variable
       			//Log.i("size: ", String.valueOf(pointsPaint.size()));
	    		synchronized(pointsPaint){
		    		for (int i = pointsPaint.size(); i > 1; i--) {
		    			pointd = pointsPaint.get(i-1);
			    		paint.setColor(Color.rgb(255, 255, 0));//point.color
			    		paint.setStrokeWidth((float) pointsPaint.get(i-1).radius);
			    		//canvas.drawCircle((float) pointd.x+i, (float) pointd.y, (float) pointd.radius, paint);
		       			//canvas.drawCircle((float) pointsPaint.get(i-1).x, (float) pointsPaint.get(i-1).y, (float) pointsPaint.get(i-1).radius, paint);
		       			canvas.drawLine((float) pointd.x, (float) pointd.y,(float) pointsPaint.get(i-2).x, (float) pointsPaint.get(i-2).y, paint);
		       			//Log.i("x: ", String.valueOf(pointsPaint.get(i-1).x));
	    			}//end point array for loop
	    		}
       			if(imtouching){
    	    		synchronized(pointsPaint){
	       				size = pointsPaint.size();
	       				if(size>0){
	       					canvas.drawBitmap(fingerPrint,  (float) pointsPaint.get(size-1).x-fingerPrint.getWidth()/2, (float) pointsPaint.get(size-1).y-fingerPrint.getHeight()/2, paint);
	       				}
    	    		}
       			}//end if I'm touching
	    	}//end if showOwn
	    	//Log.i("imtouching", String.valueOf(imtouching) + " " + String.valueOf(showOwn) + " " + String.valueOf(size));
	    	

    		//paint.setColor(Color.BLACK);
    		//paint.setTextSize(50);
    		//canvas.drawText("" + predictedPixels[1], 115, 115, paint);

	    }
		@Override
	    public boolean onTouch(View view, MotionEvent event) { //When it's touched, create a new point and add to the array
			Point point = new Point();
	        point.time = (int)System.currentTimeMillis();
			point.x = (int) event.getX();
	        point.y = (int) event.getY(); //event.getX is how to get current finger position
	        point.radius = selfRadius;
	        point.color = 0; // 0 must be white
			switch (event.getAction()) {			
			    case MotionEvent.ACTION_DOWN:
					point.vx = 0;
					point.vy = 0;
		
					// Start a new velocity tracker
					if (vTracker == null) {
						vTracker = VelocityTracker.obtain();
					} else {
						vTracker.clear();
					}
					vTracker.addMovement(event);
		
					// Set touching to true
					imtouching = true;
			        serverView.transferPoint(point.x, point.y);	 //Sending position, this will change for array

					break;
		
				case MotionEvent.ACTION_MOVE:
						// Update old positions
					point.x_old = point.x;
					point.y_old = point.y;
					// Update cursor positions
					point.x = (int)event.getX();
					point.y = (int)event.getY();		
					
					// Set touching to true
					imtouching = true;
			        serverView.transferPoint(point.x, point.y);	 	//Sending position, this will change for array
			    	
					vTracker.addMovement(event);
					vTracker.computeCurrentVelocity(1); // Compute velocity in pixels per 1 ms
					point.vx = vTracker.getXVelocity(); // get current velocities
					point.vy = vTracker.getYVelocity();
		
					break;
		
				case MotionEvent.ACTION_UP:
		
					imtouching = false;
			        serverView.transferPoint(0, 0);	 //zeros is the code for not touching
	
					break;
		
				case MotionEvent.ACTION_CANCEL:
					vTracker.recycle();
					break;
			}
    		synchronized(pointsPaint){
    			pointsPaint.add(point); //Adding the point to the array of points
    		}
    		
    		Date date = new Date();
    		SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd. h:mm:ss:SSS a");
    		String formattedDate = sdf.format(date);
	        //mainActivity.writeToLog(formattedDate + "; thistouch; " + String.valueOf(point.x) + "; " + String.valueOf(point.y) + "; " + String.valueOf(mainActivity.getTpadValue()) + ";");
    		
    		
	        invalidate(); //draws to screen, but why right here?			
			updateDraw = true; //This is the check that makes everything 
			//freeze until the next touch
			
			//NOTE THIS IS WHERE THE COMMENTING IS DONE TO SWITCH FROM 
			//SHAPE BASED FRICTION TO TEXTURE BASED FRICTION
			predictPixels();
			//
			
			//sendFingTexture();
			
			
			//calculateFriction();
	        return true;
	    }
		
		private void sendFingTexture(){
			float x, y;
			float A, R, Rmax;
			Rmax = pp.radius*5;
			
			synchronized(pointsFeel){
				if(pointsFeel.size()>0 && theyretouching && imtouching){
					pp = pointsFeel.get(pointsFeel.size()-1); // this is the most recent point sent from the other tpad
					synchronized(pointsPaint){ppoint = pointsPaint.get(pointsPaint.size()-1);} // this is the most recent point sent from the other tpad
					x = ppoint.x - pp.x;
					y = ppoint.y - pp.y;
					R = FloatMath.sqrt(x*x + y*y);
					A = (Rmax - R) / Rmax;
					mainActivity.sendTPadTexture(TPadTexture.SINUSOID, 100, A);
				}else{mainActivity.sendTPadTexture(TPadTexture.SINUSOID, 100, 0);}
				
			}
			
		}
//		private void calculateFriction(){ //Uses position and velocity to create array of tpad values
//			for (int i = pointsFeel.size(); i > feelLow; i--) {
//    			int xo = pointsFeel.get(i-1).x;// this is the most recent point sent from the other tpad
//    			int yo = pointsFeel.get(i-1).y;
//    			int r = pointsFeel.get(i-1).radius;
//
//
//    			double temp = (point.x-xo)*(point.x-xo)/.36 + (point.y-yo)*(point.y-yo)/1;
//    	        if(temp < r*r){
//    	        	z = Math.sqrt((r*r-temp));
//    	        	z = z/(double)r;
//    	        	sendval = 1-drawView.selfDecay*10*(z-zlast); //selfDecay is being used to scale here
//    	        	if (sendval > 1){sendval = 1;} 
//    	        	if (sendval < 0){sendval = 0;}
//    	        	zlast = z;
//    			}else{
//    				sendval = (float)0.5;
//    			}
//	            //drawView.setBackgroundColor(Color.rgb((int)(sendval*255), (int)(sendval*255), (int)(sendval*255))); // a check for z
//	            if(imtouching){
//	            	mainActivity.sendTPad((float)sendval);
//	            }else{
//	            	mainActivity.sendTPad(0);	            		
//	            }
//			}
//			return;
//		}
		
		private void predictPixels() { //Uses position and velocity to create array of tpad values
			double friction;
			float x, y;

//			Point debugpoint = new Point();
//	    	debugpoint.x = 400;
//	    	debugpoint.y = 500;
//	    	debugpoint.color = 0;
//	    	debugpoint.radius = 100;
//	    	pointsFeel.add(debugpoint);
    		synchronized(pointsFeel){
				if(pointsFeel.size()>0 ){
					pp = pointsFeel.get(pointsFeel.size()-1); // this is the most recent point sent from the other tpad
					synchronized(pointsPaint){ppoint = pointsPaint.get(pointsPaint.size()-1);} // this is the most recent point sent from the other tpad

					synchronized(predictedPixels){
						for (int i = 0; i < predictedPixels.length; i++) {
							x = (ppoint.x + ppoint.vx * (float)i);	//1st order hold in x direction extrapolation
							y = (ppoint.y + ppoint.vy * (float)i); //1st order hold in y direction extrapolation
							
							double temp = (x-pp.x)*(x-pp.x)/.36 + (y-pp.y)*(y-pp.y)/1f;
							//double temp = Math.exp(-((x-pp.x)*(x-pp.x)/(float)(720)+(y-pp.y)*(y-pp.y)/(float)(720)));
							//if(Math.abs(x-pp.x) < 300 && Math.abs(y-pp.y)<300){
			    	        if(temp < 50*50*4){ //pp.radius*pp.radius*4
			    	        	z = Math.sqrt((50*50*4-temp)); //pp.radius*pp.radius*4
			    	        	z = z/(double)50/2f;

			    	        	if (i > 0){ //This is to get rid of the discontinuities
			    	        		friction = 0.5f-25f*(z-zlast)/ppoint.getDist(); 
			    	        	}	else{
			    	        		//friction = mainActivity.getTpadValue();
			    	        		friction = .5;
			    	        	}
		
			    	        	if (friction > 1){friction = 1;} 
			    	        	if (friction < 0){friction = 0;}
			    	        	zlast = z; 
			    	        	frictionlast = friction; 
			    			}else{
			    				friction = (float)0.5;
			    				frictionlast = friction;
			    			}
			           		//Log.i("tpad: ", String.valueOf(x) + " " + String.valueOf(pp.x) +" " + String.valueOf(y) +" " + String.valueOf(pp.y) +" " +  String.valueOf(friction));

			    	        predictedPixels[i] = (float)friction; 
						
						}//end predictePixels for loop
						mainActivity.sendTPadBuffer(predictedPixels);
		       			//Log.i("x: ", String.valueOf(predictedPixels[0]) + " " + String.valueOf(mainActivity.getTpadValue()));

					}
				}
    		}
		}//end predicted positions
	}//end DrawView
    
    class Point {  //Point is a class he made to contain the position, radius and b value of color
        int color, x, y, x_old, y_old, radius, time;
        float vx,vy;
        float getDist(){
        	return FloatMath.sqrt(vx*vx+vy*vy);
        }
    }
    
    public class TimeDecay extends Thread {  //This is a separate thread that keeps track of the decay
    	int decay = 40; //Number of milliseconds that the decay waits for
    	private Boolean run = true;
    	public void toggle(Boolean b) {
    		run = b;
    	}
    	public void run() {
    		while (true) {
    			while (run) {
    				try {
						Thread.sleep(decay); //Causes the thread which sent this message to sleep for the given interval of time (given in milliseconds).
//						updateDraw = true;
//	    				updateFeel = true;
						drawView.Decay();
	    				mainActivity.runOnUiThread(new Runnable() { //Anything that causes the UI to be updated or changed HAS to happen on the UI thread.

	                        @Override
	                        public void run() {
	                        	drawView.invalidate();
	                        }
	                    });
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}
    }
}
