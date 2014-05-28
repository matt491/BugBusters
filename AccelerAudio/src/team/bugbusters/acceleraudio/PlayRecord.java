package team.bugbusters.acceleraudio;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

public class PlayRecord extends IntentService {

	private DbAdapter dbHelper;
	private Cursor cr;
	private long id_to_process;
	private double m;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int ncamp;
	private int sovrac;
	private String[] s,p,q;
 	private short[] x,y,z;
	private boolean  pausa,riprendi,stop;
	private int g,k,minsize;
	private AudioTrack at;
	private short[] finale;
	
	private BroadcastReceiver receiver=new BroadcastReceiver(){
		  
		public void onReceive(Context context, Intent intent) {
			pausa=intent.getBooleanExtra("Pausa", false);
   		    riprendi=intent.getBooleanExtra("Riprendi", false);
   		    stop=intent.getBooleanExtra("Stop", false);
   		    
   		    
   		       if(stop){
   		           if(at.getState()==AudioTrack.STATE_INITIALIZED && at.getPlayState()==AudioTrack.PLAYSTATE_PLAYING) {
       		    	   at.pause();
       		    	   at.flush();
       		    	   at.release();
   		           }
   		       else if(at.getState()==AudioTrack.STATE_INITIALIZED && at.getPlayState()==AudioTrack.PLAYSTATE_PAUSED) {
   		    		at.flush();
       		    	at.release();
   		        	}      
   		        stopSelf();
   		       }
   		       
   		       if(pausa) {
   		       if(at.getState()==AudioTrack.STATE_INITIALIZED && at.getPlayState()==AudioTrack.PLAYSTATE_PLAYING) 
   		    	   	at.pause();   
   		       		g=at.getPlaybackHeadPosition();
   		       }
				
   		    if(riprendi) {
		       if(at.getState()==AudioTrack.STATE_INITIALIZED && at.getPlayState()==AudioTrack.PLAYSTATE_PAUSED)   
		    	   at.setPlaybackHeadPosition(g);
		    	   at.play();    
		       }		
		}	
	};
	
