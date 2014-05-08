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
import android.preference.PreferenceManager;

public class DataRecord extends IntentService implements SensorEventListener {

	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	private Intent broadcastIntent;
	private SharedPreferences prefs;
	private StringBuilder datoX,datoY,datoZ;
	private double tempo,m;
	private long starttime,sendtime;
	private int i,pX,pY,pZ,duratadef;
	private String freq1;
	
	
	
	public DataRecord() {
		super("DataRecord");
		// TODO Auto-generated constructor stub
	}

	 @Override
	    protected void onHandleIntent(Intent intent) {
		 	prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		 	broadcastIntent = new Intent();
	        broadcastIntent.setAction(MyUI3Receiver.PROCESS_RESPONSE);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        
	        sendtime=System.currentTimeMillis();
	        
	        if(intent.hasExtra("VecchioX")){
	        	datoX=new StringBuilder().append(intent.getStringExtra("VecchioX"));
	        	datoY=new StringBuilder().append(intent.getStringExtra("VecchioY"));
	        	datoZ=new StringBuilder().append(intent.getStringExtra("VecchioZ"));
	        	freq1=intent.getStringExtra("attFreq");
	        	tempo=intent.getDoubleExtra("attTempo", 0);
	        }
	        
	        else {
	        	datoX=new StringBuilder();
	        	datoY=new StringBuilder();
	        	datoZ=new StringBuilder();
	        	freq1=prefs.getString("Campion", "NORMAL");
	        	tempo=0;
	        }

	        duratadef=prefs.getInt("duratadef", 50);
	        i=intent.getIntExtra("attCamp", 1);
	        
	        acquisizione();
	        
	        while(true);
	        //Se si viene dalla UI2 si termina qua
	        //se si viene chiamati dal widget si deve fare un altro metodo
	}
	 
	 
	 public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		}
		
		
		//Cuore dell'activity: registra i dati memorizzandoli negli array
		public void onSensorChanged(SensorEvent event) {

			    datoX.append(UI3.converti(event.values[0])+" ");
				datoY.append(UI3.converti(event.values[1])+" ");
				datoZ.append(UI3.converti(event.values[2])+" ");
				//tempo=aggiornoTempo();
				//starttime=System.currentTimeMillis();
				//m=UI3.arrotondaTempo(tempo);
				i++;

				/*Se e' passato mezzo secondo e sto registrando dalla UI3*/
				if(System.currentTimeMillis()-sendtime>300){
					sendtime=System.currentTimeMillis();
				broadcastIntent.putExtra("intPb", (int)Math.round(tempo));
				broadcastIntent.putExtra("intPbX", Math.round(Math.abs(event.values[0])));
				broadcastIntent.putExtra("intPbY", Math.round(Math.abs(event.values[1])));
				broadcastIntent.putExtra("intPbZ", Math.round(Math.abs(event.values[2])));
				broadcastIntent.putExtra("attTempo",m);
				broadcastIntent.putExtra("attCamp",i*3);
				sendBroadcast(broadcastIntent);
				}
		}
	 
	 


	@Override
	public void onDestroy(){
			mSensorManager.unregisterListener(this);
			datoX.trimToSize();
			datoY.trimToSize();
			datoZ.trimToSize();

			broadcastIntent.putExtra("ValoreX", datoX.toString());
			broadcastIntent.putExtra("ValoreY", datoY.toString());
			broadcastIntent.putExtra("ValoreZ", datoZ.toString());
			
			sendBroadcast(broadcastIntent);
			super.onDestroy();
		
	}
	
	
    protected void acquisizione(){
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if(freq1.equals("NORMAL")) 	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	if(freq1.equals("UI"))	   	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    	if(freq1.equals("GAME")) 	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    	if(freq1.equals("FASTEST"))	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }
	
	
}
