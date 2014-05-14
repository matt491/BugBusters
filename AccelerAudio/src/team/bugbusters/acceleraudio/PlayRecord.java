package team.bugbusters.acceleraudio;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class PlayRecord extends IntentService {

	private DbAdapter dbHelper;
	private Cursor cr;
	private long id_to_process;
	private double m;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int ncamp;
	private String sovrac;
	private String[] s;
 	private short[] x,y,z,w;
	long re;
	
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
        m=Float.parseFloat(cr.getString(cr.getColumnIndex(DbAdapter.KEY_DURATION)));
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
        
        s=asseX.split(" "); 
        
        x=new short[s.length];
        y=new short[s.length];
        z=new short[s.length];
        
        //Tokenizzazione delle stringhe in array di short
        int i;
        for(i=0;i<s.length;i++)
    		x[i]=Short.parseShort(s[i]);
        
        s=asseY.split(" "); 
        for(i=0;i<s.length;i++)
    		y[i]=Short.parseShort(s[i]);
        
        s=asseZ.split(" "); 
        for(i=0;i<s.length;i++)
    		z[i]=Short.parseShort(s[i]);
      
        int j=0;
        
        w=new short[3*s.length];
        for(i=0; i<w.length-2;i=i+3){
        	w[i]=x[j];
        	w[i+1]=y[j];
        	w[i+2]=z[j];
        	j++;
        }
        
        
        
        
        
        re=System.currentTimeMillis();
      		

              AudioHelper device = new AudioHelper( );
              
             
           //   while(System.currentTimeMillis()-re<10000 ){
            	//  while(System.currentTimeMillis()-re<100 )
                	  device.writeSamples( w ); 
            	  
            //	  while(System.currentTimeMillis()-re<100 )
             //   	  device.writeSamples( y ); 
            	  
            	//  while(System.currentTimeMillis()-re<200 )
              //  	  device.writeSamples( z ); 
            	  
            	  
           //   }
            	  	
          
             
              device.stop();
           }
     
        
        
        
        
	}
	
                 
            


        
