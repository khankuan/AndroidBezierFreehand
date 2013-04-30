package com.khankuan.androidbezierfreehand;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Cap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/*
 * 	Annotation View that takes in touch input and draws smooth freehand drawing.
 * 	Width and Height are expected to be fixed after initialisation
 * 	Several functions are available: Clear, Set color, Set size
 * 
 */
public class AnnotationView extends View{
	
	//	Variables, bitmap is public so screenshots can be done easily
	private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    
    //	Annotation Data, each element contains data for a single stroke
    private ArrayList<Float> widths = new ArrayList<Float>();
    private ArrayList<Integer> colors = new ArrayList<Integer>();
    private ArrayList<ArrayList<Point>> strokes = new ArrayList<ArrayList<Point>>();
    
    //	Current Stroke Data, we assume each stroke to only be a fixed size and color
    //	currentSize and currentColor and available to be set publicly
    private ArrayList<Point> currentStroke;
    private Float currentWidth = 5.0f;
    private int currentColor = Color.BLACK;
    private int currentStrokeIndex = 0;			//	Take note of current point in markerStrokes, used for undo/redo
	
    //	Constructor
	public AnnotationView(Context c, AttributeSet attr) {
        super(c, attr);
        setFocusable(true);
    }
	
	//	Method for initialising
	protected void onSizeChanged (int w, int h, int oldw, int oldh){
		//	Create bitmap to draw on
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        if (bitmap != null) 
        	canvas.drawBitmap(bitmap, 0, 0, null);
        
        //	Create paint to use for drawing strokes
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        
        clearAnnotation();
	}
	
	//	Override to render canvas
	@Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

	//	Method handle touch inputs
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			//	Create a new stroke
			currentStroke = new ArrayList<Point>();		
		    
			//	Add 3 points to it so we get a dot immediately
			this.addPointAndDraw(new Point((int)event.getX()-1, (int)event.getY()-1));
			this.addPointAndDraw(new Point((int)event.getX(), (int)event.getY()));
			this.addPointAndDraw(new Point((int)event.getX()+1, (int)event.getY()+1));
		}
	    
	    if (event.getAction() == MotionEvent.ACTION_MOVE) {
	    	final int N = event.getHistorySize();
	        final int P = event.getPointerCount();
	    	for (int i = 0; i < N; i++)
	            for (int j = 0; j < P; j++)
	            	this.addPointAndDraw(new Point((int) event.getHistoricalX(j, i), (int) event.getHistoricalY(j, i)));
	    }
	    
	    if (event.getAction() == MotionEvent.ACTION_UP) {
	    	this.addPointAndDraw(new Point((int)event.getX(), (int)event.getY()));
	        
	        //  Clear stored redos after current index
	        while (currentStrokeIndex < strokes.size()) {
	        	strokes.remove(strokes.size()-1);
	        	widths.remove(widths.size()-1);
	        	colors.remove(colors.size()-1);
	        }
	        
	        strokes.add(currentStroke);
	        widths.add(currentWidth);
	        colors.add(currentColor);
	        
	        currentStrokeIndex = strokes.size();
	    }
		
        invalidate();
        return true;
    }
	
	private void addPointAndDraw(Point p){
		//	Add point
		currentStroke.add(p);
		
		//	Draw to point, only if there are at least 3 points
	    if (currentStroke.size() < 3)
	    	return;
	    
	    //	Draw bezier curve from start to mid to end (3 points)
	    this.drawSection(currentStroke.size()-3);
	}
	
	//	Draw the ith section in the currentStroke
	private void drawSection(int i){
		//	Ensure enough points
		if (i > currentStroke.size()-3)
			return;
		
		//	Set color and size to paint
		paint.setColor(currentColor);
		paint.setStrokeWidth(currentWidth);
		
		Point mid1 = new Point((currentStroke.get(i).x + currentStroke.get(i+1).x)/2,
								(currentStroke.get(i).y + currentStroke.get(i+1).y)/2);
		Point midmid = currentStroke.get(i+1);
		Point mid2 = new Point((currentStroke.get(i+1).x + currentStroke.get(i+2).x)/2,
								(currentStroke.get(i+1).y + currentStroke.get(i+2).y)/2);

		Path path = new Path();
		path.moveTo(mid1.x, mid1.y);
		path.quadTo(midmid.x, midmid.y, mid2.x, mid2.y);
        canvas.drawPath(path, paint);
	}
	
	//	Method to clear all annotations
	public void clearAnnotation(){
		 if (canvas != null) {
			 canvas.drawColor(Color.WHITE);
			 
			 //	Reset strokes data
			 strokes.clear();
			 colors.clear();
			 widths.clear();
			 currentStrokeIndex = 0;
			    
			 invalidate();
        }
	}
	
	//	Method to Undo annotation
	public void undoAnnotation(){
		//	Reset back currentColor and currentWidth after undoing
	    Integer originalColor = currentColor;
	    Float originalWidth = currentWidth;
	    
	    //  Ensure that there are moves to undo
	    if (currentStrokeIndex < 1) return;
	    
	    //  Reset the image and update currentStrokeIndex
	    canvas.drawColor(Color.WHITE);
	    currentStrokeIndex--;
	    
	    //  Redraw the points before
	    for (int i = 0; i < currentStrokeIndex; i++) {
	        currentStroke = strokes.get(i);
	        currentColor = colors.get(i);
	        currentWidth = widths.get(i);
	        
	        for (int j = 0; j < currentStroke.size(); j++) 
	            this.drawSection(j);
	    }
	    
	    //	Reset
	    currentColor = originalColor;
	    currentWidth = originalWidth;
	    invalidate();
	}
	
	//	Method to redo annotation
	public void redoAnnotation(){
		//	Reset back currentColor and currentWidth after undoing
	    Integer originalColor = currentColor;
	    Float originalWidth = currentWidth;
	    
	    //	Ensure that there are moves to redo
	    if (currentStrokeIndex >= strokes.size()) return;
	    
	    currentStroke = strokes.get(currentStrokeIndex);
	    currentColor = colors.get(currentStrokeIndex);
	    currentWidth = widths.get(currentStrokeIndex);
	    
	    //  Draw each stroke section
	    for (int j = 0; j < currentStroke.size(); j++)
	        this.drawSection(j);
	    
	    //	Update currentStrokeIndex
	    currentStrokeIndex++;
	    
	    //	Reset
	    currentColor = originalColor;
	    currentWidth = originalWidth;
	    invalidate();
	}

	/*
	 * 	Getters and setters
	 */
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public Float getCurrentWidth() {
		return currentWidth;
	}

	public void setCurrentWidth(Float currentWidth) {
		this.currentWidth = currentWidth;
	}

	public int getCurrentColor() {
		return currentColor;
	}

	public void setCurrentColor(int currentColor) {
		this.currentColor = currentColor;
	}
}
