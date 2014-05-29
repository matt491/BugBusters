package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
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

	private long id;
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
	public static final String THREAD_RESPONSE = "team.bugbusters.acceleraudio.intent.action.THREAD_RESPONSE";
	private TextView time;
	private SeekBar sbtime;
	private TimerCounter timer;
	private long prec,endtime,starttime;
	private final long intervallo=1L;
	private boolean on_play=true;
	private static final int PREVIOUS = 0;
	private static final int NEXT = 1;
	private static final int BY_INSERTION = -1;
	private static final int BY_NAME = 0;
	private static final int BY_DATE = 1;
	private static final int BY_DURATION = 2;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        pause_resume=(ImageButton)findViewById(R.id.imageButton1);
        next=(ImageButton)findViewById(R.id.imageButton3);
        previous=(ImageButton)findViewById(R.id.imageButton2);
        iv = (ImageView) findViewById(R.id.imageView1);
        name = (TextView) findViewById(R.id.textView1);
        duration = (TextView) findViewById(R.id.textView3);
        time = (TextView) findViewById(R.id.textView4);
        sbtime=(SeekBar)findViewById(R.id.seekBar1);
        sbtime.setEnabled(false);
        starttime=System.currentTimeMillis();
        db = new DbAdapter(this);
        
        pkg_r=getPackageName();   
        id=getIntent().getLongExtra(pkg_r+".myServiceID", -1);
        
        impostaUI4(id);
        
        broadcastIntent = new Intent();
        broadcastIntent.setAction(THREAD_RESPONSE);
        playIntentService=new Intent(UI4.this, PlayRecord.class);
        
        if(primavolta){
        	primavolta=false;
	        playIntentService.putExtra("ID", id);
	    	endtime=durata;
	    	sbtime.setMax((int) endtime);
	    	creaTimer(endtime,intervallo,0);
	    	startService(playIntentService);
        }
        
 
        previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(System.currentTimeMillis()-starttime>400) {
            		starttime=System.currentTimeMillis();
	            	broadcastIntent.putExtra("Stop", true);
	            	broadcastIntent.putExtra("Pausa", false);
	            	broadcastIntent.putExtra("Riprendi", false);  
	            	sendBroadcast(broadcastIntent);
	            	stopService(playIntentService);
	            	timer.cancel();
	            	id = searchId(id, PREVIOUS, currentSorting());
	            	broadcastIntent.putExtra("Stop", false);  
	            	impostaUI4(id);
	            	playIntentService.putExtra("ID", id);
	            	endtime=durata;
	            	sbtime.setMax((int) endtime);
	            	creaTimer(endtime,intervallo,0);
	            	startService(playIntentService);
            	}
            	
            }});
        
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(System.currentTimeMillis()-starttime>400) {
            		starttime=System.currentTimeMillis();
	            	broadcastIntent.putExtra("Stop", true);
	            	broadcastIntent.putExtra("Pausa", false);
	            	broadcastIntent.putExtra("Riprendi", false);  	
	            	sendBroadcast(broadcastIntent);
	            	stopService(playIntentService);
	            	timer.cancel();
	            	id = searchId(id, NEXT, currentSorting());
	            	broadcastIntent.putExtra("Stop", false);  
	            	//id++;
	            	impostaUI4(id);
	            	playIntentService.putExtra("ID", id);
	            	endtime=durata;
	            	sbtime.setMax((int) endtime);
	            	creaTimer(endtime,intervallo,0);
	            	startService(playIntentService);
            	}            	
            }});
        
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(on_play){
            		on_play=false;
	            	broadcastIntent.putExtra("Pausa", true);
	            	broadcastIntent.putExtra("Riprendi", false);
	            	sendBroadcast(broadcastIntent);
	            	prec=timer.myCancel();
	            	pause_resume.setImageResource(android.R.drawable.ic_media_play);
            	}
            	else{
            		on_play=true;
            		broadcastIntent.putExtra("Riprendi", true);
                	broadcastIntent.putExtra("Pausa", false);
                	creaTimer(endtime-prec, intervallo, prec);
                	sendBroadcast(broadcastIntent);	
                	pause_resume.setImageResource(android.R.drawable.ic_media_pause);
            	}
            }});
        

        
	} //FINE onCreate()
	
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
	
	public long searchId(long playingId, int nextOrPrevious, int currentSorting) {
	long previousOrNextId;
	
	db.open();
	
	switch(currentSorting) {
	 
	 case BY_NAME:
		 c = db.fetchAllRecordSortedByName();
		 break;
		 
	 case BY_DATE:
		 c = db.fetchAllRecordSortedByDate();
		 break;
		 
	 case BY_DURATION:
		 c = db.fetchAllRecordSortedByDuration();
		 break;
		 
	 default:
		 c = db.fetchAllRecord();
		 break;
	 }
	
	
	
 	 switch(nextOrPrevious) {
 	 case PREVIOUS:
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			if(c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID)) == playingId) {
				if(!c.isFirst()) {
					c.moveToPrevious();
					previousOrNextId = c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID));
					c.close();
					db.close();
					return previousOrNextId;
				}
				else {
					c.moveToLast();
					previousOrNextId = c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID));
					c.close();
					db.close();
					return previousOrNextId;
				}
			 }
			}
 		 
 	 case NEXT:
 		 for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 			 if(c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID)) == playingId) {
 				 if(!c.isLast()) {
 					 c.moveToNext();
 					 previousOrNextId = c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID));
 					 c.close();
 					 db.close();
 					 return previousOrNextId;
 				 }
 				 else {
 					 c.moveToFirst();
 					 previousOrNextId = c.getLong(c.getColumnIndex(DbAdapter.KEY_RECORDID));
 					 c.close();
 					 db.close();
 					 return previousOrNextId;
 				 }
 			 }
 		 }
 		 default:
 			 previousOrNextId = playingId;
 			 return previousOrNextId;	 
 	 }
    }
	
	public void creaTimer(long fine, long this_intervallo, long prev ){
		timer=new TimerCounter(fine, this_intervallo);
    	timer.end=fine;
    	timer.previous=prev;
    	timer.start();
	}
	
	
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
        duration.setText(""+((float)(durata/10)/100));
        on_play=true;
        pause_resume.setImageResource(android.R.drawable.ic_media_pause);
        }
	
	//Quando viene premuto il tasto Back
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

	
	
	
	public class TimerCounter extends CountDownTimer{
		 private long end;
		 private long last;
		 private long previous=0;
		 private long curr;
		 
	      public TimerCounter(long millisInFuture, long countDownInterval) {
	          super(millisInFuture, countDownInterval);
	      }

	      @Override
	      public void onFinish() {
	    	  time.setText((float)((end+previous)/100)/10+"");
	    	  creaTimer(end+previous, intervallo, 0);
	      }
	      
	      public long myCancel(){
	    	  last=previous+end-curr;
	    	  super.cancel(); 
	    	  return last;
	      }
	      

	      @Override
	      public void onTick(long millisUntilFinished) {
	    	  curr=millisUntilFinished;
	          time.setText((float)((previous+end-curr)/100)/10 +"");
	          sbtime.setProgress((int)(previous+end-curr));
	          last=previous+end-curr;
	      }
	  }

		
}