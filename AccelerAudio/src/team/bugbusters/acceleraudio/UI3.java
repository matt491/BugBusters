package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private long prec;							
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
    private MyCounter timer;
    		
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
					//Non � supportato nelle API8, perch� nelle API8 non era previsto il reverse landscape
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
            		prec=timer.last;
            		timer.cancel();
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
            		intentToSer.putExtra("attCamp", i);
            		in_pausa=false;
            		         		
            		timer=new MyCounter(end_time*1000-prec,100);
            		timer.end=end_time*1000-prec;
            		timer.previous=prec;
            		timer.start();
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
            		timer.cancel();
            		
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
            		end_time=prefs.getInt("duratadef", 30);
            		pb.setMax(end_time);
            		
            		timer=new MyCounter(end_time*1000,100);
            		timer.end=end_time*1000;	
            		timer.start();
            		
            		intentToSer.putExtra("fromUI3", true);
            		startService(intentToSer);
            	}
            	else Toast.makeText(getApplicationContext(), R.string.lockScreen, Toast.LENGTH_SHORT).show();
            }
        });
        
        //Tasto Avanti premuto
        avan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	pkg=getPackageName();
            	String nomeinserito=nome_music.getText().toString();
            	
            	//Se il nome inserito e la stringa vuota o  gia presente nel DB allora
            	if(nomeinserito.contains("'") || nomeinserito.contains("_")) {
            		Toast.makeText(getApplicationContext(), R.string.apiceNonConsentito, Toast.LENGTH_LONG).show();;
            	}
            	else if((nomeinserito.equals("")) || UI1.sameName(dbHelper,nomeinserito)) {
            		Toast.makeText(getApplicationContext(), R.string.validName, Toast.LENGTH_SHORT).show();
            	}
            	else {	
            		nome = nome_music.getText().toString();
            		ts = DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
            	
            		dbHelper.open();           	
        		
            		long id_to_ui2=dbHelper.createRecord(nome, "", datoX.toString(), datoY.toString(), datoZ.toString(),
            				""+ prefs.getBoolean("Xselect", true),""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true),
        					i, UI5.campToString(prefs.getInt("sovrdef", 0)), ts, ts, null);
            		
            		String cod=DataRecord.codifica(datoX.toString(),datoY.toString(), datoZ.toString(), ts, id_to_ui2);
        		
            		//Update dei dati immagine
            		dbHelper.updateImageCode(id_to_ui2, cod);
            		
            		//Chiusura del DB
            		dbHelper.close();
        		
            		intent.putExtra(pkg+".myIdToUi2", id_to_ui2);
            		
            		//Decommentando queste due righe di codice si fa si che quando si torna alla UI#1 la music session appena registrata compare in fondo alla 
            		//lista (e l'INTERA lista torna in disordine anche se la si era ordinata precedentemente). Lasciandole commentate invece la lista resta
            		//lista (e l'intera lista torna in disordine anche se la si era ordinata precedentemente). Lasciandole commentate invece la lista resta
            		//disordinata se non la si era ordinata oppure resta ordinata e la music session appena registrata viene aggiunta al giusto posto nella lista se la si era precedentemente ordinata.
            		//Come � meglio fare? Boh. Il punto � che per fare una cosa fatta bene, ogni volta che si preme su ordina dovrebbe essere ordinato anche l'intero database... ma probabilmente � un casino!
            		
            		//Editor prefsEditor = prefs.edit();
            		//prefsEditor.putBoolean("sorted", false).commit();
            		
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

	 
	//Menu Option, passa alla UI5
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.option_menu, menu);
 		return true;
 		}
     
     @Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem ordina = menu.findItem(R.id.Ordina);
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
	
	  public class MyCounter extends CountDownTimer{
		 private long end;
		 private long last;
		 private long previous=0;
	        public MyCounter(long millisInFuture, long countDownInterval) {
	            super(millisInFuture, countDownInterval);
	        }
	 
	        @Override
	        public void onFinish() {
	            t.setText((float)((end+previous)/100)/10+"");
	            pb.setProgress((int)(end+previous)/1000);
	            Toast.makeText(UI3.this, R.string.registrationEnd, Toast.LENGTH_SHORT).show();
	            stopService(intentToSer);
	            avan.setEnabled(true);
        		pause_resume.setEnabled(false);
        		stop.setEnabled(false);
	        }
	 
	        @Override
	        public void onTick(long millisUntilFinished) {
	            t.setText((float)((previous+end-millisUntilFinished)/100)/10 +"");
	            pb.setProgress((int)(previous+end-millisUntilFinished)/1000);
	            last=previous+end-millisUntilFinished;
	        }
	    }
	

	   public class MyUI3Receiver extends BroadcastReceiver{

		   public static final String PROCESS_RESPONSE = "team.bugbusters.acceleraudio.intent.action.PROCESS_RESPONSE";
	        @Override
	        	public void onReceive(Context context, Intent intent) {
	        		pbX.setProgress(intent.getIntExtra("intPbX", 0));
	        		pbY.setProgress(intent.getIntExtra("intPbY", 0));
	        		pbZ.setProgress(intent.getIntExtra("intPbZ", 0));
	            
	        		i=intent.getIntExtra("serCamp",0);
	        		varcamp.setText(""+i);
	        		datoX=intent.getStringExtra("ValoreX");
	        		datoY=intent.getStringExtra("ValoreY");
	        		datoZ=intent.getStringExtra("ValoreZ");
	        		freq_curr=intent.getStringExtra("serFreq");
	        		end_time=intent.getIntExtra("serDur",0);       
	        	}
	        }

	   
		//Quando viene premuto il tasto Back
		@Override
		public void onBackPressed() {
				if(timer!=null) timer.cancel();
    			stopService(intentToSer);
				Intent returnIntent = new Intent(getApplicationContext(), UI1.class);
	        	startActivity(returnIntent);
	        	finish();	

		}
	   
	
	}
