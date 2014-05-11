package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class UI3 extends Activity {
	
    private String datoX,datoY,datoZ;
    private ProgressBar pbX,pbY,pbZ,pb;
	private int i,end_time;									
    private double time;							
    private String freq_curr;						
    private String nome; 								// Nome inserito dall'utente tramite EditText
    private String ts;
    private String pkg;
    private Button pause_resume,stop,rec,avan;								
    private EditText nome_music;						//Campo di testo del nome della registrazione
    private TextView t,varcamp;
    Intent intent,intentToSer;
    private SharedPreferences prefs;
    private DbAdapter dbHelper;
    private MyUI3Receiver receiver;
    private IntentFilter filter;
    private ToggleButton toggle;
    private boolean in_pausa=false;
    		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        receiver = new MyUI3Receiver();
        intentToSer = new Intent(UI3.this, DataRecord.class);
        filter = new IntentFilter(MyUI3Receiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filter);
        
        setContentView(R.layout.ui3_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper = new DbAdapter(this);
        
        
        
        //Toggle button che blocca la rotazione dello schermo
        toggle = (ToggleButton) findViewById(R.id.toggleButton1);
    
        //Intent predisposto per passare alla UI2
        intent=new Intent(getApplicationContext(), UI2.class);
        
        pause_resume=(Button)findViewById(R.id.pause_res);		//Tasto pause/resume
        stop=(Button)findViewById(R.id.stop);					//Tasto Stop
        rec=(Button)findViewById(R.id.record);					//Tasto Record
        avan=(Button)findViewById(R.id.avanz);					//Tasto Avanti
        nome_music = (EditText)findViewById(R.id.nome);			//Campo di testo del nome della registrazione
        t= (TextView)findViewById(R.id.last);					//Mostra la durata della registrazione in corso
      
        //3 ProgressBar per visualizzare la variazione di accelerazione lungo i 3 assi
		 pbX=(ProgressBar)findViewById(R.id.x_axis);
		 pbY=(ProgressBar)findViewById(R.id.y_axis);
		 pbZ=(ProgressBar)findViewById(R.id.z_axis);
		 pb=(ProgressBar)findViewById(R.id.progressBar1);
				 
		//Visualizza il numero di campioni registrati
		varcamp=(TextView)findViewById(R.id.campioni);

        
        //Diabilito i pulsanti all'inizio
        pause_resume.setEnabled(false);
        stop.setEnabled(false);
        avan.setEnabled(false);
        rec.setEnabled(true);
        
        
        //Toggle premuto
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
					Display disp = wm.getDefaultDisplay();
					int orientation = disp.getRotation();
					
					if(orientation==0) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
					if(orientation==1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					//Non è supportato nelle API8, perchè nelle API8 non era previsto il reverse landscape
					if(orientation==3) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
								
				}
				else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						
			}
		});
       
        //Tasto Pausa/Resume premuto
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!in_pausa){
            		pause_resume.setText("Riprendi");
            		in_pausa=true;
            		intentToSer.putExtra("fromUI3", true);
            		stopService(intentToSer);
            	}
            	else {
            		pause_resume.setText("Pausa");
            		intentToSer.putExtra("fromUI3", true);
            		intentToSer.putExtra("VecchioX", datoX);
            		intentToSer.putExtra("VecchioY", datoY);
            		intentToSer.putExtra("VecchioZ", datoZ);
            		intentToSer.putExtra("attFreq", freq_curr);
            		intentToSer.putExtra("attFineTempo", end_time);
            		intentToSer.putExtra("attTempo", time);
            		intentToSer.putExtra("attCamp", i);
            		in_pausa=false;
            		startService(intentToSer);
            	}
            }
        });
        

        //Tasto Stop premuto
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            		avan.setEnabled(true);
            		pause_resume.setEnabled(false);
            		stop.setEnabled(false);
            		intentToSer.putExtra("fromUI3", true);
            		Toast.makeText(getApplicationContext(), "Registrazione Terminata", Toast.LENGTH_SHORT).show();
            		stopService(intentToSer);
            		
            	}
        });
         
        //Tasto Record premuto
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	if(toggle.isChecked()){
            		avan.setEnabled(false);
            		pause_resume.setEnabled(true);
            		stop.setEnabled(true);
            		rec.setEnabled(false);
            		toggle.setEnabled(false);
            		end_time=prefs.getInt("duratadef", 50);
            		pb.setMax(end_time);
            		intentToSer.putExtra("fromUI3", true);
            		startService(intentToSer);
            	}
            	else Toast.makeText(getApplicationContext(), "Devi bloccare lo Schermo", Toast.LENGTH_SHORT).show();
            }
        });
        
        //Tasto Avanti premuto
        avan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	pkg=getPackageName();
            	String nomeinserito=nome_music.getText().toString();
            	
            	//Se il nome inserito e la stringa vuota o  gia presente nel DB allora	
            	if((nomeinserito.equals("")) || sameName(nomeinserito))
            		Toast.makeText(getApplicationContext(), "Immetere un nome valido!", Toast.LENGTH_SHORT).show();
            		
            	else {	
            	nome = nome_music.getText().toString();
            	ts = DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
            	
            	intent.putExtra(pkg+".myDurata", time);
            	intent.putExtra(pkg+".myNome", nome);
            	intent.putExtra(pkg+".myTimeStamp", ts); 
            	intent.putExtra(pkg+".myNCamp", i);
                intent.putExtra(pkg+".myDatoX", datoX.toString());
                intent.putExtra(pkg+".myDatoY", datoY.toString());
                intent.putExtra(pkg+".myDatoZ", datoZ.toString());
               
            	startActivity(intent);
            	finish();
            	}
            	
            	}
            });
               
    }    //FINE onCreate()
    
 
     @Override
     public void onDestroy() {
         this.unregisterReceiver(receiver);
         super.onDestroy();
     }

	
	//Metodo che controlla se e gia presente un NOME di una music session nel DB
	public boolean sameName(String s){
			dbHelper.open();
			Cursor cursor=dbHelper.fetchRecordByFilter(s);
			while (cursor.moveToNext()) {
				String rNAME = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME) );
				if(s.equals(rNAME)) {
					cursor.close();
					dbHelper.close();
					return true;
				}
			} 
			cursor.close();
			dbHelper.close();
			return false;
	}
	 
	//Menu Option, passa alla UI5
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
	

	   public class MyUI3Receiver extends BroadcastReceiver{

		   public static final String PROCESS_RESPONSE = "team.bugbusters.acceleraudio.intent.action.PROCESS_RESPONSE";
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            pb.setProgress(intent.getIntExtra("intPb", 0));
	            pbX.setProgress(intent.getIntExtra("intPbX", 0));
	            pbY.setProgress(intent.getIntExtra("intPbY", 0));
	            pbZ.setProgress(intent.getIntExtra("intPbZ", 0));
	            
	            i=intent.getIntExtra("serCamp",1);
	            varcamp.setText(""+i);
	            datoX=intent.getStringExtra("ValoreX");
	            datoY=intent.getStringExtra("ValoreY");
	            datoZ=intent.getStringExtra("ValoreZ");
	            freq_curr=intent.getStringExtra("serFreq");
	            end_time=intent.getIntExtra("serDur",0);
	            time=intent.getDoubleExtra("serTempo", 0);
	            t.setText(""+time);
	            
	            if(intent.getBooleanExtra("STOP", false)){
	            	
	            	avan.setEnabled(true);
            		pause_resume.setEnabled(false);
            		stop.setEnabled(false);
	            }
	            
	        }


	        }

	
	
	}