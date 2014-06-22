package team.bugbusters.acceleraudio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UI5 extends Activity {

	private SharedPreferences prefs;
	private CheckBox defX;
	private CheckBox defY;
	private CheckBox defZ;
	private SeekBar sbdurdef;
	private SeekBar sbsovradef;
	private Spinner spinner;
	private String freqdef;
	private TextView scampdef;
	private TextView dmax;
	public static boolean fromWidget;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.ui5_layout);
	        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        defX = (CheckBox) findViewById(R.id.checkXdef);  
	        defY = (CheckBox) findViewById(R.id.checkYdef);
	        defZ = (CheckBox) findViewById(R.id.checkZdef);
	        sbdurdef = (SeekBar) findViewById(R.id.durdef);
	    	sbsovradef = (SeekBar) findViewById(R.id.sbcampdef);
	    	scampdef = (TextView) findViewById(R.id.sovradef);
	    	dmax = (TextView) findViewById(R.id.durmax);
        	
	    	
	    	/*-- Checkbox listener: used to keep at least one checkbox checked --*/
	    	OnCheckedChangeListener listener = new OnCheckedChangeListener() {
	        	public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
	        		if(!isChecked){
	        		switch(arg0.getId())
	        		  {
	        		    case R.id.checkXdef:{
	        		         if(!defY.isChecked() && !defZ.isChecked())
	        		        	 defX.setChecked(true);
	        		         break;
	        		    }
	        		    case R.id.checkYdef:{
	        		    	if(!defX.isChecked() && !defZ.isChecked())
	        		        	 defY.setChecked(true);
	        		         break;
	        		    }
	        		   case R.id.checkZdef:{
	        			   if(!defY.isChecked() && !defX.isChecked())
	        		        	 defZ.setChecked(true);
	        		        break;
	        		   }
	        		  }
	        		}
	        	}};
	    	
	        	defX.setOnCheckedChangeListener(listener);
	        	defY.setOnCheckedChangeListener(listener);
	        	defZ.setOnCheckedChangeListener(listener);
	        	
	        	
	        	/*-- Spinner menu to select recording deafult sampling --*/
	        	spinner = (Spinner) findViewById(R.id.spinner1);
	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,
	        			new String[]{"Lento","Normale","Veloce"});
	        	spinner.setAdapter(adapter);
   
	        	/*-- Spinner listener: used to set accelerometer default samplerate --*/
	        	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        	public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
	        		String selected = (String) adapter.getItemAtPosition(pos);
	        		freqdef = selected;
	        	}
	        	public void onNothingSelected(AdapterView<?> arg0) {}
	        	});
	        
	         
	         	/*-- Reading settings from Shared Preferences --*/
	        	defX.setChecked(prefs.getBoolean("Xselect", true));
	        	defY.setChecked(prefs.getBoolean("Yselect", true));
	        	defZ.setChecked(prefs.getBoolean("Zselect", true));
	        	sbdurdef.setProgress(prefs.getInt("duratadef", 30));
	        	sbsovradef.setProgress(prefs.getInt("sovrdef", 0));
	        	spinner.setSelection(stringToFreq(prefs.getString("Campion", "Normale")));
	        	dmax.setText(sbdurdef.getProgress()+" secondi");
	        	scampdef.setText(""+sbsovradef.getProgress());
	
	         
	        	/*-- Methods override of seekbar which set default recording duration --*/
	        	sbdurdef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
	        		
		            @Override
		            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		            	sbdurdef.setProgress(seekBar.getProgress());
		            	dmax.setText(seekBar.getProgress()+" secondi");
		            }
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
	
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						sbdurdef.setProgress(seekBar.getProgress());
		                dmax.setText(seekBar.getProgress()+" secondi");
		            }
	        	});

	        
	        	/*-- Methods override of seekbar which set default upsampling --*/
	        	sbsovradef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

	                @Override
	                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	                	sbsovradef.setProgress(seekBar.getProgress());
	                	scampdef.setText(""+sbsovradef.getProgress());
	                }

	    			@Override
	    			public void onStartTrackingTouch(SeekBar seekBar) {}

	    			@Override
	    			public void onStopTrackingTouch(SeekBar seekBar) {
	    				sbsovradef.setProgress(seekBar.getProgress());
	    				scampdef.setText(""+sbsovradef.getProgress());
		            }
	        	});
	
	        	
	
	}
	
	
	public void onClick(View view) {
		switch(view.getId()) {
			
			/*-- Salva button pressed --*/
			case R.id.salva_imp: {
				Editor prefsEditor = prefs.edit();
            	prefsEditor.putBoolean("Xselect", defX.isChecked());
            	prefsEditor.putBoolean("Yselect", defY.isChecked());
            	prefsEditor.putBoolean("Zselect", defZ.isChecked());
            	prefsEditor.putString("Campion", freqdef);
            	prefsEditor.putInt("duratadef", sbdurdef.getProgress());
            	prefsEditor.putInt("sovrdef", sbsovradef.getProgress());
            	prefsEditor.commit();
            	
            	Toast.makeText(UI5.this, R.string.savedPref,Toast.LENGTH_SHORT).show();
			}
		
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void onResume() {
		super.onResume();
		
		/*-- If UI5 was launched/restored from widget then lock the screen at the current position --*/
    	if(fromWidget) {
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			Display disp = wm.getDefaultDisplay();
			int orientation = disp.getRotation();
			if(orientation == Surface.ROTATION_0) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
			if(orientation == Surface.ROTATION_90) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if(orientation == Surface.ROTATION_270) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);	
    	}
    	
	}
	
	
	public void onPause() {
		super.onPause();
		if(fromWidget)
			finish();		
	}
	
	
	public void onDestroy(){
		super.onDestroy();
		fromWidget = false;
	}
	
	
	/*-- Back button pressed --*/
	@Override
	public void onBackPressed() {
        	finish();
	}
	
	
	
	/*-- Method used to set spinner selection --*/
	public static int stringToFreq(String s){
		if(s.equals("Lento")) return 0;
		if(s.equals("Normale")) return 1;
		else return 2;
	}
	

	
	
}
