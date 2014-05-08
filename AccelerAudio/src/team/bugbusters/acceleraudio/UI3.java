package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class UI3 extends Activity {
	
    private StringBuilder datoX;
    private StringBuilder datoY;
    private StringBuilder datoZ;
    private ProgressBar pbX,pbY,pbZ,pb;
	private int i=0;									//i: indice dei campioni
	private long starttime;
    private double millis,m=0;							//variabile usata per tenere traccia della durata della registrazione
    private String freq1;						
    private String nome;								// Nome inserito dall'utente tramite EditText
    private String ts;
    private String pkg;
    private String prefix="";
    private Button pause,resume,stop,rec,avan;									
    private EditText nome_music;						//Campo di testo del nome della registrazione
    private TextView t,varcamp;
    Intent intent,msgIntent;
    private SharedPreferences prefs;
    private DbAdapter dbHelper;
    private MyUI3Receiver receiver;
    private IntentFilter filter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        receiver = new MyUI3Receiver();
        msgIntent = new Intent(UI3.this, DataRecord.class);
        filter = new IntentFilter(MyUI3Receiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filter);
        
        setContentView(R.layout.ui3_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper = new DbAdapter(this);
        
        datoX=new StringBuilder();
        datoY=new StringBuilder();
        datoZ=new StringBuilder();
        
        //Intent predisposto per passare alla UI2
        intent=new Intent(getApplicationContext(), UI2.class);
        
        pause=(Button)findViewById(R.id.pause);					//Tasto Pause
        resume=(Button)findViewById(R.id.resume);				//Tasto Resume
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

        t.setText("Tempo: ");
        
        //Diabilito i pulsanti all'inizio
        pause.setEnabled(false);
        resume.setEnabled(false);
        stop.setEnabled(false);
        avan.setEnabled(false);
        
       
        //Tasto Pausa premuto
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	millis=((double)(System.currentTimeMillis() - starttime)/1000)+millis;
            	m=arrotondaTempo(millis);
            	t.setText("Tempo: "+m);
            	pause.setEnabled(false);
            	resume.setEnabled(true);
            	Toast.makeText(getApplicationContext(),"Registrazione in pausa",Toast.LENGTH_SHORT).show();
            	
            	msgIntent.putExtra("tempocorr",m);
            	startService(msgIntent);
            	//pausa();
            	}
            });
        
        //Tasto Resume premuto
        resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	starttime=System.currentTimeMillis();
            	resume.setEnabled(false);
            	pause.setEnabled(true);
            		Toast.makeText(getApplicationContext(), "Registrazione ripresa", Toast.LENGTH_SHORT).show();
            		
            		
            	//	acquisizione();
            }
            	
        });
        
        //Tasto Stop premuto
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(pause.isEnabled()){
            		millis=((double)(System.currentTimeMillis() - starttime)/1000)+millis;
            		m=arrotondaTempo(millis);
            		t.setText("Tempo: "+m);
            	}
            	
            	stopService(msgIntent);
            	//arresto();
            	}
        });
         
        //Tasto Record premuto
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            		starttime=System.currentTimeMillis();
            		freq1=prefs.getString("Campion", "NORMAL");
            		pb.setMax(prefs.getInt("duratadef", 20));
            		avan.setEnabled(false);
            		pause.setEnabled(true);
            		stop.setEnabled(true);
            		rec.setEnabled(false);
            		Toast.makeText(getApplicationContext(), "Registrazione iniziata", Toast.LENGTH_LONG).show();
            		
            		 
            		startService(msgIntent);
            		//acquisizione();
            		
            	}
        });
        
        //Tasto Avanti premuto
        avan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	pkg=getPackageName();
            	String nomeinserito=nome_music.getText().toString();
            	
            	//Se il nome inserito è la stringa vuota o è già presente nel DB allora	
            	if((nomeinserito.equals("")) || sameName(nomeinserito))
            		Toast.makeText(getApplicationContext(), "Immetere un nome valido!", Toast.LENGTH_SHORT).show();
            		
            	else {	
            	nome = nome_music.getText().toString();
            	ts = (DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString());
            	
            	intent.putExtra(pkg+".myDurata", m);
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
    
        /*

    

    protected void arresto(){
    	mSensorManager.unregisterListener(this);
    	Toast.makeText(getApplicationContext(), "Registrazione terminata", Toast.LENGTH_SHORT).show();
    	pause.setEnabled(false);
    	rec.setEnabled(false);
    	resume.setEnabled(false);
    	stop.setEnabled(false);
    	avan.setEnabled(true);
    }
    */
    protected void onResume() {
        super.onResume();
       
    }

 	 protected void onPause() {
        super.onPause();
        
    }
 	 
     @Override
     public void onDestroy() {
         this.unregisterReceiver(receiver);
         super.onDestroy();
     }


	
	
	//Metodo per aggiornare la variabile della durata
	protected double aggiornoTempo(){
		return ((double)(System.currentTimeMillis() - starttime)/1000)+millis;
	}
	
	//Metodo per arrotondare a 2 cifre decimali la durata
	public static double arrotondaTempo(double x){
		x = Math.floor(x*100);
		x = x/100;
		return x;
		}
	

	//Metodo per la conversione in short che servirà all'AudioTrack
	public static short converti(float x){
		if(x>32.767) return 32767;
		if(x<-32.768) return -32768;
		else return (short)Math.round(x*1000);
	}
	
	//Metodo che controlla se � gia presente un NOME di una music session nel DB
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
	            varcamp.setText(""+(intent.getIntExtra("attCamp",0)));
	           // intent.getStringExtra("ValoreX");
	            
	        }


	        }

	
	
	}