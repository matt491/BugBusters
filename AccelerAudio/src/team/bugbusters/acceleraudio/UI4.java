package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//Riceve l'ID del record nel DB, estrae tutti i dati necessari e riproduce un suono
public class UI4 extends Activity {

	private long id;
	private String pkg_r;
	private Intent playIntentService;
	private Button play;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        play=(Button)findViewById(R.id.playbutton);
        
        
        pkg_r=getPackageName();   
        id=getIntent().getLongExtra(pkg_r+".myServiceID", -1);
        
        
        
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	playIntentService=new Intent(UI4.this, PlayRecord.class);
            	playIntentService.putExtra("ID", id);
            	playIntentService.putExtra("fromUI4", true);
            	startService(playIntentService);
            	
            }});
        
    
        
        
	} //FINE onCreate()
	
	
	
	
}