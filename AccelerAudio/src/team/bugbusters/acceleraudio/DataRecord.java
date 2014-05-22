package team.bugbusters.acceleraudio;

import java.util.Random;

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
	private long sendtime;
	private int i,durata_def;
	private String freq;
	private DbAdapter dbHelper;
	private boolean ric_UI3;
	private float NOISE;
	private float[] valprec;
	
	
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

	        	durata_def=intent.getIntExtra("attFineTempo", 0);
	        		
	        }
	        
	        //Parametri di una nuova registrazione
	        else {
	        	datoX=new StringBuilder();
	        	datoY=new StringBuilder();
	        	datoZ=new StringBuilder();
	        	freq=prefs.getString("Campion", "Normale");

	        	durata_def=prefs.getInt("duratadef", 30);
	        
	        }

	        	i=intent.getIntExtra("attCamp", 0);
	        	
	        	valprec=new float[3];
	        	ric_UI3=intent.getBooleanExtra("fromUI3", false);
 	
	        	acquisizione();
       	
	        	//Ogni 25 ms si controlla se il tempo trascorso supera la durata massima impostata
	        	SystemClock.sleep(1000*durata_def);

	        
	 	}
	 
	 
	 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		
		//Ad ogni variazione dell'accelerometro
		public void onSensorChanged(SensorEvent event) {
				
				//Aggiornamento campioni registrati
				//i=i+3;
				//Aggiornamento delle StringBuilder
			
				if(event.values[0]-valprec[0]>NOISE){
					datoX.append((converti(event.values[0]))+" ");
					i++;
					broadcastIntent.putExtra("intPbX", Math.round(Math.abs(event.values[0])));
				}
				
				if(event.values[1]-valprec[1]>NOISE){
					datoY.append((converti(event.values[1]))+" ");
					i++;
					broadcastIntent.putExtra("intPbY", Math.round(Math.abs(event.values[1])));
				}
				if(event.values[2]-valprec[2]>NOISE){
					datoZ.append((converti(event.values[2]))+" ");
					i++;
					broadcastIntent.putExtra("intPbZ", Math.round(Math.abs(event.values[2])));
				}
				
				

				//Aggiornamento delle barre dei 3 assi e dei campioni registrati che vengono visualizzati nella UI3
				if(ric_UI3 && System.currentTimeMillis()-sendtime>100){
					sendtime=System.currentTimeMillis();
					broadcastIntent.putExtra("serCamp",i);
					sendBroadcast(broadcastIntent);
				}
				
				valprec[0]=event.values[0];
				valprec[1]=event.values[1];
				valprec[2]=event.values[2];
		}
	 
	 


	@Override
	public void onDestroy(){
			mSensorManager.unregisterListener(this);
					
			datoX.trimToSize();
			datoY.trimToSize();
			datoZ.trimToSize();
			
			//Se si e' cominciata la registrazione dalla UI3
			if(ric_UI3==true) {

				broadcastIntent.putExtra("ValoreX", datoX.toString());
				broadcastIntent.putExtra("ValoreY", datoY.toString());
				broadcastIntent.putExtra("ValoreZ", datoZ.toString());
				broadcastIntent.putExtra("serFreq", freq);
				broadcastIntent.putExtra("serDur", durata_def);
				broadcastIntent.putExtra("serCamp",i);
				sendBroadcast(broadcastIntent);
			
			}
			
			//Invece se si proviene dal widget
			else {
				String timestamp = DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
											
				dbHelper.open();
				long id=dbHelper.createRecord("Rec_", "", datoX.toString(), datoY.toString(), datoZ.toString(), ""+ prefs.getBoolean("Xselect", true),
						""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true), i, UI5.campToString(prefs.getInt("sovrdef", 0)),
						timestamp, timestamp, null);
				String code = codifica(datoX.toString(),datoY.toString(),datoZ.toString(),timestamp,id);
				dbHelper.updateRecordNameAndImage(id, "Rec_"+id, code);
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
    		valprec[0]=valprec[1]=valprec[2]=0;
    		
    		if(freq.equals("Molto lento")) {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    			NOISE=1.1F;
    		}
    		if(freq.equals("Lento"))   {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    			NOISE=0.8F;
    		}
    		if(freq.equals("Normale")) {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    			NOISE=0.6F;
    		}
    		if(freq.equals("Veloce"))  {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    			NOISE=0.4F;
    		}
    	}
    	
    }
	
        
	//Metodo per la conversione in short che servira' all'AudioTrack
	protected static short converti(float x){
		if(x>32.767) return 3276;
		if(x<-32.768) return -3276;
		else return (short)Math.round(x*100);
	}
	
	
	//Metodo che genera la stringa di numeri che poi verra' elaborata x creare le immagini
	public static String codifica(String s, String p, String q, String time, long id) {
		StringBuilder sb=new StringBuilder();
		Random r=new Random();
		
		int c=r.nextInt(256);
		if(c<10) sb.append("00"+c);
		else if (c>=10 && c<100) sb.append("0"+c);
		else sb.append(""+c);
		
		if (s.length()>=50) {
			if (s.charAt(49)!=' ' && s.charAt(49)!='-') sb.append(s.charAt(49));
			else sb.append(""+r.nextInt(3));
		}
		else sb.append(""+r.nextInt(3));
		
		if (p.length()>=50) {
			if (p.charAt(49)!=' ' && p.charAt(49)!='-') sb.append(p.charAt(49));
			else sb.append(""+r.nextInt(6));
		}
		else sb.append(""+r.nextInt(6));
		
		if (q.length()>=50) {
			if (q.charAt(49)!=' ' && q.charAt(49)!='-') sb.append(q.charAt(49));
			else sb.append(""+r.nextInt(6));
		}
		else sb.append(""+r.nextInt(6));

		sb.append(time.charAt(1));
		sb.append(time.charAt(12));
		sb.append(time.charAt(15));
		long l=id%(1+r.nextInt(255));
		if(l<10) sb.append("00"+l);
		else if (l>=10 && l<100) sb.append("0"+l);
		else sb.append(""+l);
		return sb.toString();
	}
	
	
}
