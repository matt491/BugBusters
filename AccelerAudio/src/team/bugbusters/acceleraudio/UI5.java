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
	    	
      
	        //Menu a tendina per la scelta della Frequenza
	        spinner = (Spinner)findViewById(R.id.spinner1);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	        		this,
	        		android.R.layout.simple_spinner_item,
	        		new String[]{"NORMAL","UI","GAME","FASTEST"}
	        		);
	         spinner.setAdapter(adapter);
    
	         
	         
	         spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	        	public void onItemSelected(AdapterView<?> adapter, View view,int pos, long id) {
	        		String selected = (String)adapter.getItemAtPosition(pos);
	        		Toast.makeText(getApplicationContext(),"Hai selezionato "+selected,Toast.LENGTH_SHORT).show();
	        		freqdef=selected;
	        	}
	        	public void onNothingSelected(AdapterView<?> arg0) {}
	        	});
	        
	         
	        	//Lettura delle impostazioni già salvate
	        	defX.setChecked(prefs.getBoolean("Xselect", true));
	        	defY.setChecked(prefs.getBoolean("Yselect", true));
	        	defZ.setChecked(prefs.getBoolean("Zselect", true));
	        	sbdurdef.setProgress(prefs.getInt("duratadef", 50));
	        	sbsovradef.setProgress(prefs.getInt("sovrdef", 0));
	        	if(prefs.getString("Campion", "NORMAL").equals("NORMAL")) spinner.setSelection(0);
	        	if(prefs.getString("Campion", "NORMAL").equals("UI")) spinner.setSelection(1);
	        	if(prefs.getString("Campion", "NORMAL").equals("GAME")) spinner.setSelection(2);
	        	if (prefs.getString("Campion", "NORMAL").equals("FASTEST")) spinner.setSelection(3);
	        	dmax.setText(sbdurdef.getProgress()+" secondi");					//Visualizzazione Durata default (max 120 sec)
	        	if(sbsovradef.getProgress()==0)scampdef.setText("Scelta 0");		//Visualizzazione Sovracampionamento
				if(sbsovradef.getProgress()==25)scampdef.setText("Scelta 1");
				if(sbsovradef.getProgress()==50)scampdef.setText("Scelta 2");
				if(sbsovradef.getProgress()==75)scampdef.setText("Scelta 3");
				if(sbsovradef.getProgress()==100)scampdef.setText("Scelta 4");
	         
	        	//Sovrascritti i metodi della SeekBar della durata di default
	        	sbdurdef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

	            @Override
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            }
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {				
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					String l;
					if(seekBar.getProgress()%60 < 10) l="0"+seekBar.getProgress()%60;
					else l=""+seekBar.getProgress()%60;
					 dmax.setText(seekBar.getProgress()/60+":"+l);
	              //  dmax.setText(seekBar.getProgress()+" secondi");
				}});

	        
	        	//Sovrascritti i metodi della SeekBar del Sovracampionamento di default
	        	sbsovradef.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

	                @Override
	                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	                	if(progress<15)seekBar.setProgress(0);
	                    if(progress>15 && progress<40)seekBar.setProgress(25);
	                    if(progress>40 && progress<60)seekBar.setProgress(50);
	                    if(progress>60 && progress<80)seekBar.setProgress(75);
	                    if(progress>80)seekBar.setProgress(100);
	                }

	    			@Override
	    			public void onStartTrackingTouch(SeekBar seekBar) {
	    			}

	    			@Override
	    			public void onStopTrackingTouch(SeekBar seekBar) {
	    				scampdef.setText(UI2.campToString(sbsovradef.getProgress()));
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
	        	
	        	
	        	
	} //Fine onCreate
	

}
