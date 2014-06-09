package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class UI2 extends Activity {

	private CheckBox chX;
	private CheckBox chY;
	private CheckBox chZ;
	private SeekBar sb;
	private EditText nomevar;
	private TextView timevar;
	private TextView ultimavar;
	private TextView result;
	private Button fineui2;
	private ImageView iv;
	private String timestamp_ric;
	private String nome_ric;
	private int sovra_ric,ncampx,ncampy,ncampz;
	private String dataulitmamodifica;
	private String codifica;
    private boolean x_selected, y_selected, z_selected;
    private final boolean INIZIO=true;
	private long id_ric;
	private String pkg_r;
	private DbAdapter db;
	private Cursor cr;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
	        	
	        /*-- Show Keyboard if was already visible before --*/
	      	if(savedInstanceState.getBoolean("KeyboardVisible",false))
	      		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }        
        setContentView(R.layout.ui2_layout);
        Intent intent_r=getIntent();
        pkg_r=getPackageName();
        
        db = new DbAdapter(this);
     
        /*-- Set layout view resources --*/
        nomevar = (EditText)findViewById(R.id.nomeSessione);
        timevar = (TextView)findViewById(R.id.datareg);
        ultimavar = (TextView)findViewById(R.id.dataulmod);
        result = (TextView)findViewById(R.id.res);
        fineui2 =(Button)findViewById(R.id.toPlay);
        chX = (CheckBox)findViewById(R.id.checkBoxX);
        chY = (CheckBox)findViewById(R.id.checkBoxY);
        chZ = (CheckBox)findViewById(R.id.checkBoxZ);
        sb = (SeekBar)findViewById(R.id.seekBar1);
        iv = (ImageView) findViewById(R.id.thumbnail);
        
        
        /*-- Checkboxes listener: used to have at least one checkbox checked --*/
        OnCheckedChangeListener listener = new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
        		if(!isChecked){
        		switch(arg0.getId())
        		  {
        		    case R.id.checkBoxX:{
        		         if(!chY.isChecked() && !chZ.isChecked())
        		        	 chX.setChecked(true);
        		         break;
        		    }
        		    case R.id.checkBoxY:{
        		    	if(!chX.isChecked() && !chZ.isChecked())
        		    		chY.setChecked(true);
        		         break;
        		    }
        		   case R.id.checkBoxZ:{
        			   if(!chY.isChecked() && !chX.isChecked())
        				   chZ.setChecked(true);
        		        break;
        		   }
        		  }
        		}
        	}};
    	
        	chX.setOnCheckedChangeListener(listener);
        	chY.setOnCheckedChangeListener(listener);
        	chZ.setOnCheckedChangeListener(listener);
        

        id_ric=intent_r.getLongExtra(pkg_r+".myIdToUi2", -1);
        
        aggiornaDati(INIZIO);
        
        
       
        /*-- Upsampling seekbar listener --*/
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            	sb.setProgress(seekBar.getProgress());
            	result.setText(""+sb.getProgress());
            }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				sb.setProgress(seekBar.getProgress());
				result.setText(""+sb.getProgress());
				
			}});
        
        
        /*-- Riproduci sessione button pressed --*/
        fineui2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	String nomeNuovo=nomevar.getText().toString();
            	
            	/*-- Check for changes --*/
            	if(chX.isChecked()!=x_selected || chY.isChecked()!=y_selected || chZ.isChecked()!=z_selected ||
            		sb.getProgress()!=sovra_ric || !(nome_ric.equals(nomeNuovo)) ){
            	
            		/*-- Check name to prevent errors and if it is already on DB --*/
            		if(nomeNuovo.contains("'") || (nomeNuovo.contains("_") && !(nome_ric.equals(nomeNuovo))))
            			Toast.makeText(getApplicationContext(), R.string.apiceNonConsentito, Toast.LENGTH_SHORT).show();
            	
            		else if((!(nome_ric.equals(nomeNuovo)) && UI1.sameName(db, nomeNuovo)) || nomeNuovo.equals(""))
            			Toast.makeText(getApplicationContext(), R.string.ToastAlertSameName, Toast.LENGTH_SHORT).show();
            	
            			/*-- Check if the track to update is already on play --*/
            			else if (id_ric==widget_big.currid && !widget_big.pause)
            				Toast.makeText(getApplicationContext(), R.string.cannotUpdate, Toast.LENGTH_SHORT).show();
            				
            				/*-- Then update existing record with new informations --*/
	            			else {
	            				nome_ric=nomeNuovo;
	            				dataulitmamodifica=DateFormat.format("dd-MM-yyyy kk:mm:ss", new java.util.Date()).toString();
	            				long dur=DataRecord.calcoloTempo(ncampx,ncampy,ncampz,chX.isChecked(),chY.isChecked(),chZ.isChecked(),sb.getProgress());	
	            				db.open();
	            				db.updateRecord(id_ric, nome_ric, ""+dur, ""+chX.isChecked(),""+chY.isChecked(),""+chZ.isChecked(),
	            						""+sb.getProgress(), dataulitmamodifica);
	            				db.close();
	            		
	            				prosegui();
	              			}
            	}
            	else prosegui();
            	
            }
        });

               
	} /*-- onCreate End --*/
	
	
	/*-- Method which check if playback isn't already running, in this case 
	 *-- we send ID to UI4 to play it, otherwise a toast appears and current displayed data will be update --*/
	private void prosegui(){
		if(widget_big.pause){
			stopService(new Intent(this, PlayRecord.class));
			Intent intentToUI4=new Intent(UI2.this, UI4.class);
			intentToUI4.putExtra(pkg_r+".myServiceID", (int) id_ric);
			startActivity(intentToUI4);
		}
		else {
			Toast.makeText(getApplicationContext(), R.string.alreadyPlaying, Toast.LENGTH_SHORT).show();
			aggiornaDati(!INIZIO);
		}
	}
	
	
	/*-- Method which reads updated data from DB and displays correct informations --*/
	private void aggiornaDati(boolean first_time){
		db.open();
        cr=db.fetchRecordById(id_ric);
        cr.moveToNext();
        
        nome_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_NAME));
        timestamp_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_DATE));
        dataulitmamodifica=cr.getString(cr.getColumnIndex(DbAdapter.KEY_LAST));
        x_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        y_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        z_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        sovra_ric=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
        codifica = cr.getString(cr.getColumnIndex(DbAdapter.KEY_IMM));
        
        ultimavar.setText(dataulitmamodifica.substring(0, 16));
        
        if(first_time){
            ncampx=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPX)));
            ncampy=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPY)));
            ncampz=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPZ)));
            
            int alpha = Integer.parseInt(codifica.substring(0, 3));
            int red = Integer.parseInt(codifica.substring(3, 6));
            int green = Integer.parseInt(codifica.substring(6, 9));
            int blue = Integer.parseInt(codifica.substring(9, 12));
            
            iv.setBackgroundColor(Color.argb(alpha, red, green, blue));
            
            switch(Integer.parseInt(codifica.substring(11))) {
    			case 0:
    				iv.setImageResource(R.drawable.ic_music_0);
    				break;
    			case 1:
    				iv.setImageResource(R.drawable.ic_music_1);
    				break;
    			case 2:
    				iv.setImageResource(R.drawable.ic_music_2);
    				break;
    			case 3:
    				iv.setImageResource(R.drawable.ic_music_3);
    				break;
    			case 4:
    				iv.setImageResource(R.drawable.ic_music_4);
    				break;
    			case 5: 
    				iv.setImageResource(R.drawable.ic_music_5);
    				break;
    			case 6:
    				iv.setImageResource(R.drawable.ic_music_6);
    				break;
    			case 7:
    				iv.setImageResource(R.drawable.ic_music_7);
    				break;
    			case 8:
    				iv.setImageResource(R.drawable.ic_music_8);
    				break;
    			case 9:
    				iv.setImageResource(R.drawable.ic_music_9);
    				break;
    		}
            
            chX.setChecked(x_selected);
            chY.setChecked(y_selected);
            chZ.setChecked(z_selected);
            nomevar.setText(nome_ric);
            nomevar.setSelection(nomevar.getText().length());
            timevar.setText(timestamp_ric.substring(0, 16));
            result.setText(""+sovra_ric);
            sb.setProgress(sovra_ric);
        }
        
        cr.close();
        db.close();
        
	}
	
	/*-- On activity resume (e.g. come back from UI4) it displays updated informations --*/
	public void onResume() {
		super.onResume();
		aggiornaDati(!INIZIO);
	}
	
	
	
	/*-- Back button pressed --*/
	@Override
	public void onBackPressed() {
			Intent returnIntent = new Intent(UI2.this, UI1.class);
        	startActivity(returnIntent);
        	finish();	
	return;
	}
	
	
	/*-- Method used to save current state --*/
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  if(((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).isAcceptingText())
		  savedInstanceState.putBoolean("KeyboardVisible", true);

	}
	
	
	/*-- Option menu --*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.option_menu, menu);
		return true;
		}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem ordina = menu.findItem(R.id.Or);
		ordina.setVisible(false);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
		case R.id.Preferenze:
			Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
            startActivity(prefIntentUI5);
            return(true);
		}
		
		return (super.onOptionsItemSelected(item));
	}

}
