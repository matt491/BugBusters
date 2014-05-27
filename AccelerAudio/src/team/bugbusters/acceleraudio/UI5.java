package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        
	        setContentView(R.layout.ui5_layout);
	        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        defX=(CheckBox)findViewById(R.id.checkXdef);
	        
	        defY=(CheckBox)findViewById(R.id.checkYdef);
	        defZ=(CheckBox)findViewById(R.id.checkZdef);
	        sbdurdef =(SeekBar)findViewById(R.id.durdef);
	    	sbsovradef=(SeekBar)findViewById(R.id.sbcampdef);
	    	scampdef=(TextView)findViewById(R.id.sovradef);
	    	Button salva=(Button)findViewById(R.id.salva_imp);
	    	dmax=(TextView)findViewById(R.id.durmax);
	    	
      
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
	        	
	        	
	        //Menu a tendina per la scelta della Frequenza
	        spinner = (Spinner)findViewById(R.id.spinner1);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	        		this,
	        		android.R.layout.simple_spinner_item,
	        		new String[]{"Molto lento","Lento","Normale","Veloce"}
	        		);
	         spinner.setAdapter(adapter);
    
	         
	         
	         spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        	public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
	        		String selected = (String)adapter.getItemAtPosition(pos);
	        		freqdef=selected;
	        	}
	        	public void onNothingSelected(AdapterView<?> arg0) {}
	        	});
	        
	         
	        	//Lettura delle impostazioni già salvate
	        	defX.setChecked(prefs.getBoolean("Xselect", true));
	        	defY.setChecked(prefs.getBoolean("Yselect", true));
	        	defZ.setChecked(prefs.getBoolean("Zselect", true));
	        	sbdurdef.setProgress(prefs.getInt("duratadef", 30));
	        	sbsovradef.setProgress(prefs.getInt("sovrdef", 0));
	        	spinner.setSelection(stringToFreq(prefs.getString("Campion", "Normale")));
	        	dmax.setText(sbdurdef.getProgress()+" secondi");
	        	scampdef.setText(campToString(sbsovradef.getProgress()));
	
	         
	        	//Sovrascritti i metodi della SeekBar della durata di default
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
				}});

	        
	        	//Sovrascritti i metodi della SeekBar del Sovracampionamento di default
	        	sbsovradef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

	                @Override
	                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	                	if(progress<15){
	                		seekBar.setProgress(0);
	                		scampdef.setText(campToString(sbsovradef.getProgress()));
	                		}
	                    if(progress>15 && progress<40){
	                    	seekBar.setProgress(25);
	                    	scampdef.setText(UI5.campToString(sbsovradef.getProgress()));
	                    	}
	                    if(progress>40 && progress<60){
	                    	seekBar.setProgress(50);
	                    	scampdef.setText(UI5.campToString(sbsovradef.getProgress()));
	                    	}
	                    if(progress>60 && progress<80){
	                    	seekBar.setProgress(75);
	                    	scampdef.setText(UI5.campToString(sbsovradef.getProgress()));
	                    	}
	                    if(progress>80){
	                    	seekBar.setProgress(100);
	                    	scampdef.setText(UI5.campToString(sbsovradef.getProgress()));
	                    	}
	                }

	    			@Override
	    			public void onStartTrackingTouch(SeekBar seekBar) {}

	    			@Override
	    			public void onStopTrackingTouch(SeekBar seekBar) {
	    				scampdef.setText(campToString(sbsovradef.getProgress()));
	    			}});
	        	
	        	
	        	
	        	//Tasto Salva premuto
	        	salva.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View v) {

	                	Editor prefsEditor = prefs.edit();
	                	prefsEditor.putBoolean("Xselect",defX.isChecked());
	                	prefsEditor.putBoolean("Yselect",defY.isChecked());
	                	prefsEditor.putBoolean("Zselect",defZ.isChecked());
	                	prefsEditor.putString("Campion", freqdef);
	                	prefsEditor.putInt("duratadef", sbdurdef.getProgress());
	                	prefsEditor.putInt("sovrdef", sbsovradef.getProgress());
	                	prefsEditor.commit();
	                	
	                	Toast.makeText(getApplicationContext(),"Preferenze salvate",Toast.LENGTH_SHORT).show();
	                	
	                }});
	        	
	
	
		

	}//Fine onCreate
	
	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
		super.onBackPressed();
        	finish();	
	return;
	}
	
	public static String campToString(int c){
		if(c==0) return "Scelta 0";
		else if(c==25) return "Scelta 1";
		else if(c==50) return "Scelta 2";
		else if(c==75) return "Scelta 3";
		else return  "Scelta 4";
		
	}
	
	public static int stringToFreq(String s){
		if(s.equals("Molto lento")) return 0;
		if(s.equals("Lento")) return 1;
		if(s.equals("Normale")) return 2;
		else return 3;
	}
	
	
	public static int stringToCamp(String s){
		if(s.equals("Scelta 0")) return 0;
		if(s.equals("Scelta 1")) return 25;
		if(s.equals("Scelta 2")) return 50;
		if(s.equals("Scelta 3")) return 75;
		else return 100;
	}

	
	
}
