package team.bugbusters.acceleraudio;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

public class PlayRecord extends IntentService {

	private DbAdapter dbHelper;
	private Cursor cr;
	private long id;
	private double m;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int ncamp;
	private String sovrac;
	private String[] s;
 	private short[] x,y,z;
	
	
	public PlayRecord() {
		super("PlayRecord");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
        
        dbHelper = new DbAdapter(this);
        
        id=intent.getLongExtra("ID", -1);
        
        //Query che individua l'unica riga con questo ID
        dbHelper.open();
        cr=dbHelper.fetchRecordById(id);
        
        //Lettura dei dati dal record(cursor) restituito
        m=Double.parseDouble(cr.getString(cr.getColumnIndex(DbAdapter.KEY_DURATION)));
        asseX=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEX));
        asseY=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEY));
        asseZ=cr.getString(cr.getColumnIndex(DbAdapter.KEY_ASSEZ));
        checkX=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKX)));
        checkY=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKY)));
        checkZ=Boolean.parseBoolean(cr.getString(cr.getColumnIndex(DbAdapter.KEY_CHECKZ)));
        ncamp=cr.getInt(cr.getColumnIndex(DbAdapter.KEY_NUMCAMP));
        sovrac=cr.getString(cr.getColumnIndex(DbAdapter.KEY_UPSAMPLE));

        x=new short[ncamp/3];
        y=new short[ncamp/3];
        z=new short[ncamp/3];
        
        //Tokenizzazione delle stringhe in array di short
        s=asseX.split(" "); 
        for(int i=0;i<s.length-1;i++)
    		x[i]=Short.parseShort(s[i]);
        
        s=asseY.split(" "); 
        for(int i=0;i<s.length-1;i++)
    		y[i]=Short.parseShort(s[i]);
        
        s=asseZ.split(" "); 
        for(int i=0;i<s.length-1;i++)
    		z[i]=Short.parseShort(s[i]);
        
        
        

	}

}
