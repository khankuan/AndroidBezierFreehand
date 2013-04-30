package com.khankuan.androidbezierfreehand;

import com.khankuan.androidbezierfreehand.R;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.Activity;
import android.graphics.Color;

public class MainActivity extends Activity {
	//	Reference
	AnnotationView annotationView;
	SeekBar widthSeekBar;
	TextView widthText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		annotationView = (AnnotationView)findViewById(R.id.AnnotationView);
		widthText = (TextView)findViewById(R.id.widthText);
		widthSeekBar = (SeekBar)findViewById(R.id.widthSeekBar);
		widthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				annotationView.setCurrentWidth(progress+1.0f);
				widthText.setText("Width: "+(progress+1));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	public void undo(View v){
		annotationView.undoAnnotation();
	}
	
	public void redo(View v){
		annotationView.redoAnnotation();
	}
	
	public void blackColor(View v){
		annotationView.setCurrentColor(Color.BLACK);
	}
	
	public void redColor(View v){
		annotationView.setCurrentColor(Color.RED);
	}
	
	public void clear(View v){
		annotationView.clearAnnotation();
	}
}
