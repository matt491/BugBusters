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
import android.os.SystemClock;
import android.widget.Toast;

public class PlayRecord extends IntentService {

	private DbAdapter dbHelper;
	private Cursor cr;
	private long id_to_process;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int sovrac,campx,campy,campz;
	private String[] s,p,q;
 	private short[] x,y,z;
	private boolean  pausa,riprendi,stop;
	private int g,i,j;
	private AudioTrack at;
	private short[] finale;
	private final int minsize=7000;
	private int sc;
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
	    	   g=at.getPlaybackHeadPosition();	
	    	   at.pause();   
	       		
	       }
				
		    if(riprendi) {
		       if(at.getState()==AudioTrack.STATE_INITIALIZED && at.getPlayState()==AudioTrack.PLAYSTATE_PAUSED)   
		    	   at.setPlaybackHeadPosition(g);
		    	   at.play();    
		       }		
		}	
	};
	
	//Costruttore
	public PlayRecord() {
		super("PlayRecord");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		//Registrazione ricevitore
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
        asseX=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEX));
        asseY=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEY));
        asseZ=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEZ));
        campx=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPX)));
        campy=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPY)));
        campz=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_NUMCAMPZ)));
        checkX=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        checkY=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        checkZ=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        sovrac=Integer.parseInt(cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
        
        //Chiusura DB
        dbHelper.close();
        
        
      //Tokenizzazione delle stringhe in array di short        
        if(!asseX.equals(""))
        	s=asseX.split(" "); 
        else {
        	s=new String[1];
        	s[0]="0";
        }
        
        if(!asseY.equals(""))
        	p=asseY.split(" "); 
        else {
        	p=new String[1];
        	p[0]="0";
        }
        
        if(!asseZ.equals(""))
        	q=asseZ.split(" ");
        else {
        	q=new String[1];
        	q[0]="0";
        }
        
 
        x=new short[s.length];
        y=new short[p.length];
        z=new short[q.length];
        

        for(i=0;i<s.length;i++)
    		x[i]=Short.parseShort(s[i]);
        
        
        for(i=0;i<p.length;i++)
    		y[i]=Short.parseShort(p[i]);
        
        
        for(i=0;i<q.length;i++)
    		z[i]=Short.parseShort(q[i]);


        sc=calcoloSovra(sovrac, x.length+y.length+z.length);
        
	        	short[] s1 = new short[0];
		        if(checkX){
		        	s1 = new short[minsize+sc*x.length];
		           	 j=0;
		           	 for(i=0;i<s1.length;i++){
		           		 s1[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;		           	
		            }
		        }
		         
		        short[] s2 =new short[0];
				if(checkZ){
					s2 =new short[minsize+sc*z.length];
		           	 j=0;
		           	 for(i=0;i<s2.length;i++){
		           		s2[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		           	 }
		            }
		        
		        short[] s3 =new short[0];	
		        if(checkY)	{ 
		        	s3 =new short[minsize+sc*y.length];
		        	 j=0;
		        	 for(i=0;i<s3.length;i++){
		        		 s3[i]=y[j];
		        		 j++;
		        		 if(j==y.length) j=0;
	 
		         }
		        }
		        
		        short[] s4 =new short[0];	 
		        if(checkZ){
		        	s4 =new short[minsize+sc*z.length];
		           	 j=0;
		           	 for(i=0;i<s4.length;i++){
		           		s4[i]=z[j];
		           		 j++;
		           		 if(j==z.length) j=0;
		            }
		        }
		        
		        short[] s5 =new short[0];    
		        if(checkX){
		        	s5 =new short[minsize+sc*x.length];
		        	j=0;
		           	 for(i=0;i<s5.length;i++){
		           		s5[i]=x[j];
		           		 j++;
		           		 if(j==x.length) j=0;
		           	 }
		            }
		            
	            short[] s6 =new short[0];    
	           	if(checkY)	{
	           		s6 =new short[minsize+sc*y.length];  
			        j=0;
			        for(i=0;i<s6.length;i++){
			        	s6[i]=y[j];
			        	j++;
			        	if(j==y.length) j=0;
			        }
		         }
				         
		         finale=new short[s1.length+s2.length+s3.length+s4.length+s5.length+s6.length];
		         System.arraycopy(s1, 0, finale, 0, s1.length);	
		         System.arraycopy(s2, 0, finale, s1.length, s2.length);	
		         System.arraycopy(s3, 0, finale, s1.length+s2.length, s3.length);
		         System.arraycopy(s4, 0, finale, s1.length+s2.length+s3.length, s4.length);
		         System.arraycopy(s5, 0, finale, s1.length+s2.length+s3.length+s4.length, s5.length);
		         System.arraycopy(s6, 0, finale, s1.length+s2.length+s3.length+s4.length+s5.length, s6.length);
		         

	    
	 /*
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
        
       

        at = new AudioTrack(AudioManager.STREAM_MUSIC, 24000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 2*finale.length,
        					AudioTrack.MODE_STATIC);
        at.write(finale, 0, finale.length); 
        at.setLoopPoints(0, finale.length-1, -1);
        at.play();
        
       // 10 ore di Sleep XD XD
        SystemClock.sleep(36000000);
        
        //Dopo 10 ore si termina il servizio e l'AudioTrack
        //ES: si è dimenticati il telefono in ricarica con la riproduzione in corso o in pausa
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
	  
        
  public void onDestroy(){
	  this.unregisterReceiver(receiver);
	  Toast.makeText(getApplicationContext(), "Servizio Terminato", Toast.LENGTH_SHORT).show();
	  super.onDestroy();
	  
  }
  
  public static int calcoloSovra(int s, int camp){
	  if(camp>=1000) return (int) (30*s/100);
	  else if (camp<1000) return (int) (40*s/100);
	  else if (camp<500) return (int) (80*s/100);
	  else if (camp<250) return (int) (120*s/100);
	  else return (200*s/100);
	  
  }
  
  	
	
 }
	
                 
            


        
