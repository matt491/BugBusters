package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//Riceve l'ID del record nel DB, estrae tutti i dati necessari e riproduce un suono
public class UI4 extends Activity {

	private DbAdapter dbHelper;
	private Cursor cr;
	private long id;
	private double m;
	private String asseX,asseY,asseZ;
	private boolean checkX,checkY,checkZ;
	private int ncamp;
	private String sovrac, pkg_r;
	private String[] s;
 	private short[] x,y,z;
	private Intent playIntentService;
	private Button play;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        play=(Button)findViewById(R.id.playbutton);
        
        
        pkg_r=getPackageName();   
        id=getIntent().getLongExtra(pkg_r+".myServiceID", 2);
        
        playIntentService=new Intent(UI4.this, PlayRecord.class);
        
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	playIntentService.putExtra("ID", 5);
            	playIntentService.putExtra("fromUI4", true);
            	startService(playIntentService);
            	
            }});
        
    
        
        
	} //FINE onCreate()
	
	
	
	
}