	public PlayRecord() {
		super("PlayRecord");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
        registerReceiver(receiver,new IntentFilter(UI4.THREAD_RESPONSE));
        
        dbHelper = new DbAdapter(this);
        
        id_to_process=intent.getLongExtra("ID", -1);
        
        //Apertura DB
        dbHelper.open();
        
        //Query che individua l'unica riga con questo ID
        cr=dbHelper.fetchRecordById(id_to_process);
        
        //Si posiziona all'unica tupla esistente
        cr.moveToNext();
        
        //Lettura dei dati dal record(cursor) restituito
       // m=Float.parseFloat(cr.getString(cr.getColumnIndex(DbAdapter.KEY_DURATION)));
        asseX=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEX));
        asseY=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEY));
        asseZ=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEZ));
        checkX=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        checkY=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        checkZ=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        ncamp=cr.getInt(cr.getColumnIndex(DbAdapter.KEY_NUMCAMP));
        sovrac=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
        
        //Chiusura DB
        dbHelper.close();
        
        
        s=asseX.split(" "); 
        p=asseY.split(" "); 
        q=asseZ.split(" "); 
        
        x=new short[s.length];
        y=new short[p.length];
        z=new short[q.length];
        
        //Tokenizzazione delle stringhe in array di short
        int i;
        for(i=0;i<s.length;i++)
    		x[i]=Short.parseShort(s[i]);
        
        
        for(i=0;i<p.length;i++)
    		y[i]=Short.parseShort(p[i]);
        
        
        for(i=0;i<q.length;i++)
    		z[i]=Short.parseShort(q[i]);

        
        minsize=AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO , AudioFormat.ENCODING_PCM_16BIT);
        
	        	short[] s1 = new short[0];
		        if(checkX){
		        	s1 = new short[2*minsize];
		            if(x.length >= minsize)
		           	 at.write(x, 0, x.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<s1.length;i++){
		           		 s1[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;
		           	 }
		           	
		            }
		        }
		         
		        short[] s2 =new short[0];
				if(checkZ){
					s2 =new short[2*minsize];
		        	if(z.length >= minsize)
		           	 at.write(z, 0, z.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<s2.length;i++){
		           		s2[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		           	 }
		          
		            } 
		            }
		        
		        short[] s3 =new short[0];	
		        if(checkY)	{ 
		         s3 =new short[2*minsize];
		         if(y.length >= minsize)
		        	 at.write(y, 0, y.length);
		         else {
		        	 int j=0;
		        	 for(i=0;i<s3.length;i++){
		        		 s3[i]=y[j];
		        		 j++;
		        		 if(j==y.length) j=0;
		        	 }
		        		 
		         }
		        }
		        
		        short[] s4 =new short[0];	 
		        if(checkZ){
		        	s4 =new short[2*minsize];
		            if(z.length >= minsize)
		           	 at.write(z, 0, z.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<s4.length;i++){
		           		s4[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		           	 }

		            }
		        }
		        
		        short[] s5 =new short[0];    
		        if(checkX){
		        	s5 =new short[2*minsize];
		            if(x.length >= minsize)
		           	 at.write(x, 0, x.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<s5.length;i++){
		           		s5[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;
		           	 }

		            }
		            }
		            
		            short[] s6 =new short[0];    
		           	if(checkY)	{
		           		s6 =new short[2*minsize];  
				         if(y.length >= minsize)
				        	 at.write(y, 0, y.length);
				         else {
				        	 int j=0;
				        	 for(i=0;i<s6.length;i++){
				        		 s6[i]=y[j];
				        		 j++;
				        		 if(j==y.length) j=0;
				        	 }

		            }
		           	}
				         
		         finale=new short[s1.length+s2.length+s3.length+s4.length+s5.length+s6.length];
		         System.arraycopy(s1, 0, finale, 0, s1.length);	
		         System.arraycopy(s2, 0, finale, s1.length, s2.length);	
		         System.arraycopy(s3, 0, finale, s1.length+s2.length, s3.length);
		         System.arraycopy(s4, 0, finale, s1.length+s2.length+s3.length, s4.length);
		         System.arraycopy(s5, 0, finale, s1.length+s2.length+s3.length+s4.length, s5.length);
		         System.arraycopy(s6, 0, finale, s1.length+s2.length+s3.length+s4.length+s5.length, s6.length);
		         

	    
	  /*      case 1: {
	        	samples = new short[2*minsize+20*x.length];
		        if(checkX)
		            if(x.length >= minsize)
		           	 at.write(x, 0, x.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<samples.length;i++){
		           		 samples[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;
		           	 }
		           		 
		           	 at.write(samples, 0, samples.length);
		            }
		        
		        samples = new short[2*minsize+20*z.length];
		        if(checkZ)
		        	if(z.length >= minsize)
		           	 at.write(z, 0, z.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<samples.length;i++){
		           		 samples[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		           	 }
		           		 
		           	 at.write(samples, 0, samples.length);
		            }
		        
		        samples = new short[2*minsize+20*y.length]; 
		        if(checkY)	 
		         if(y.length >= minsize)
		        	 at.write(y, 0, y.length);
		         else {
		        	 int j=0;
		        	 for(i=0;i<samples.length;i++){
		        		 samples[i]=y[j];
		        		 j++;
		        		 if(j==y.length) j=0;
		        	 }
		        		 
		        	 at.write(samples, 0, samples.length);
		         }
		        
		        samples = new short[2*minsize+20*z.length];
		        if(checkZ)
		            if(z.length >= minsize)
		           	 at.write(z, 0, z.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<samples.length;i++){
		           		 samples[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		           	 }
		           		 
		           	 at.write(samples, 0, samples.length);
		            }
		        
		        samples = new short[2*minsize+20*x.length];
		        if(checkX)
		            if(x.length >= minsize)
		           	 at.write(x, 0, x.length);
		            else {
		           	 int j=0;
		           	 for(i=0;i<samples.length;i++){
		           		 samples[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;
		           	 }
		           		 
		           	 at.write(samples, 0, samples.length);
		           	 
		            }
		        samples = new short[2*minsize+20*y.length]; 
		        if(checkY)	 
				    if(y.length >= minsize)
			        	 at.write(y, 0, y.length);
			         else {
			        	 int j=0;
			        	 for(i=0;i<samples.length;i++){
			        		 samples[i]=y[j];
			        		 j++;
			        		 if(j==y.length) j=0;
			        	 }
			        		 
			        	 at.write(samples, 0, samples.length);    
		            }
	        	
	        	break;
	        	
	        } //Fine case 1 "Scelta 1" durata=6*(2*minsize+20*numcamp)/44100  <-- VARIABILE -->
	        
	        case 2: {
	        	short[] s = new short[minsize];
	        	if(checkX)
	        		for(int j=0;j<x.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (x[j]*Math.sin(((double)i)/5)+x[j]/10);		 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	if(checkY)
	        		for(int j=0;j<y.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (y[j]*Math.sin(((double)i)/5)+y[j]/10);			 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	if(checkZ)
	        		for(int j=0;j<z.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (z[j]*Math.sin(((double)i)/5)+z[j]/10);			 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	break;
	        } //Fine case 2 "Scelta 2" durata=ncamp*minsize/44100  <-- LUNGO/PSICADELICO -->
	        
	       	        
        } //Fine switch*/
        
       

        at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 2*finale.length,
        					AudioTrack.MODE_STATIC);
        at.write(finale, 0, finale.length); 
        at.setLoopPoints(0, finale.length-1, -1);
        at.play();
        
        Looper.loop();

	
      
	
	}
	  
        
  public void onDestroy(){
	  this.unregisterReceiver(receiver);
	  Toast.makeText(getApplicationContext(), "Servizio Terminato", Toast.LENGTH_SHORT).show();
	  super.onDestroy();
	  
  }
  
  	
	
 }
	
                 
            


        
