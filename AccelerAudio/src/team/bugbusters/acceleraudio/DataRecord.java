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
import android.text.format.DateFormat;
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
	private boolean ric_UI3;
	
	public DataRecord() {
		super("DataRecord");
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

	        	i=intent.getIntExtra("attCamp", 0);
	        	
	        	ric_UI3=intent.getBooleanExtra("fromUI3", false);
 	
	        	acquisizione();
       	
	        	//Ogni 25 ms si controlla se il tempo trascorso supera la durata massima impostata
	        	while(tempo<durata_def)	SystemClock.sleep(25);
	        
	        
	 	}
	 
	 
	 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		
		//Ad ogni variazione dell'accelerometro
		public void onSensorChanged(SensorEvent event) {
				
				//Aggiornamento campioni registrati
				i=i+3;
				//Aggiornamento delle StringBuilder
			    datoX.append(converti(event.values[0])+" ");
				datoY.append(converti(event.values[1])+" ");
				datoZ.append(converti(event.values[2])+" ");
				
				//Aggiornamento del tempo attuale
				tempo=((double)((System.currentTimeMillis()-starttime)/100))/10;
				
				
				
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
			
			//Se si e' cominciata la registrazione dalla UI3
			if(ric_UI3==true) {

				if(tempo>=durata_def){
					tempo=(double)durata_def;
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
			
			}
			
			//Invece se si proviene dal widget
			else {
				String timestamp = DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
				String code = codifica(datoX.toString(), datoY.toString(), datoZ.toString(), prefs.getBoolean("Xselect", true),
						prefs.getBoolean("Yselect", true),prefs.getBoolean("Zselect", true), prefs.getInt("sovrdef", 0));
							
				dbHelper.open();
				long id=dbHelper.createRecord("Rec_", ""+tempo, datoX.toString(), datoY.toString(), datoZ.toString(), ""+ prefs.getBoolean("Xselect", true),
						""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true), i, UI5.campToString(prefs.getInt("sovrdef", 0)),
						timestamp, timestamp, code);
				
				dbHelper.updateRecordName(id, "Rec_"+id);
				dbHelper.close();
			}
			
			super.onDestroy();
		
	}
	
	//Metodo che inizializza l'acquisizione dei dati da parte dell'accelerometro
	protected void acquisizione(){
    	if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null)
    		Toast.makeText(getApplicationContext(), "Impossibile registrare!", Toast.LENGTH_SHORT).show();
    	
    	else {
    		sendtime=System.currentTimeMillis();	
    		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    		if(freq.equals("Molto lento")) 	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    		if(freq.equals("Lento"))	   	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    		if(freq.equals("Normale")) 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    		if(freq.equals("Veloce"))		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    		Toast.makeText(getApplicationContext(), "Registrazione iniziata", Toast.LENGTH_SHORT).show();
    	}
    	
    }
	
        
	//Metodo per la conversione in short che servira' all'AudioTrack
	protected static short converti(float x){
		if(x>32.767) return 32767;
		if(x<-32.768) return -32768;
		else return (short)Math.round(x*1000);
	}
	
	
	//Metodo che genera la stringa di numeri che poi verra' elaborata x creare le immagini
	public static String codifica(String s, String p, String q,boolean a,boolean b,boolean c, int t) {
		StringBuilder sb=new StringBuilder();
		if (s.charAt(7)==' ' || s.charAt(7)=='-') sb.append("8");
			else sb.append(s.charAt(7));
		if (s.charAt(23)==' ' || s.charAt(23)=='-') sb.append("1");
		else sb.append(s.charAt(23));
		if (s.charAt(52)==' ' || s.charAt(52)=='-') sb.append("5");
		else sb.append(s.charAt(52));
		if (s.charAt(27)==' ' || s.charAt(27)=='-') sb.append("6");
		else sb.append(s.charAt(27));
		if (s.charAt(43)==' ' || s.charAt(43)=='-') sb.append("3");
		else sb.append(s.charAt(43));
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
	
	
}
