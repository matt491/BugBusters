package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class UI2 extends Activity {

	private CheckBox chX;
	private CheckBox chY;
	private CheckBox chZ;
	private SeekBar sb;
	private TextView nomevar;
	private TextView timevar;
	private TextView ultimavar;
	private TextView result;
	private Button fineui2;
	private String timestamp_ric;
	private String nome_ric;
	private String sovra_ric;
	private String dataulitmamodifica;
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
     
        nomevar= (TextView)findViewById(R.id.nomeSessione);
        timevar= (TextView)findViewById(R.id.datareg);
        ultimavar= (TextView)findViewById(R.id.dataulmod);
        result= (TextView)findViewById(R.id.res);
        fineui2=(Button)findViewById(R.id.toPlay);
        chX=(CheckBox)findViewById(R.id.checkBoxX);
        chY=(CheckBox)findViewById(R.id.checkBoxY);
        chZ=(CheckBox)findViewById(R.id.checkBoxZ);
        sb = (SeekBar)findViewById(R.id.seekBar1);
        
   
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
        sovra_ric=cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE));
        
        //Chiusura Cursor e DB
        cr.close();
        dbHelper.close();
        
        chX.setChecked(x_selected);
        chY.setChecked(y_selected);
        chZ.setChecked(z_selected);
        nomevar.setText(nome_ric);
        timevar.setText(timestamp_ric);
        ultimavar.setText(dataulitmamodifica);
        result.setText(sovra_ric);
        sb.setProgress(UI5.stringToCamp(sovra_ric));
        
        
       
        //Barra del Sovracampionamento
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            	if(progress<15){
            		seekBar.setProgress(0);
            		result.setText(UI5.campToString(sb.getProgress()));
            		}
                if(progress>15 && progress<40){
                	seekBar.setProgress(25);
                	result.setText(UI5.campToString(sb.getProgress()));
                	}
                if(progress>40 && progress<60){
                	seekBar.setProgress(50);
                	result.setText(UI5.campToString(sb.getProgress()));
                	}
                if(progress>60 && progress<80){
                	seekBar.setProgress(75);
                	result.setText(UI5.campToString(sb.getProgress()));
                	}
                if(progress>80){
                	seekBar.setProgress(100);
                	result.setText(UI5.campToString(sb.getProgress()));
                	}
            }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				result.setText(UI5.campToString(sb.getProgress()));
				
			}});
        
        
        fineui2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	         	
            if(chX.isChecked()!=x_selected || chY.isChecked()!=y_selected || chZ.isChecked()!=z_selected || sb.getProgress()!=UI5.stringToCamp(sovra_ric)){
            		
            		dataulitmamodifica=DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
            		dbHelper.open();
            		dbHelper.updateRecord(id_ric, nome_ric, ""+chX.isChecked(),""+chY.isChecked(),""+chZ.isChecked(),
							UI5.campToString(sb.getProgress()), dataulitmamodifica);
            		dbHelper.close();
            	}
            	
            	//Alla UI4 viene spedito l'ID del record creato/aggiornato
            	Intent intentToUI4=new Intent(UI2.this, UI4.class);
            	
            	intentToUI4.putExtra(pkg_r+".myServiceID", id_ric);
                startActivity(intentToUI4);

            	}
            });

               
	} //Fine onCreate()
	

	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
			Intent returnIntent = new Intent(getApplicationContext(), UI1.class);
        	startActivity(returnIntent);
        	finish();	
	return;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Preferenze").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
            		Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
                    startActivity(prefIntentUI5);
                    return true;
            }
		});;

	        return true;
	}

}
