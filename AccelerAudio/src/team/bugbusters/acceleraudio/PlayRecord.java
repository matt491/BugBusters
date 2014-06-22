package team.bugbusters.acceleraudio;

import team.bugbusters.acceleraudio.UI4.MyUI4Receiver;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.SystemClock;


public class PlayRecord extends IntentService {
	private DbAdapter dbHelper;
	private Cursor cr;
	private long id_to_process;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int sovrac;
	private String[] s,p,q;
	private boolean  pausa,riprendi,stop,from_UI4;
	private int i,j, g = 0;
	private AudioTrack at;
	private int sc;
	private Intent broadUI4;
	private short[] finale;
	private CommandReceiver receiver=new CommandReceiver();
	public static boolean running;
	public static final int MINSIZE = 7000;
	public static final int AT_SAMPLE_RATE = 24000;
 	
	
	/*-- Constructor --*/
	public PlayRecord() {
		super("PlayRecord");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
			
		/*-- Service status knowledgement --*/
	    running = true;
	        
		/*-- Registering Broadcast receiver used to receive playback commands --*/
        registerReceiver(receiver,new IntentFilter(UI4.COMMAND_RESPONSE));
        
        from_UI4 = intent.getBooleanExtra("fromUI4", false);
        if(from_UI4) {
        	broadUI4 = new Intent();
        	broadUI4.setAction(MyUI4Receiver.NOTIFY_FRAME);
        }
        
        dbHelper = new DbAdapter(this);
        id_to_process = intent.getLongExtra("ID", -1);
        
        /*-- Opening DB --*/
        dbHelper.open();
        
        /*-- Query that return all informations about a certain ID --*/
        cr=dbHelper.fetchRecordById(id_to_process);
        
    	/*-- Reading data from query result --*/
        cr.moveToNext();
        asseX = cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEX));
        asseY = cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEY));
        asseZ = cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEZ));
        checkX = Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        checkY = Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        checkZ = Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        sovrac = Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
        
    	/*-- Closing DB --*/
        dbHelper.close();
        
              
    	/*-- Tokenization and conversion of samples to short arrays --*/
        s = myTokenizer(asseX);
        p = myTokenizer(asseY);
        q = myTokenizer(asseZ);
        
        short[] x=new short[s.length];
        short[] y=new short[p.length];
        short[] z=new short[q.length];
        

        for(i=0; i<s.length; i++)
    		x[i] = Short.parseShort(s[i]);
        for(i=0; i<p.length; i++)
    		y[i] = Short.parseShort(p[i]);      
        for(i=0; i<q.length; i++)
    		z[i] = Short.parseShort(q[i]);
        

        /*-- Calculate upsampling coefficient --*/
        sc = calcoloSovra(sovrac, x.length+y.length+z.length);
        
        
        /*-- Creation and initialization of arrays which will be written in the AudioTrack internal buffer --*/
        	short[] s1 = new short[0];
	        if(checkX){
	        	s1 = new short[MINSIZE+sc*x.length];
	           	 j = 0;
	           	 for(i=0; i<s1.length; i++){
	           		 s1[i] = x[j];
	           		 j++;
	           		 if(j == x.length) j = 0;		           	
	            }
	        }
	         
	        short[] s2 = new short[0];
			if(checkZ){
				s2 = new short[MINSIZE+sc*z.length];
	           	 j = 0;
	           	 for(i=0; i<s2.length; i++){
	           		s2[i] = z[j];
	           		 j++;
	           		 if(j == z.length) j = 0;
	           	 }
	            }
	        
	        short[] s3 = new short[0];	
	        if(checkY)	{ 
	        	s3 = new short[MINSIZE+sc*y.length];
	        	 j = 0;
	        	 for(i=0; i<s3.length; i++){
	        		 s3[i] = y[j];
	        		 j++;
	        		 if(j == y.length) j = 0;
 
	         }
	        }
	        
	        short[] s4 = new short[0];	 
	        if(checkZ){
	        	s4 = new short[MINSIZE+sc*z.length];
	           	 j = 0;
	           	 for(i=0; i<s4.length; i++){
	           		s4[i] = z[j];
	           		 j++;
	           		 if(j == z.length) j = 0;
	            }
	        }
	        
	        short[] s5 = new short[0];    
	        if(checkX){
	        	s5 = new short[MINSIZE+sc*x.length];
	        	j = 0;
	           	 for(i=0; i<s5.length; i++){
	           		s5[i] = x[j];
	           		 j++;
	           		 if(j == x.length) j = 0;
	           	 }
	            }
	            
            short[] s6 = new short[0];    
           	if(checkY)	{
           		s6 = new short[MINSIZE+sc*y.length];  
		        j = 0;
		        for(i=0; i<s6.length; i++){
		        	s6[i] = y[j];
		        	j++;
		        	if(j == y.length) j = 0;
		        }
	         }
			   
           	
       	/*-- Merging previous arrays into one --*/
        finale = new short[s1.length+s2.length+s3.length+s4.length+s5.length+s6.length];
        System.arraycopy(s1, 0, finale, 0, s1.length);	
        System.arraycopy(s2, 0, finale, s1.length, s2.length);	
        System.arraycopy(s3, 0, finale, s1.length+s2.length, s3.length);
        System.arraycopy(s4, 0, finale, s1.length+s2.length+s3.length, s4.length);
        System.arraycopy(s5, 0, finale, s1.length+s2.length+s3.length+s4.length, s5.length);
        System.arraycopy(s6, 0, finale, s1.length+s2.length+s3.length+s4.length+s5.length, s6.length);
        
       
     	/*-- Creation and initialization of AudioTrack object in static mode --*/    
        at = new AudioTrack(AudioManager.STREAM_MUSIC, AT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 2*finale.length,
        					AudioTrack.MODE_STATIC); 
        
        /*-- Writing finale array into AudioTrack internal buffer --*/
        at.write(finale, 0, finale.length); 
           
        
        /*-- Setting music loop (infinte) --*/
        at.setLoopPoints(0, finale.length-1, -1);
        
            	
    	/*-- Playback --*/
    	at.play();
        
   
        /*-- If Play Record service was launched from UI 4 then send a broadcast to notify playback start --*/
        if(from_UI4) {
	        broadUI4.putExtra("CurrFrame",0);
	        broadUI4.putExtra("Inizia", true);
	        sendBroadcast(broadUI4);
        }
        

        /*-- Keep alive Play Record service, thus to continue to receive commands from UI 4 or widget --*/
        while(true)
        	SystemClock.sleep(36000000);

	
	
	}
	  
        
  public void onDestroy(){
      running = false;
      
      /*-- Unregister receiver --*/
	  this.unregisterReceiver(receiver);
	  super.onDestroy();  
  }
  
  
  /*-- Method used to calculate upsampling coefficient, based on number of samples recorded and
    on the upsamplig SeekBar position --*/
  public static int calcoloSovra(int s, int camp){
	  if(camp >= 1000) return (int) (30*s/100);
	  else if (camp < 1000) return (int) (40*s/100);
	  else if (camp < 500) return (int) (80*s/100);
	  else if (camp < 250) return (int) (120*s/100);
	  else return (200*s/100);
	  
  }
  
  
  /*-- Method used to tokenize a string into an array string --*/
  private String[] myTokenizer(String s){
  	String[] ret;
	 if(s.length() != 0)
    	ret = s.split(" "); 
    else {
    	ret = new String[1];
    	ret[0] = "0";
    }
	 return ret;
  }
  
  		
 
  /*-- Inner class of commands receiver --*/
	public class CommandReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			pausa=intent.getBooleanExtra("Pausa", false);
			riprendi=intent.getBooleanExtra("Riprendi", false);
			stop=intent.getBooleanExtra("Stop", false);
		
		   if(stop){
		       if(at.getState() == AudioTrack.STATE_INITIALIZED && at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
		    	   at.pause();
		    	   at.flush();
		    	   at.release();
		       }
		       else if(at.getState() == AudioTrack.STATE_INITIALIZED && at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
					at.flush();
					at.release();
					}      
			   stopSelf();
		   }
		   
		   if(pausa) {
		   if(at.getState() == AudioTrack.STATE_INITIALIZED && at.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) 
			    g = at.getPlaybackHeadPosition();	
		   		if(from_UI4) {
			   		broadUI4.putExtra("Inizia", false);
			   		broadUI4.putExtra("CurrFrame",g%finale.length);
			   		sendBroadcast(broadUI4);
		   		}
		   		at.pause();   

	       }
				
		    if(riprendi) {
		       if(at.getState() == AudioTrack.STATE_INITIALIZED && at.getPlayState() == AudioTrack.PLAYSTATE_PAUSED)   
		    	   at.setPlaybackHeadPosition(g%finale.length);
		       
		       	   int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		           if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT)
		        	   at.setLoopPoints(0, finale.length-1, -1);
		        	   
		       	   at.play();
		       		   
		       }		
		}
		
	}
	
}
      


        
