package team.bugbusters.acceleraudio;

import java.util.Locale;
import java.util.Random;

import team.bugbusters.acceleraudio.UI3.MyUI3Receiver;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
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
	private Intent broadcastIntent,broadcastWidget;
	private SharedPreferences prefs;
	private StringBuilder datoX,datoY,datoZ;
	private long sendtime;
	private int i,j,k,durata_def;
	private String freq;
	private DbAdapter db;
	private boolean ric_UI3,ric_LIL,was_empty=false;
	private float NOISE;
	private float[] valprec;
	
	
	/*-- Constructor --*/
	public DataRecord() {
		super("DataRecord");	
	}
	
	 @Override
	    protected void onHandleIntent(Intent intent) {
		 	prefs = PreferenceManager.getDefaultSharedPreferences(this);
		 	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		 	db = new DbAdapter(this);
		 	
		 	/*-- Broadcast intent used to send data and update UI --*/
		 	broadcastIntent = new Intent();
	        broadcastIntent.setAction(MyUI3Receiver.DATA_RESPONSE);
	        
      
	        /*-- Restore parameters after a pause --*/
	        if(intent.hasExtra("VecchioX")){
	        	datoX = new StringBuilder().append(intent.getStringExtra("VecchioX"));
	        	datoY = new StringBuilder().append(intent.getStringExtra("VecchioY"));
	        	datoZ = new StringBuilder().append(intent.getStringExtra("VecchioZ"));
	        	freq = intent.getStringExtra("attFreq");
	        	durata_def = intent.getIntExtra("attFineTempo", 0);
	        }
	        
	        /*-- Instantiate parameters for a new record --*/
	        else {
	        	datoX = new StringBuilder();
	        	datoY = new StringBuilder();
	        	datoZ = new StringBuilder();
	        	freq = prefs.getString("Campion", "Normale");
	        	durata_def = prefs.getInt("duratadef", 30);
	        }

	        	i = intent.getIntExtra("attCampX", 0);
	        	j = intent.getIntExtra("attCampY", 0);
	        	k = intent.getIntExtra("attCampZ", 0);
	        	
	        	valprec = new float[3];
	        	ric_UI3 = intent.getBooleanExtra("fromUI3", false);
	        	ric_LIL = intent.getBooleanExtra("fromLIL", false);
	        	
	        	acquisizione();
	        	
	        	/*-- Set record timeout --*/
	        	SystemClock.sleep(1000*durata_def);
	        			
	 	}
	 
	 
	 	/*-- Method unused --*/
	 	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		
		
		/*-- Sensor method that record new accelerometer data --*/
		public void onSensorChanged(SensorEvent event) {
				
				broadcastIntent.putExtra("intPbX",0);
				broadcastIntent.putExtra("intPbY",0);
				broadcastIntent.putExtra("intPbZ",0);
			
				/*-- Register accelerometer variations on every axis --*/
			
				if(event.values[0]-valprec[0] > NOISE){
					datoX.append((converti(event.values[0]))+" ");
					i++;
					
					/*-- Update intent extra with the value of X-axis progress bar on UI 3 --*/
					broadcastIntent.putExtra("intPbX",(int) (Math.abs(event.values[0]-valprec[0])));
				}
				
				if(event.values[1]-valprec[1] > NOISE){
					datoY.append((converti(event.values[1]))+" ");
					j++;
					
					/*-- Update intent extra with the value of Y-axis progress bar on UI 3 --*/
					broadcastIntent.putExtra("intPbY", (int) (Math.abs(event.values[1]-valprec[1])));
				}
				if(event.values[2]-valprec[2] > NOISE){
					datoZ.append((converti(event.values[2]))+" ");
					k++;
					
					/*-- Update intent extra with the value of Z-axis progress bar on UI 3 --*/
					broadcastIntent.putExtra("intPbZ", (int) (Math.abs(event.values[2]-valprec[2])));
				}
				
				
				/*-- Every 200ms the Service notify only UI 3 with samples recorded on every axis  --*/
				if(ric_UI3 && System.currentTimeMillis()-sendtime > 200){
					sendtime=System.currentTimeMillis();
					broadcastIntent.putExtra("serCampX",i);
					broadcastIntent.putExtra("serCampY",j);
					broadcastIntent.putExtra("serCampZ",k);
					sendBroadcast(broadcastIntent);
				}
				
				
				valprec[0] = event.values[0];
				valprec[1] = event.values[1];
				valprec[2] = event.values[2];
		}
	 
	 

	@Override
	public void onDestroy(){
		
			/*-- Unregister sensor (accelerometer) listener --*/
			mSensorManager.unregisterListener(this);
					
			datoX.trimToSize();
			datoY.trimToSize();
			datoZ.trimToSize();
			
			
			/*-- If record started from UI 3 then send update values --*/
			if(ric_UI3 == true) {
				broadcastIntent.putExtra("ValoreX", datoX.toString());
				broadcastIntent.putExtra("ValoreY", datoY.toString());
				broadcastIntent.putExtra("ValoreZ", datoZ.toString());
				broadcastIntent.putExtra("serFreq", freq);
				broadcastIntent.putExtra("serDur", durata_def);
				broadcastIntent.putExtra("serCampX",i);
				broadcastIntent.putExtra("serCampY",j);
				broadcastIntent.putExtra("serCampZ",k);
				sendBroadcast(broadcastIntent);		
			}
			
			/*-- Otherwise record started from a widget then finish and insert he record on DB --*/
			else {
				
				String timestamp = DateFormat.format("dd-MM-yyyy kk:mm:ss", new java.util.Date()).toString();
				String dur = calcoloTempo(i,j,k,prefs.getBoolean("Xselect", true),prefs.getBoolean("Yselect", true),
										prefs.getBoolean("Zselect", true),prefs.getInt("sovrdef", 0));	
				
				try {
					db.open();
					
					/*-- Check if Database is empty --*/
					if(db.fetchAllRecord().getCount() == 0)
						was_empty = true;
					
					long id = db.createRecord("Rec_", dur, datoX.toString(), datoY.toString(), datoZ.toString(), ""+ prefs.getBoolean("Xselect", true),
							""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true), i,j,k, ""+prefs.getInt("sovrdef", 0),
							timestamp, timestamp, null);

					
					String code = codifica(datoX.toString(),datoY.toString(),datoZ.toString(),timestamp,id);
					db.updateRecordNameAndImage(id, "Rec_"+id, code);
					db.close();
					
					/*-- If Database was empty before then notify widget big --*/
					if(was_empty) {
						was_empty = false;
						Intent notifica = new Intent(DataRecord.this,widget_big.class);
						notifica.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
						sendBroadcast(notifica);	
					}
					
					
				} catch (SQLException e) {
					Toast.makeText(DataRecord.this, R.string.dbError, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
				
				/*-- Signal to widget that the REC is over due to time elapsed expired --*/
				
			    /*-- Intent used to communicate with little or big widget --*/
				if(ric_LIL) broadcastWidget = new Intent(this,widget_lil.class);
				else  {
					broadcastWidget = new Intent(this,widget_big.class);
					broadcastWidget.putExtra("RS", true);
				}
				
				broadcastWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				broadcastWidget.putExtra("Terminata",true);
				sendBroadcast(broadcastWidget);
				
			}
			
			super.onDestroy();
		
	}
	
	
	/*-- Method that start data capture from accelerometer --*/
	protected void acquisizione(){
		
		/*-- Check accelerometer --*/
    	if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
    		
    		/*-- Intent used to communicate with little or big widget --*/
			if(ric_LIL)  broadcastWidget = new Intent(this,widget_lil.class);
			else  broadcastWidget = new Intent(this,widget_big.class);
			
    		broadcastWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    		broadcastWidget.putExtra("NoAccelerometro",true);
			sendBroadcast(broadcastWidget);
    	}
    	
    	else {
    		sendtime=System.currentTimeMillis();	
    		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    		valprec[0] = valprec[1] = valprec[2] = 0;
    		
    		/*-- Register sensor (accelerometer) listener --*/
    		if(freq.equals("Lento")) {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    			NOISE = 1.0F;
    		}
    		if(freq.equals("Normale"))   {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    			NOISE = 0.7F;
    		}
    		if(freq.equals("Veloce")) {
    			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    			NOISE = 0.5F;
    		}

    	}
    	
    }
	
        
	/*-- Method used to convert accelerometer values into short numbers used by AudioTrack for playback --*/
	protected static short converti(float x){
		if(x>32.767) return 3276;
		if(x<-32.768) return -3276;
		else return (short)Math.round(x*100);
	}
	
	
	/*-- Method used to calculate record playback time --*/
	public static String calcoloTempo(int n_campX,int n_campY,int n_campZ, boolean cX, boolean cY, boolean cZ, int sovra){
		int somma = n_campX+n_campY+n_campZ;
		int s = PlayRecord.calcoloSovra(sovra,somma);
		int dimX = 2*(PlayRecord.MINSIZE+s*n_campX);
		int dimY = 2*(PlayRecord.MINSIZE+s*n_campY);
		int dimZ = 2*(PlayRecord.MINSIZE+s*n_campZ);
		
		if(cX && cY && cZ) return String.format(Locale.ITALY,"%04d",(dimX+dimY+dimZ)/(PlayRecord.AT_SAMPLE_RATE/1000));
		else if(cX && cY) return String.format(Locale.ITALY,"%04d",(dimX+dimY)/(PlayRecord.AT_SAMPLE_RATE/1000));
		else if(cX && cZ) return String.format(Locale.ITALY,"%04d",(dimX+dimZ)/(PlayRecord.AT_SAMPLE_RATE/1000));
		else if(cY && cZ) return String.format(Locale.ITALY,"%04d",(dimY+dimZ)/(PlayRecord.AT_SAMPLE_RATE/1000));
		else if(cX) return String.format(Locale.ITALY,"%04d",dimX/(PlayRecord.AT_SAMPLE_RATE/1000));
		else if(cY) return String.format(Locale.ITALY,"%04d",dimY/(PlayRecord.AT_SAMPLE_RATE/1000));
		else return String.format(Locale.ITALY,"%04d",dimZ/24);
	}

	
	
	/*-- Method used to create a string that will be processed to create the record image --*/
	public static String codifica(String s, String p, String q, String time, long id) {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		
		int c = 110+r.nextInt(146);
		if(c<10) sb.append("00"+c);
		else if (c >= 10 && c < 100) sb.append("0"+c);
		else sb.append(""+c);
		
		if (s.length() >= 50 && p.length() >= 50 && q.length() >= 50)
		try {
			int k = Integer.parseInt(s.charAt(49) + "" + p.charAt(49) + ""+ q.charAt(49));
			k = Math.abs(Integer.valueOf(k+r.nextInt(10000)).byteValue() & 0xFF);
			if(k < 10) sb.append("00"+k);
			else if (k >= 10 && k<100) sb.append("0"+k);
			else sb.append(""+k);	
		} 
		catch (NumberFormatException e) {
			int k = Math.abs(Integer.valueOf(r.nextInt(10000)).byteValue() & 0xFF);
			if(k<10) sb.append("00"+k);
			else if (k >= 10 && k < 100) sb.append("0"+k);
			else sb.append(""+k);	
		}
		
		
		else{
			int k = Math.abs(Integer.valueOf(r.nextInt(10000)).byteValue() & 0xFF);
			if(k < 10) sb.append("00"+k);
			else if (k >= 10 && k < 100) sb.append("0"+k);
			else sb.append(""+k);	
		}
			
		int h = Integer.parseInt(time.charAt(1)+""+time.charAt(12)+""+time.charAt(15));
		h = Math.abs(Integer.valueOf(h+r.nextInt(10000)).byteValue() & 0xFF);
		if(h < 10) sb.append("00"+h);
		else if (h >= 10 && h < 100) sb.append("0"+h);
		else sb.append(""+h);

		int l = (int) Math.abs(Math.sin(((double)id)/10)*255);
		if(l < 10) sb.append("00"+l);
		else if (l >= 10 && l < 100) sb.append("0"+l);
		else sb.append(""+l);
		return sb.toString();
	}
	
	
}
