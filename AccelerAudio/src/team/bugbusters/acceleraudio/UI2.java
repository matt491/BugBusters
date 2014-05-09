package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

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
	private double dur_ric;
	private String dataulitmamodifica;
	private String datoX;
	private String datoY;
	private String datoZ;
	private int ncamp_ric;
	private long id_ric,id_to_ui4;
	private SharedPreferences prefs;
	private boolean primavolta=true;
	private DbAdapter dbHelper;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui2_layout);
        Intent intent_r=getIntent();
        String pkg_r=getPackageName();
        
        dbHelper = new DbAdapter(this);
       
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
     
        nomevar= (TextView)findViewById(R.id.nomeSessione);
        timevar= (TextView)findViewById(R.id.datareg);
        ultimavar= (TextView)findViewById(R.id.dataulmod);
        result= (TextView)findViewById(R.id.res);
        fineui2=(Button)findViewById(R.id.toPlay);
        chX=(CheckBox)findViewById(R.id.checkBoxX);
        chY=(CheckBox)findViewById(R.id.checkBoxY);
        chZ=(CheckBox)findViewById(R.id.checkBoxZ);
        sb = (SeekBar)findViewById(R.id.seekBar1);
        
               
        //Se si proviene dalla UI3 di registrazione o dalla UI1 allora:
        timestamp_ric=intent_r.getStringExtra(pkg_r+".myTimeStamp");
        nome_ric=intent_r.getStringExtra(pkg_r+".myNome");
        dur_ric=intent_r.getDoubleExtra(pkg_r+".myDurata", 0.0);
        datoX=intent_r.getStringExtra(pkg_r+".myDatoX");
        datoY=intent_r.getStringExtra(pkg_r+".myDatoY");
        datoZ=intent_r.getStringExtra(pkg_r+".myDatoZ");
        ncamp_ric=intent_r.getIntExtra(pkg_r+".myNCamp", 0);
        nomevar.setText(nome_ric);
        timevar.setText(timestamp_ric);
        
        //Se si proviene dalla UI1 allora
        if(intent_r.hasExtra(pkg_r+".myIdFromUI1")){
        	dataulitmamodifica=intent_r.getStringExtra(pkg_r+".myDataUltima");
        	chX.setChecked(Boolean.parseBoolean(intent_r.getStringExtra(pkg_r+".myCheckX")));
        	chY.setChecked(Boolean.parseBoolean(intent_r.getStringExtra(pkg_r+".myCheckY")));
        	chZ.setChecked(Boolean.parseBoolean(intent_r.getStringExtra(pkg_r+".myCheckZ")));
        	sb.setProgress(UI5.stringToCamp(intent_r.getStringExtra(pkg_r+".mySovra")));
        	result.setText(intent_r.getStringExtra(pkg_r+".mySovra"));
        	ultimavar.setText(dataulitmamodifica); 	
        }
        
        //Se invece si proviene dalla UI3 si leggono le impostazioni dalla UI5 e si impostano le 2 date uguali
        else{
        	chX.setChecked(prefs.getBoolean("Xselect", true));
        	chY.setChecked(prefs.getBoolean("Yselect", true));
        	chZ.setChecked(prefs.getBoolean("Zselect", true));
        	sb.setProgress(prefs.getInt("sovrdef", 0));
        	result.setText(UI5.campToString(sb.getProgress()));
        	dataulitmamodifica=timestamp_ric;
        	ultimavar.setText(dataulitmamodifica);
        }
        //Se si proviene dalla UI1 allora

        id_ric=intent_r.getLongExtra(pkg_r+".myIdFromUI1", -1);

       
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
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				result.setText(UI5.campToString(sb.getProgress()));
				
			}});
        
        
        fineui2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	dataulitmamodifica=(DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString());            	
            	if(!(timestamp_ric.equals(dataulitmamodifica))){
            		dataulitmamodifica=(DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString());
                	ultimavar.setText(dataulitmamodifica);
            	}
            	
            	
            	//Inserimento/aggiornamento del DB
            	dbHelper.open();
            	
            	//Se e' -1 vuol dire che hai appena registrato
            	if(id_ric==-1){ //La stringa per l'immagine viene generata solo alla creazione del record(anche la duplicazione crea un record)
            		String cod=codifica(datoX,datoY,datoZ,chX.isChecked(),chY.isChecked(),chZ.isChecked(),sb.getProgress());
            		
            		id_to_ui4=dbHelper.createRecord(nome_ric, ""+dur_ric, datoX, datoY, datoZ, ""+chX.isChecked(),""+chY.isChecked(),
            								""+chZ.isChecked(), ncamp_ric, UI5.campToString(sb.getProgress()), timestamp_ric, dataulitmamodifica, cod);
            	}
            		
            	else {
            		dbHelper.updateRecord(id_ric, nome_ric, ""+chX.isChecked(),""+chY.isChecked(),""+chZ.isChecked(),
            								UI5.campToString(sb.getProgress()), dataulitmamodifica);
            		id_to_ui4=id_ric;
            		
            	}
            		
            	dbHelper.close();
            	
            	//Alla UI4 viene spedito l'ID del record creato/aggiornato
            	Intent intentToUI4=new Intent(getApplicationContext(), UI5.class);
            	String pkg=getPackageName();
            	intentToUI4.putExtra(pkg+".myID", id_to_ui4);
                startActivity(intentToUI4);

            	}
            });

               
	} //Fine onCreate()
	


	//Metodo che genera la stringa di numeri che poi verra' elaborata x creare le immagini
	public String codifica(String s, String p, String q,boolean a,boolean b,boolean c, int t){
		StringBuilder sb=new StringBuilder();
		sb.append(s.charAt(7));
		sb.append(p.charAt(23));
		sb.append(q.charAt(52));
		sb.append(s.charAt(27));
		sb.append(p.charAt(43));
		if(t==0) sb.append(""+0);
		if(t==25) sb.append(""+3);
		if(t==50) sb.append(""+5);
		if(t==75) sb.append(""+7);
		if(t==100) sb.append(""+9);
		if(a==true) sb.append(""+2);
		else sb.append(""+0);
		if(b==true) sb.append(""+5);
		else sb.append(""+0);
		if(c==true) sb.append(""+5);
		else sb.append(""+0);
		return sb.toString();
	}
	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
		if(primavolta){
			Toast.makeText(getApplicationContext(),"Premi di nuovo per uscire",Toast.LENGTH_SHORT).show();
			primavolta=false;
		}
		else{
			Intent returnIntent = new Intent(getApplicationContext(), UI1.class);
        	startActivity(returnIntent);
		}
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
