package team.bugbusters.acceleraudio;

//import team.bugbusters.acceleraudio.PlayRecord.MyPlayerReceiver;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

//Riceve l'ID del record nel DB, estrae tutti i dati necessari e riproduce un suono
public class UI4 extends Activity {

	private long id;
	private String pkg_r;
	private Intent playIntentService;
	private Button play;
	private ImageButton pause,riprendi;
	private ImageView iv;
	private TextView name;
	private TextView duration;
	private Intent broadcastIntent;
	private DbAdapter db;
	private Cursor c;
	private String nome;
	private String thumbnail;
	private String durata;
	private static boolean primavolta=true;
	public static final String THREAD_RESPONSE = "team.bugbusters.acceleraudio.intent.action.THREAD_RESPONSE";
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ui4_layout);
        play=(Button)findViewById(R.id.playbutton);
        pause=(ImageButton)findViewById(R.id.imageButton1);
        riprendi=(ImageButton)findViewById(R.id.imageButton3);
        iv = (ImageView) findViewById(R.id.imageView1);
        name = (TextView) findViewById(R.id.textView1);
        duration = (TextView) findViewById(R.id.textView3);
        db = new DbAdapter(this);
        
        pkg_r=getPackageName();   
        id=getIntent().getLongExtra(pkg_r+".myServiceID", -1);
        
        db.open();
        c = db.fetchRecordById(id);
        c.moveToNext();
        
        thumbnail = c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
        nome = c.getString(c.getColumnIndex(DbAdapter.KEY_NAME));
        durata = c.getString(c.getColumnIndex(DbAdapter.KEY_DURATION));
        
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
        duration.setText(durata);
        
        
        broadcastIntent = new Intent();
        broadcastIntent.setAction(THREAD_RESPONSE);
        
        if(primavolta){
        	primavolta=false;
        playIntentService=new Intent(UI4.this, PlayRecord.class);
        playIntentService.putExtra("ID", id);
    	playIntentService.putExtra("fromUI4", true);
    	startService(playIntentService);
        }
        
 
        
        
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	broadcastIntent.putExtra("Pausa", true);
            	broadcastIntent.putExtra("Riprendi", false);
            	broadcastIntent.putExtra("Play", false);
            	sendBroadcast(broadcastIntent);
            	pause.setImageResource(android.R.drawable.ic_media_play);
            }});
        
        riprendi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	broadcastIntent.putExtra("Riprendi", true);
            	broadcastIntent.putExtra("Pausa", false);
            	broadcastIntent.putExtra("Play", false);
            	sendBroadcast(broadcastIntent);	
            }});
        
        
	} //FINE onCreate()
	
	//Quando viene premuto il tasto Back
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		broadcastIntent.putExtra("Stop", true);
    	broadcastIntent.putExtra("Pausa", false);
    	broadcastIntent.putExtra("Play", false);
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