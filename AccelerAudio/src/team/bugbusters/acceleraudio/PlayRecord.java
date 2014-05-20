package team.bugbusters.acceleraudio;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
	private String sovrac;
	private String[] s,p,q;
 	private short[] x,y,z,w;
	private Thread t;
	private boolean isRunning = true;
	private int k,minsize;
	
	public PlayRecord() {
		super("PlayRecord");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
        
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
        sovrac=cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE));
        
        //Chiusura DB
        dbHelper.close();
        
        if(sovrac.equals("Scelta 0")) k=0;
        if(sovrac.equals("Scelta 1")) k=1;
        if(sovrac.equals("Scelta 2")) k=2;
        if(sovrac.equals("Scelta 3")) k=3;
        
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
      
        	


        // start a new thread to synthesise audio
     /*   t = new Thread() {
         public void run() {
         setPriority(Thread.MAX_PRIORITY);
      */
        
     /*    
         for(i=0;i<x.length;i++)
				x[i]=(short) (x[i]*Math.sin(((double)i)/100));
         for(i=0;i<y.length;i++)
				y[i]=(short) (y[i]*Math.sin(((double)i)/100));
         for(i=0;i<z.length;i++)
				z[i]=(short) (z[i]*Math.sin(((double)i)/100));
     */    
         
         minsize = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
         
         AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC,44100, AudioFormat.CHANNEL_OUT_MONO,
                                   AudioFormat.ENCODING_PCM_16BIT, minsize, AudioTrack.MODE_STREAM);

         short[] samples = new short[2*minsize];

         // start audio
        at.play();
        
        switch(k){
	        
	        case 0: {
	           
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
	        } //Fine case 0 "Scelta 0" durata uguale per tutti
	    
	        case 1: {
	        	short[] s = new short[minsize];
	        	if(checkX)
	        		for(int j=0;j<x.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (x[j]*Math.sin(((double)i)/10)+x[j]/10);		 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	if(checkY)
	        		for(int j=0;j<y.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (y[j]*Math.sin(((double)i)/10)+y[j]/10);			 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	if(checkZ)
	        		for(int j=0;j<z.length;j++) {
	        			for(i=0;i<s.length;i++)
	        				s[i]=(short) (z[j]*Math.sin(((double)i)/10)+y[j]/10);			 
		           	 at.write(s, 0, s.length);
		            }
	        	
	        	
	        }
	        
        } //Fine switch
        
        
        
         
	}
         
         
         
         
         
         
         
         
         
         
         
         
         
      /*  int minsize = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC,44100, AudioFormat.CHANNEL_OUT_MONO,
                                  AudioFormat.ENCODING_PCM_16BIT, minsize, AudioTrack.MODE_STREAM);

        short samples[] = new short[minsize];

        // start audio
       at.play();
       switch(k) {
       		case 0:{ //Sovracampionamento "Scelta 0"
       			if(checkX)
       			for(int i=0;i<60;i++)
       				at.write(x, 0, x.length);
       			
       			if(checkY)
       			for(int i=0;i<60;i++)
       				at.write(z, 50, z.length);
       			
       			if(checkZ)
       			for(int i=0;i<60;i++)
       				at.write(y, 50, y.length);
       
       		
       			break;
       		}
       		case 1: {
       			short[] w=new short[x.length+y.length+z.length];
       			for(int u=0;u<x.length+y.length+z.length-2;u++)	{
       				if(u<x.length)	w[u]=x[u];
       				
       				if(u<y.length) w[u+1]=y[u];
       				
       				if(u<z.length) w[u+2]=z[u];
       			}
       					
       					
       			for(int j=0; j<50; j++){
       				at.write(w, 0, w.length);
       			}
       			
       			
       			
       			break;
       		}
    	   
       }

    
       
       at.stop();
       at.release();
    */   
  /*       }
        	};
        	
   		t.start();        

          
             
              
           }
     */
       
        
  public void onDestroy(){
	  
	  Toast.makeText(getApplicationContext(), ""+minsize, Toast.LENGTH_SHORT).show();
	  
  }
	
	
	
 }
	
                 
            


        
