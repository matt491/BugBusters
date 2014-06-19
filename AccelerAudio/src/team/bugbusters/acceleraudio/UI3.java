package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class UI3 extends Activity {
	
    private String datoX,datoY,datoZ;
    private ProgressBar pbX,pbY,pbZ,pb;
	private int i,j,k,end_time;									
    private long prec;							
    private String freq_curr;						
    private String nome; 								
    private String ts;
    private String pkg;
    private Button pause_resume,stop,rec,avan;								
    private EditText nome_music;
    private TextView timeView,varcamp;
    private CheckBox cb;
    Intent intent,intentToSer;
    private SharedPreferences prefs;
    private DbAdapter db;
    private MyUI3Receiver receiver;
    private boolean in_pausa=false, was_empty=false;
    private boolean isChecked;
    private RecordCounter timer;
    private final long INTERVALLO=100L;
    private PackageManager packageManager;
    private AlertDialog alertDialog;
    private static boolean alreadyShowed = false;;
    		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
	        isChecked = savedInstanceState.getBoolean("isChecked", false);
	        
	        /*-- Show Keyboard if it was already visible before --*/
	  	    if(savedInstanceState.getBoolean("KeyboardVisible",false))
	  	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        
        setContentView(R.layout.ui3_layout);
        packageManager = getApplicationContext().getPackageManager();
        
        intentToSer = new Intent(UI3.this, DataRecord.class);
        intent=new Intent(UI3.this, UI2.class);     
        
        /*-- Create and register receiver which update time progress bar and axes progress bars --*/
        receiver = new MyUI3Receiver();   
        registerReceiver(receiver, new IntentFilter(MyUI3Receiver.DATA_RESPONSE));
        
        
        /*-- Reading Shared Preferences --*/
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DbAdapter(this);
    
        /*-- Set layout view resources --*/
        pause_resume = (Button)findViewById(R.id.pause_res);
        stop = (Button)findViewById(R.id.stop);
        rec = (Button)findViewById(R.id.record);
        avan = (Button)findViewById(R.id.avanz);
        nome_music = (EditText)findViewById(R.id.nome);
        timeView = (TextView)findViewById(R.id.last);
      
        /*-- ProgressBars which show acceleration changes on every axis --*/
		 pbX=(ProgressBar)findViewById(R.id.x_axis);
		 pbY=(ProgressBar)findViewById(R.id.y_axis);
		 pbZ=(ProgressBar)findViewById(R.id.z_axis);
		 
		 /*-- ProgressBar which show recording time --*/
		 pb=(ProgressBar)findViewById(R.id.progressBar1);
				 
		/*-- Show recorded samples --*/
		varcamp=(TextView)findViewById(R.id.campioni);

        
        /*-- Enable/Disable buttons --*/
        pause_resume.setEnabled(false);
        stop.setEnabled(false);
        avan.setEnabled(false);
        rec.setEnabled(true);
       
        /*-- Pause/Resume button pressed --*/
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!in_pausa){
            		pause_resume.setText("Riprendi");
            		in_pausa=true;
            		prec=timer.myCancel();
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
            		intentToSer.putExtra("attCampX", i);
            		intentToSer.putExtra("attCampY", j);
            		intentToSer.putExtra("attCampZ", k);	
            		in_pausa=false;
            		timer=new RecordCounter(end_time*1000-prec, INTERVALLO, prec);
            		timer.start();
            		startService(intentToSer);
            	}
            }
        });
        

        /*-- Stop button pressed --*/
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            		avan.setEnabled(true);
            		pause_resume.setEnabled(false);
            		stop.setEnabled(false);
            		timer.cancel();
            		stopService(intentToSer);		
            	}
        });
         
        /*-- Record button pressed --*/
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	
            	/*-- Check free space before starting data capture (at least 100kb to record a session) --*/
            	StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
				@SuppressWarnings("deprecation")
				long kilobytesAvailable = ((long)stat.getBlockSize() *(long)stat.getAvailableBlocks())/1024;
				
            	if(kilobytesAvailable>=100) {
            		
            		/*-- Check if accelerometer is available --*/
            		if(packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            			
            			/*-- Check if another record is still running --*/
            			if(widget_lil.record_running==false) {
            				
            				/*-- Lock the screen at the current position --*/
            				WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
							Display disp = wm.getDefaultDisplay();
							int orientation = disp.getRotation();
							if(orientation==Surface.ROTATION_0) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
							if(orientation==Surface.ROTATION_90) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
							if(orientation==Surface.ROTATION_270) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
							
							/*-- Set Lock for Record --*/
	            			widget_lil.record_running=true;
	            			
		            		avan.setEnabled(false);
		            		pause_resume.setEnabled(true);
		            		stop.setEnabled(true);
		            		rec.setEnabled(false);
		            		end_time=prefs.getInt("duratadef", 30);
		            		pb.setMax(end_time);
		            		timer=new RecordCounter(end_time*1000, INTERVALLO, 0 );
		            		timer.start();
		            		intentToSer.putExtra("fromUI3", true);
		            		startService(intentToSer);
            			}
	            		else Toast.makeText(UI3.this, R.string.alreadyRecording, Toast.LENGTH_SHORT).show();
            		}
            		else Toast.makeText(UI3.this, R.string.accelUnavailable, Toast.LENGTH_SHORT).show();
            	}
            	else Toast.makeText(UI3.this, R.string.spaceUnavailable, Toast.LENGTH_SHORT).show();
            }
        });
        
        
        /*-- Avanti button pressed --*/
        avan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	pkg=getPackageName();
            	String nomeinserito=nome_music.getText().toString();
            	
            	/*-- If name isn't valid or already exists --*/
            	if(nomeinserito.contains("'") || nomeinserito.contains("_")) {
            		Toast.makeText(UI3.this, R.string.apiceNonConsentito, Toast.LENGTH_LONG).show();;
            	}
            	else if((nomeinserito.equals("")) || UI1.sameName(db,nomeinserito)) {
            		Toast.makeText(UI3.this, R.string.validName, Toast.LENGTH_SHORT).show();
            	}
            	
            	
            	/*-- Otherwise insert the new record into Database --*/
            	else {	
            		nome = nome_music.getText().toString();
            		ts = DateFormat.format("dd-MM-yyyy kk:mm:ss", new java.util.Date()).toString();
            	
            		long dur=DataRecord.calcoloTempo(i,j,k,prefs.getBoolean("Xselect", true),prefs.getBoolean("Yselect", true),
							prefs.getBoolean("Zselect", true),prefs.getInt("sovrdef", 0));	
            		
            		try {
						db.open();		
						
						/*-- Check if Database is empty --*/
						if(db.fetchAllRecord().getCount()==0)
							was_empty=true;
						
	            		long id_to_ui2=db.createRecord(nome, ""+dur , datoX.toString(), datoY.toString(), datoZ.toString(),
	            				""+ prefs.getBoolean("Xselect", true),""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true),
	        					i,j,k, ""+prefs.getInt("sovrdef", 0), ts, ts, null);
	            	    
	            		
	            		/*-- Calculate and update image code of new record --*/
	            		String cod=DataRecord.codifica(datoX.toString(),datoY.toString(), datoZ.toString(), ts, id_to_ui2);
	            		db.updateImageCode(id_to_ui2, cod);
	            		db.close();
	            		
	            		/*-- If Database was empty before then notify widget big --*/
	            		if(was_empty) {
							was_empty=false;
							Intent notifica = new Intent(UI3.this,widget_big.class);
							notifica.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
							sendBroadcast(notifica);	
						}
	            		
	            		/*-- Release Lock for Record --*/
	            		widget_lil.record_running=false;
	            		
	            		/*-- Start UI 2 --*/
	            		intent.putExtra(pkg+".myIdToUi2", id_to_ui2);
	            		startActivity(intent);
	            		finish();
            		
            		} catch (SQLException e) {
						Toast.makeText(UI3.this, R.string.dbError, Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}    
	
            	}
            	
          }
       });
               
    } /*-- OnCreate End --*/
    
    @Override
    public void onResume() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View dialogView = inflater.inflate(R.layout.notshowagain, null);
    	cb = (CheckBox) dialogView.findViewById(R.id.checkBox1);
    	cb.setChecked(isChecked);
    	alert.setView(dialogView);
    	alert.setTitle(R.string.alertTitle);
    	alert.setIcon(android.R.drawable.ic_lock_lock);
    	alert.setCancelable(false);
    	alert.setPositiveButton(R.string.okAlert, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(cb.isChecked()) {
					Editor prefsEditor = prefs.edit();
					prefsEditor.putBoolean("notShowAgain", true).commit();
				}
				dialog.dismiss();
				}	
			});
    	if(!alreadyShowed) {
    	if(!prefs.getBoolean("notShowAgain", false)){
    		alertDialog = alert.create();
    		alertDialog.show();
    		alreadyShowed = true;
    	}
    		
    	}
    	super.onResume();
    }
 
     @Override
     public void onDestroy() {
    	 if(alertDialog != null && alertDialog.isShowing()) {
    		 alertDialog.dismiss();
    		 alreadyShowed = false;
    	 }
         this.unregisterReceiver(receiver);
         super.onDestroy();
     }

	 
	/*-- Option menu --*/
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.option_menu, menu);
 		return true;
 		}
     
     @Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem ordina = menu.findItem(R.id.Or);
 		ordina.setVisible(false);
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		
 		case R.id.Preferenze:
 			Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
 			prefIntentUI5.putExtra("prefFromWidget", false);
            startActivity(prefIntentUI5);
            return(true);
 		}
 		
 		return (super.onOptionsItemSelected(item));
 	}
 	
 	
 	
 	/*-- Customized CountDownTimer used to count up to default duration --*/
	public class RecordCounter extends CountDownTimer{
		 private long end;
		 private long previous;
		 private long curr;
		 
	     public RecordCounter(long millisInFuture, long countDownInterval, long prev) {
	          super(millisInFuture, countDownInterval);
	          previous=prev;
	          end=millisInFuture;	
	      }
	 
	        @Override
	        public void onFinish() {
	        	timeView.setText(String.format("%.1f s", (float)(previous+end)/1000));
	            pb.setProgress((int)(end+previous)/1000);
	            Toast.makeText(UI3.this, R.string.registrationEnd, Toast.LENGTH_SHORT).show();
	            stopService(intentToSer);
	            avan.setEnabled(true);
        		pause_resume.setEnabled(false);
        		stop.setEnabled(false);
	        }
	 
	        @Override
	        public void onTick(long millisUntilFinished) {
	        	curr=millisUntilFinished;
	        	timeView.setText(String.format("%.1f s", (float)(previous+end-curr)/1000));
	            pb.setProgress((int)(previous+end-curr)/1000);
	        }
	        
	        public long myCancel(){
		    	  super.cancel(); 
		    	  return previous+end-curr;
		      }
	 }
	
	   
	 /*-- Customized BroadcastReceiver used to receive data from Data Record class --*/
	 public class MyUI3Receiver extends BroadcastReceiver{

		 public static final String DATA_RESPONSE = "team.bugbusters.acceleraudio.intent.action.DATA_RESPONSE";
	      @Override
	       public void onReceive(Context context, Intent intent) {
	    	  
	    	    /*-- Update progress bars --*/  
        		pbX.setProgress(intent.getIntExtra("intPbX", 0));
        		pbY.setProgress(intent.getIntExtra("intPbY", 0));
        		pbZ.setProgress(intent.getIntExtra("intPbZ", 0));
            
           		/*-- Update recorded samples --*/
        		i=intent.getIntExtra("serCampX",0);
        		j=intent.getIntExtra("serCampY",0);
        		k=intent.getIntExtra("serCampZ",0);
        		varcamp.setText(""+(i+j+k));
        		
        		/*-- Update recorded axes data --*/
        		datoX=intent.getStringExtra("ValoreX");
        		datoY=intent.getStringExtra("ValoreY");
        		datoZ=intent.getStringExtra("ValoreZ");
        		
        		/*-- Data received to maintain current settings (end time and recording samplerate) --*/
        		freq_curr=intent.getStringExtra("serFreq");
        		end_time=intent.getIntExtra("serDur",0);       
	        }
	  }

	   
	  /*-- Back button pressed --*/
	  @Override
	  public void onBackPressed() {
				if(timer!=null) timer.cancel();
    			if(!widget_lil.record_widget_lil && !widget_big.record_widget_big) {
    				stopService(intentToSer);
    				widget_lil.record_running=false;
    			}
    			alreadyShowed = false;
				Intent returnIntent = new Intent(getApplicationContext(), UI1.class);
	        	startActivity(returnIntent);
	        	finish();	

		}
		
		/*-- Method used to save current state --*/
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
		  super.onSaveInstanceState(savedInstanceState);
		  savedInstanceState.putBoolean("isChecked", cb.isChecked());
		  
		  if(((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).isAcceptingText())
			  savedInstanceState.putBoolean("KeyboardVisible", true);

		}
		   
	
	}
