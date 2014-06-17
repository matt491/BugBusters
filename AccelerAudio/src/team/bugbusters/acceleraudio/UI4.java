package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

//Riceve l'ID del record nel DB, estrae tutti i dati necessari e riproduce un suono
public class UI4 extends Activity {

	private int id;
	private String pkg_r;
	private Intent playIntentService;
	private ImageButton pause_resume,next,previous;
	private ImageView iv;
	private TextView name;
	private TextView duration;
	private Intent broadcastIntent;
	private DbAdapter db;
	private Cursor c;
	private String nome;
	private String thumbnail;
	private long durata;
	private SharedPreferences prefs;
	private static boolean primavolta=true;
	public static final String COMMAND_RESPONSE = "team.bugbusters.acceleraudio.intent.action.COMMAND_RESPONSE";
	private TextView time;
	private SeekBar sbtime;
	private TimerCounter timer;
	private long endtime,starttime,starttime1;
	private final long INTERVALLO=10;
	private boolean on_play=true;
	private MyUI4Receiver receiver;
	private int frame=0;
	public static final int PREVIOUS = 0;
	public static final int NEXT = 1;
	public static final int BY_INSERTION = -1;
	public static final int BY_NAME = 0;
	public static final int BY_DATE = 1;
	public static final int BY_DURATION = 2;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        pause_resume=(ImageButton)findViewById(R.id.imageButton1);
        next=(ImageButton)findViewById(R.id.imageButton3);
        previous=(ImageButton)findViewById(R.id.imageButton2);
        iv = (ImageView) findViewById(R.id.imageView1);
        name = (TextView) findViewById(R.id.title_widget_big);
        duration = (TextView) findViewById(R.id.textView3);
        time = (TextView) findViewById(R.id.textView4);
        sbtime=(SeekBar)findViewById(R.id.seekBar1);
        sbtime.setEnabled(false);
        starttime=System.currentTimeMillis();
        starttime1=starttime;
        db = new DbAdapter(this);
        
        receiver = new MyUI4Receiver();
        registerReceiver(receiver,new IntentFilter(MyUI4Receiver.NOTIFY_FRAME));
        
        pkg_r=getPackageName();   
        id=getIntent().getIntExtra(pkg_r+".myServiceID", -1);
        playIntentService=new Intent(UI4.this, PlayRecord.class);
        
        /*-- Layout settings --*/
        impostaUI4(id);
        
        /*-- Set Lock for Play --*/
        widget_big.play_widget=false;
        
        /*-- Broadcast used to send playback commands to Play Record service --*/
        broadcastIntent = new Intent();
        broadcastIntent.setAction(COMMAND_RESPONSE);
        
        
        /*-- Only on activity start --*/
        if(primavolta){
        	primavolta=false;
        	playIntentService.putExtra("fromUI4", true);
	        playIntentService.putExtra("ID", id);
	    	endtime=durata;
	    	sbtime.setMax((int) endtime);
	    	startService(playIntentService);
        }
        
