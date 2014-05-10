package team.bugbusters.acceleraudio;

import team.bugbusters.acceleraudio.UI3.MyUI3Receiver;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class DataRecord extends IntentService implements SensorEventListener {

	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	private Intent broadcastIntent;
	private SharedPreferences prefs;
	private StringBuilder datoX,datoY,datoZ;
	private double tempo;
	private long starttime,sendtime;
	private int i,durata_def;
	private String freq;
	private DbAdapter dbHelper;
	
	public DataRecord() {
		super("DataRecord");
		// TODO Auto-generated constructor stub
	}
	
	 @Override
	    protected void onHandleIntent(Intent intent) {
		 	prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		 	
		 	//Intent usato per comunicare con il Broadcast Receiver della UI3
		 	broadcastIntent = new Intent();
	        broadcastIntent.setAction(MyUI3Receiver.PROCESS_RESPONSE);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        
	        //Inizializzo il database
	        dbHelper = new DbAdapter(this);
	        
	        //Variabile usata per il refresh dei dati sulla UI3
	        sendtime=System.currentTimeMillis();
	        
	        //Ripristino parametri dopo una pausa
	        if(intent.hasExtra("VecchioX")){
	        	datoX=new StringBuilder().append(intent.getStringExtra("VecchioX"));
	        	datoY=new StringBuilder().append(intent.getStringExtra("VecchioY"));
	        	datoZ=new StringBuilder().append(intent.getStringExtra("VecchioZ"));
	        	freq=intent.getStringExtra("attFreq");
	        	tempo=intent.getDoubleExtra("attTempo", 0);
	        	durata_def=intent.getIntExtra("attFineTempo", 0);
	        	starttime=System.currentTimeMillis()-(long)(tempo*1000); 	
	        }
	        
	        //Parametri di una nuova registrazione
	        else {
	        	datoX=new StringBuilder();
	        	datoY=new StringBuilder();
	        	datoZ=new StringBuilder();
	        	freq=prefs.getString("Campion", "Molto lento");
	        	tempo=0;
	        	durata_def=prefs.getInt("duratadef", 50);
	        	starttime=System.currentTimeMillis();
	        }

	        	i=intent.getIntExtra("attCamp", 1);
	        	        	
	        	acquisizione();
       	
	        	//Ogni 25 ms controlla se il tempo trascorso supera la durata massima impostata
	        	while(tempo<durata_def)	SystemClock.sleep(35);
	        
	        	//Se si viene dalla UI2 si termina qua
	        	
	        	//Altrimenti superata l'istruzione sopra continuo come se fossi statom lanciato dal widget
	        
	        
	 	}
	 
	 
	 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		
		//Ad ogni variazione dell'accelerometro
		public void onSensorChanged(SensorEvent event) {
				
				//Aggiornamento delle StringBuilder
			    datoX.append(converti(event.values[0])+" ");
				datoY.append(converti(event.values[1])+" ");
				datoZ.append(converti(event.values[2])+" ");
				
				//Aggiornamento del tempo attuale
				tempo=((double)((System.currentTimeMillis()-starttime)/100))/10;
				
				//Aggiornamento campioni registrati
				i=i+3;
				
				//Aggiornamento delle barre dei 3 assi, del tempo e dei campioni registrati che vengono visualizzati nella UI3
				if(System.currentTimeMillis()-sendtime>100){
					sendtime=System.currentTimeMillis();
					broadcastIntent.putExtra("intPb", (int)Math.round(tempo));
					broadcastIntent.putExtra("intPbX", Math.round(Math.abs(event.values[0])));
					broadcastIntent.putExtra("intPbY", Math.round(Math.abs(event.values[1])));
					broadcastIntent.putExtra("intPbZ", Math.round(Math.abs(event.values[2])));
					broadcastIntent.putExtra("serTempo",tempo);
					broadcastIntent.putExtra("serCamp",i);
					sendBroadcast(broadcastIntent);
				}
		}
	 
	 


	@Override
	public void onDestroy(){
			mSensorManager.unregisterListener(this);
			
			datoX.trimToSize();
			datoY.trimToSize();
			datoZ.trimToSize();
			
			if(tempo>=durata_def){
				Toast.makeText(getApplicationContext(), "Registrazione Terminata", Toast.LENGTH_SHORT).show();
				broadcastIntent.putExtra("STOP", true);
			}
			
			broadcastIntent.putExtra("ValoreX", datoX.toString());
			broadcastIntent.putExtra("ValoreY", datoY.toString());
			broadcastIntent.putExtra("ValoreZ", datoZ.toString());
			broadcastIntent.putExtra("serFreq", freq);
			broadcastIntent.putExtra("serDur", durata_def);
			broadcastIntent.putExtra("serTempo", tempo);
			broadcastIntent.putExtra("serCamp",i);
			sendBroadcast(broadcastIntent);
			super.onDestroy();
		
	}
	
	
    protected void acquisizione(){
    	if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null)
    		Toast.makeText(getApplicationContext(), "Impossibile registrare!", Toast.LENGTH_SHORT).show();
    		
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if(freq.equals("Molto lento")) 	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	if(freq.equals("Lento"))	   	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    	if(freq.equals("Normale")) 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    	if(freq.equals("Veloce"))		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    	Toast.makeText(getApplicationContext(), "Registrazione iniziata", Toast.LENGTH_SHORT).show();
    	
    	
    }
	
        
	//Metodo per la conversione in short che servira' all'AudioTrack
	public static short converti(float x){
		if(x>32.767) return 32767;
		if(x<-32.768) return -32768;
		else return (short)Math.round(x*1000);
	}
	
}
