package team.bugbusters.acceleraudio;

import team.bugbusters.acceleraudio.PlayRecord.MyPlayerReceiver;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

//Riceve l'ID del record nel DB, estrae tutti i dati necessari e riproduce un suono
public class UI4 extends Activity {

	private long id;
	private String pkg_r;
	private Intent playIntentService;
	private Button play;
	private ImageButton pause,riprendi;
	private Intent broadcastIntent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        play=(Button)findViewById(R.id.playbutton);
        pause=(ImageButton)findViewById(R.id.imageButton1);
        riprendi=(ImageButton)findViewById(R.id.imageButton3);
        
        pkg_r=getPackageName();   
        id=getIntent().getLongExtra(pkg_r+".myServiceID", -1);
        
        broadcastIntent = new Intent();
        broadcastIntent.setAction(MyPlayerReceiver.THREAD_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        
        
        
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	/*playIntentService=new Intent(UI4.this, PlayRecord.class);
            	playIntentService.putExtra("ID", id);
            	playIntentService.putExtra("fromUI4", true);
            	startService(playIntentService);*/
            	broadcastIntent.putExtra("Play", true);
            	sendBroadcast(broadcastIntent);	
            }});  
        
        
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	broadcastIntent.putExtra("Pausa", true);
            	sendBroadcast(broadcastIntent);	
            }});
        
        riprendi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	broadcastIntent.putExtra("Riprendi", true);
            	sendBroadcast(broadcastIntent);	
            }});
        
        
	} //FINE onCreate()
	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		broadcastIntent.putExtra("Stop", true);
    	sendBroadcast(broadcastIntent);
    	
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
	
	
}