        /*-- Previous button pressed --*/
        previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	/*-- Delay between all 3 button pressing which permit Play Record service to conclude pending operations --*/
            	if(System.currentTimeMillis()-starttime>500) {
            		starttime=System.currentTimeMillis();
            		timer.cancel();
	            	broadcastIntent.putExtra("Stop", true);
	            	broadcastIntent.putExtra("Pausa", false);
	            	broadcastIntent.putExtra("Riprendi", false);  
	            	sendBroadcast(broadcastIntent);
	            	stopService(playIntentService);
	            	id = searchId(db, id, PREVIOUS, currentSorting());
	            	impostaUI4(id);
	            	playIntentService.putExtra("fromUI4", true);
	            	playIntentService.putExtra("ID", id);
	            	endtime=durata;
	            	sbtime.setMax((int) endtime);
	            	startService(playIntentService);
            	}
            	
            }});
        
        /*-- Next button pressed --*/
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	/*-- Delay between all 3 button pressing which permit Play Record service to conclude pending operations --*/
            	if(System.currentTimeMillis()-starttime>500) {
            		starttime=System.currentTimeMillis();
            		timer.cancel();
	            	broadcastIntent.putExtra("Stop", true);
	            	broadcastIntent.putExtra("Pausa", false);
	            	broadcastIntent.putExtra("Riprendi", false);  	
	            	sendBroadcast(broadcastIntent);
	            	stopService(playIntentService);
	            	id = searchId(db, id, NEXT, currentSorting()); 
	            	impostaUI4(id);
	            	playIntentService.putExtra("fromUI4", true);
	            	playIntentService.putExtra("ID", id);
	            	endtime=durata;
	            	sbtime.setMax((int) endtime);
	            	startService(playIntentService);
            	}            	
            }});
        
        
        /*-- Pause/Resume button pressed --*/
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	/*-- Delay between all 3 button pressing which permit Play Record service to conclude pending operations --*/
            	if(System.currentTimeMillis()-starttime>100) {
            		starttime=System.currentTimeMillis();
	            	if(on_play){
	            		on_play=false;
	            		timer.cancel();
		            	broadcastIntent.putExtra("Pausa", true);
		            	broadcastIntent.putExtra("Riprendi", false);
		            	broadcastIntent.putExtra("Stop", false);
		            	sendBroadcast(broadcastIntent);
		            	pause_resume.setImageResource(android.R.drawable.ic_media_play);
	            	}
	            	else{
	            		on_play=true;
	            		broadcastIntent.putExtra("Riprendi", true);
	                	broadcastIntent.putExtra("Pausa", false);
	                	broadcastIntent.putExtra("Stop", false);
	                	sendBroadcast(broadcastIntent);	
	                	timer=new TimerCounter(endtime-(frame/(PlayRecord.AT_SAMPLE_RATE/1000)), INTERVALLO, frame/(PlayRecord.AT_SAMPLE_RATE/1000));
	                	timer.start();
	                	sendBroadcast(broadcastIntent);	
	                	pause_resume.setImageResource(android.R.drawable.ic_media_pause);
	            	}
            	}
            }});
        

        
	} /*-- onCreate End --*/
	
	
	/*-- Method that read and return current sorting from Shared Preferences --*/
	public int currentSorting() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int currentSorting = BY_INSERTION; //Default
		
		if(prefs.getBoolean("sortedByName", false)) {
			currentSorting = BY_NAME;
		}
		else if(prefs.getBoolean("sortedByDate", false)) {
			currentSorting = BY_DATE;
		}
		else if(prefs.getBoolean("sortedByDuration", false)) {
			currentSorting = BY_DURATION;
		}
		
		return currentSorting;
	}
	
	
	/*-- Static method used to search a previously or next coming ID respect on saved current sorting --*/
	public static int searchId(DbAdapter this_db, int playingId, int nextOrPrevious, int currentSorting) {
	int previousOrNextId;
	Cursor cursor;
	this_db.open();
	
	switch(currentSorting) {
	 
	 case BY_NAME:
		 cursor = this_db.fetchAllRecordSortedByName();
		 break;
		 
	 case BY_DATE:
		 cursor = this_db.fetchAllRecordSortedByDate();
		 break;
		 
	 case BY_DURATION:
		 cursor = this_db.fetchAllRecordSortedByDuration();
		 break;
		 
	 default:
		 cursor = this_db.fetchAllRecord();
		 break;
	 }
	

 	 switch(nextOrPrevious) {
 	 case PREVIOUS:
 		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if(cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_RECORDID)) == playingId) {
				if(!cursor.isFirst()) {
					cursor.moveToPrevious();
					previousOrNextId = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_RECORDID));
					cursor.close();
					this_db.close();
					return previousOrNextId;
				}
				else {
					cursor.moveToLast();
					previousOrNextId = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_RECORDID));
					cursor.close();
					this_db.close();
					return previousOrNextId;
				}
			 }
			}
 		 
 	 case NEXT:
 		 for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
 			 if(cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_RECORDID)) == playingId) {
 				 
 				 Log.i("CAZZO","ENTRATO");
 				 if(!cursor.isLast()) {
 					 cursor.moveToNext();
 					 previousOrNextId = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_RECORDID));
 					 cursor.close();
 					 this_db.close();
 					 return previousOrNextId;
 				 }
 				 else {
 					 cursor.moveToFirst();
 					 previousOrNextId = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_RECORDID));
 					 cursor.close();
 					 this_db.close();
 					 return previousOrNextId;
 				 }
 			 }
 		 }
 		 default:
 			 previousOrNextId = playingId;
 			 return previousOrNextId;	 
 	 }

    }
	
	
	/*-- Method used to set UI4's layout --*/
	public void impostaUI4(long this_id){
        db.open();
        c = db.fetchRecordById(this_id);
        c.moveToNext();
        
        thumbnail = c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
        nome = c.getString(c.getColumnIndex(DbAdapter.KEY_NAME));
        durata = Long.parseLong(c.getString(c.getColumnIndex(DbAdapter.KEY_DURATION)));
        
        c.close();
        db.close();
        
        int alpha = Integer.parseInt(thumbnail.substring(0, 3));
        int red = Integer.parseInt(thumbnail.substring(3, 6));
        int green = Integer.parseInt(thumbnail.substring(6, 9));
        int blue = Integer.parseInt(thumbnail.substring(9, 12));
        
        iv.setBackgroundColor(Color.argb(alpha, red, green, blue));
        
        switch(Integer.parseInt(thumbnail.substring(11))) {
			case 0:
				iv.setImageResource(R.drawable.ic_music_0);
				break;
			case 1:
				iv.setImageResource(R.drawable.ic_music_1);
				break;
			case 2:
				iv.setImageResource(R.drawable.ic_music_2);
				break;
			case 3:
				iv.setImageResource(R.drawable.ic_music_3);
				break;
			case 4:
				iv.setImageResource(R.drawable.ic_music_4);
				break;
			case 5: 
				iv.setImageResource(R.drawable.ic_music_5);
				break;
			case 6:
				iv.setImageResource(R.drawable.ic_music_6);
				break;
			case 7:
				iv.setImageResource(R.drawable.ic_music_7);
				break;
			case 8:
				iv.setImageResource(R.drawable.ic_music_8);
				break;
			case 9:
				iv.setImageResource(R.drawable.ic_music_9);
				break;
		}
        
        name.setText(nome);
        duration.setText(String.format("%.2f secondi", (float) durata/1000));
        on_play=true;
        pause_resume.setImageResource(android.R.drawable.ic_media_pause);
	}
	
	public void onDestroy(){
		
		/*-- Release Lock for Play --*/
		widget_big.play_widget=true;
		
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/*-- Back button pressed --*/
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		broadcastIntent.putExtra("Stop", true);
    	broadcastIntent.putExtra("Pausa", false);
    	broadcastIntent.putExtra("Play", false);
    	broadcastIntent.putExtra("Riprendi", false);
    	sendBroadcast(broadcastIntent);
    	primavolta=true;
        finish();	
	return;
	}
	
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
	
	
	/*-- Option menu --*/
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

	
	/*-- Inner class used as timer which updates duration progress bar --*/
	public class TimerCounter extends CountDownTimer{
		 private long end;
		 private long previous;
		 private long curr=0;
		  
	     public TimerCounter(long millisInFuture, long countDownInterval, long prev) {
	          super(millisInFuture, countDownInterval);
	          previous=prev;
	          end=millisInFuture;	
	      }

	      @Override
	     public void onFinish() {
	    	  time.setText(String.format("%.2f s", (float)(previous+end)/1000));
	    	  timer=new TimerCounter(previous+end, INTERVALLO, 0);
	    	  timer.start();   	  
	      }
	       

	      @Override
	      public void onTick(long millisUntilFinished) {
	    	  curr=millisUntilFinished;
	          time.setText(String.format("%.2f s", (float)(previous+end-curr)/1000));
	          sbtime.setProgress((int)(previous+end-curr));
	      }
	  }
	
	
	 /*-- Customized BroadcastReceiver used to receive notification from Play Record class --*/
	public class MyUI4Receiver extends BroadcastReceiver{

		   public static final String NOTIFY_FRAME = "team.bugbusters.acceleraudio.intent.action.NOTIFY_FRAME";
	        @Override
	        	public void onReceive(Context context, Intent intent) {
	        	
	        		/*-- Current frame (sample) (received when playback is on pause) --*/
	        		frame=intent.getIntExtra("CurrFrame", 0);
	        		
	        		/*-- Play Record service notifies that playback being started --*/
	        		if(intent.getBooleanExtra("Inizia", false)){
	        			timer=new TimerCounter(endtime,INTERVALLO,0);
	        	    	timer.start();
	        		}
	        			
	        	}
	        }


		
}