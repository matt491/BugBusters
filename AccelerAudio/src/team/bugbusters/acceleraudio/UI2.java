package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
	private int sovra_ric;
	private String dataulitmamodifica;
	private String codifica;
    private boolean x_selected, y_selected, z_selected;
	private long id_ric;
	private String pkg_r;
	
	private DbAdapter dbHelper;
	private Cursor cr;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui2_layout);
        Intent intent_r=getIntent();
        pkg_r=getPackageName();
        
        dbHelper = new DbAdapter(this);
     
        nomevar= (EditText)findViewById(R.id.nomeSessione);
        timevar= (TextView)findViewById(R.id.datareg);
        ultimavar= (TextView)findViewById(R.id.dataulmod);
        result= (TextView)findViewById(R.id.res);
        fineui2=(Button)findViewById(R.id.toPlay);
        chX=(CheckBox)findViewById(R.id.checkBoxX);
        chY=(CheckBox)findViewById(R.id.checkBoxY);
        chZ=(CheckBox)findViewById(R.id.checkBoxZ);
        sb = (SeekBar)findViewById(R.id.seekBar1);
        iv = (ImageView) findViewById(R.id.thumbnail);
        
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
        //Apertura DB
        dbHelper.open();
        //Query al DB: dato l'ID recupero tutta la tupla
        cr=dbHelper.fetchRecordById(id_ric);
        cr.moveToNext();
        
        //Recupero delle informazioni dalla query fatta al DB
        nome_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_NAME));
        timestamp_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_DATE));
        dataulitmamodifica=cr.getString(cr.getColumnIndex(DbAdapter.KEY_LAST));
        x_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        y_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        z_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        sovra_ric=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
        codifica = cr.getString(cr.getColumnIndex(DbAdapter.KEY_IMM));
        
        //Chiusura Cursor e DB
        cr.close();
        dbHelper.close();
        
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
        timevar.setText(timestamp_ric);
        ultimavar.setText(dataulitmamodifica);
        result.setText(""+sovra_ric);
        sb.setProgress(sovra_ric);
        
        
       
        //Barra del Sovracampionamento
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
        
        
        fineui2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	String nomeNuovo=nomevar.getText().toString();
            	
            	if(chX.isChecked()!=x_selected || chY.isChecked()!=y_selected || chZ.isChecked()!=z_selected ||
            		sb.getProgress()!=sovra_ric || !(nome_ric.equals(nomeNuovo)) ){
            	
            		if(nomeNuovo.contains("'") || (nomeNuovo.contains("_") && !(nome_ric.equals(nomeNuovo))))
            			Toast.makeText(getApplicationContext(), R.string.apiceNonConsentito, Toast.LENGTH_LONG).show();
            	
            		else if((!(nome_ric.equals(nomeNuovo)) && UI1.sameName(dbHelper, nomeNuovo)) || nomeNuovo.equals(""))
            			Toast.makeText(getApplicationContext(), R.string.ToastAlertSameName, Toast.LENGTH_LONG).show();
            	
            			else {
            				nome_ric=nomeNuovo;
            				dataulitmamodifica=DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
            				dbHelper.open();
            				dbHelper.updateRecord(id_ric, nome_ric, ""+chX.isChecked(),""+chY.isChecked(),""+chZ.isChecked(),
            						""+sb.getProgress(), dataulitmamodifica);
            				dbHelper.close();
            		
            
            				//Alla UI4 viene spedito l'ID del record creato/aggiornato
            				Intent intentToUI4=new Intent(UI2.this, UI4.class);
            				intentToUI4.putExtra(pkg_r+".myServiceID", id_ric);
            				startActivity(intentToUI4);
              			}
            	}
            	else {
            		Intent intentToUI4=new Intent(UI2.this, UI4.class);
            		intentToUI4.putExtra(pkg_r+".myServiceID", id_ric);
            		startActivity(intentToUI4);
            	}
            }
        });

               
	} //Fine onCreate()
	
	public void onResume() {
		super.onResume();
			dbHelper.open();
	        //Query al DB: dato l'ID recupero tutta la tupla
	        cr=dbHelper.fetchRecordById(id_ric);
	        cr.moveToNext();
	        
	        //Recupero delle informazioni dalla query fatta al DB
	        nome_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_NAME));
	        timestamp_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_DATE));
	        dataulitmamodifica=cr.getString(cr.getColumnIndex(DbAdapter.KEY_LAST));
	        x_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
	        y_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
	        z_selected=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
	        sovra_ric=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
	        codifica = cr.getString(cr.getColumnIndex(DbAdapter.KEY_IMM));
	        
	        //Chiusura Cursor e DB
	        cr.close();
	        dbHelper.close();
	        ultimavar.setText(dataulitmamodifica);
	}
	
	
	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
			Intent returnIntent = new Intent(UI2.this, UI1.class);
        	startActivity(returnIntent);
        	finish();	
	return;
	}
	
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
