package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class UI4 extends Activity {

	private long id;
	private String pkg_r;
	private Intent playIntentService;
	private ImageButton pause_resume; 
	private ImageView iv;
	private TextView name;
	private TextView duration;
	private Intent broadcastIntent;
	private DbAdapter db;
	private Cursor c;
	private String nome;
	private String thumbnail;
	private long durata;
	private static boolean primavolta=true;
	public static final String COMMAND_RESPONSE = "team.bugbusters.acceleraudio.intent.action.COMMAND_RESPONSE";
	private TextView time;
	private SeekBar sbtime;
	private TimerCounter timer;
	private long endtime;
	private static long starttime;
	private final long INTERVALLO=10;
	private boolean on_play, stopped_first;
	private MyUI4Receiver receiver;
	private int frame=0;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        
        /*-- Set layout view resources --*/
        pause_resume = (ImageButton) findViewById(R.id.imageButton1);
        iv = (ImageView) findViewById(R.id.imageView1);
        name = (TextView) findViewById(R.id.title_big);
        duration = (TextView) findViewById(R.id.textView3);
        time = (TextView) findViewById(R.id.textView4);
        sbtime = (SeekBar) findViewById(R.id.seekBar1);
        sbtime.setEnabled(false);
        starttime = System.currentTimeMillis();
        on_play = false;
        
        db = new DbAdapter(this);
        
        /*-- Create and register receiver which is used to handle Play Record service notification --*/
        receiver = new MyUI4Receiver();
        registerReceiver(receiver,new IntentFilter(MyUI4Receiver.NOTIFY_FRAME));
        
        pkg_r=getPackageName();  
        
        /*-- Receive record ID from UI1/UI2 --*/
        id = getIntent().getLongExtra(pkg_r+".myServiceID", -1);
        playIntentService = new Intent(UI4.this, PlayRecord.class);
        
        /*-- Layout settings --*/
        impostaUI4(id);
        
        /*-- Set Lock for Play --*/
        widget_big.play_widget = false;
        
        /*-- Broadcast used to send playback commands to Play Record service --*/
        broadcastIntent = new Intent();
        broadcastIntent.setAction(COMMAND_RESPONSE);
        
        
        /*-- Only on activity start --*/
        if(primavolta){
        	primavolta = false;
        	playIntentService.putExtra("fromUI4", true);
	        playIntentService.putExtra("ID", id);
	    	endtime = durata;
	    	sbtime.setMax((int) endtime);
	    	
	        /*-- Check if speakers is already in use --*/ 
	        if(!((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isMusicActive()) 
	        	startService(playIntentService);
 
	        else {
	        	stopped_first = true;
	        	Toast.makeText(UI4.this, R.string.speakerUnavailable, Toast.LENGTH_SHORT).show();
	        }
		    	
        }
        

        
        /*-- Pause/Resume button pressed --*/
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	/*-- Delay to pressing button which permits Play Record service to conclude pending operations --*/
            	if(System.currentTimeMillis()-starttime > 150) {
            		starttime = System.currentTimeMillis();
	            	if(on_play){
	            		on_play = false;
	            		timer.cancel();
	            		
	            		/*-- Send Pause signal to Play record service --*/
		            	broadcastIntent.putExtra("Pausa", true);
		            	broadcastIntent.putExtra("Riprendi", false);
		            	broadcastIntent.putExtra("Stop", false);
		            	sendBroadcast(broadcastIntent);
		            	pause_resume.setImageResource(R.drawable.ic_action_play);
	            	}
	            	else{
	            		/*-- Check if speakers is already in use --*/ 
	        	        if(!((AudioManager)getSystemService(Context.AUDIO_SERVICE)).isMusicActive()){
	        	        	
	        	        	if(stopped_first){
	        	        		stopped_first = false;
	        	        		startService(playIntentService);
	        	        	}
	        	        	
	        	        	else{
			            		on_play = true;
			            		
			            		/*-- Send Resume signal to Play record service --*/
			            		broadcastIntent.putExtra("Riprendi", true);
			                	broadcastIntent.putExtra("Pausa", false);
			                	broadcastIntent.putExtra("Stop", false);
			                	sendBroadcast(broadcastIntent);	
			                	timer = new TimerCounter(endtime-(frame/(PlayRecord.AT_SAMPLE_RATE/1000)), INTERVALLO, frame/(PlayRecord.AT_SAMPLE_RATE/1000));
			                	timer.start();
			                	sendBroadcast(broadcastIntent);	
			                	pause_resume.setImageResource(R.drawable.ic_action_pause);
	        	        	}
	        	        	
	        	        }
	        	        else Toast.makeText(UI4.this, R.string.speakerUnavailable, Toast.LENGTH_SHORT).show();
	            		
	            	}
            	}
            }});
        

        
	} /*-- onCreate End --*/
	

	
	/*-- Method used to set UI4's layout --*/
	public void impostaUI4(long this_id){
        try {
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
		} catch (SQLException e) {
			Toast.makeText(UI4.this, R.string.dbError, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	public void onDestroy(){
		
		/*-- Release Lock for Play --*/
		widget_big.play_widget = true;
		
		/*-- Unregister receiver --*/
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/*-- Back button pressed --*/
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		/*-- Send Stop signal to Play record service --*/
		broadcastIntent.putExtra("Stop", true);
    	broadcastIntent.putExtra("Pausa", false);
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
			prefIntentUI5.putExtra("prefFromWidget", false);
            startActivity(prefIntentUI5);
            return(true);
		}
		
		return (super.onOptionsItemSelected(item));
	}

	
	/*-- Inner class used as timer which updates duration progress bar --*/
	public class TimerCounter extends CountDownTimer{
		 private long end;
		 private long previous;
		 private long curr = 0;
		  
	     public TimerCounter(long millisInFuture, long countDownInterval, long prev) {
	          super(millisInFuture, countDownInterval);
	          previous=prev;
	          end=millisInFuture;	
	      }

	      @Override
	     public void onFinish() {
	    	  time.setText(String.format("%.2f s", (float)(previous+end)/1000));
	    	  timer = new TimerCounter(previous+end, INTERVALLO, 0);
	    	  timer.start();   	  
	      }
	       

	      @Override
	      public void onTick(long millisUntilFinished) {
	    	  curr = millisUntilFinished;
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
	        			on_play=true;
	        			pause_resume.setImageResource(R.drawable.ic_action_pause);
	        			timer=new TimerCounter(endtime,INTERVALLO,0);
	        	    	timer.start();
	        		}
	        		
        				
	        	}
	        }


		
}