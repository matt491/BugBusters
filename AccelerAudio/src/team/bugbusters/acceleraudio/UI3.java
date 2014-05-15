package team.bugbusters.acceleraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class UI3 extends Activity {
	
    private String datoX,datoY,datoZ;
    private ProgressBar pbX,pbY,pbZ,pb;
	private int i,end_time;									
    private long prec;							
    private String freq_curr;						
    private String nome; 								// Nome inserito dall'utente tramite EditText
    private String ts;
    private String pkg;
    private Button pause_resume,stop,rec,avan;								
    private EditText nome_music;						//Campo di testo del nome della registrazione
    private TextView t,varcamp;
    Intent intent,intentToSer;
    private SharedPreferences prefs;
    private DbAdapter dbHelper;
    private MyUI3Receiver receiver;
    private IntentFilter filter;
    private ToggleButton toggle;
    private boolean in_pausa=false;
    private MyCounter timer;
    		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        receiver = new MyUI3Receiver();
        intentToSer = new Intent(UI3.this, DataRecord.class);
        filter = new IntentFilter(MyUI3Receiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver,filter);
        
        setContentView(R.layout.ui3_layout);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper = new DbAdapter(this);
        
        
        
        //Toggle button che blocca la rotazione dello schermo
        toggle = (ToggleButton) findViewById(R.id.toggleButton1);
    
        //Intent predisposto per passare alla UI2
        intent=new Intent(getApplicationContext(), UI2.class);
        
        pause_resume=(Button)findViewById(R.id.pause_res);		//Tasto pause/resume
        stop=(Button)findViewById(R.id.stop);					//Tasto Stop
        rec=(Button)findViewById(R.id.record);					//Tasto Record
        avan=(Button)findViewById(R.id.avanz);					//Tasto Avanti
        nome_music = (EditText)findViewById(R.id.nome);			//Campo di testo del nome della registrazione
        t= (TextView)findViewById(R.id.last);					//Mostra la durata della registrazione in corso
      
        //3 ProgressBar per visualizzare la variazione di accelerazione lungo i 3 assi
		 pbX=(ProgressBar)findViewById(R.id.x_axis);
		 pbY=(ProgressBar)findViewById(R.id.y_axis);
		 pbZ=(ProgressBar)findViewById(R.id.z_axis);
		 pb=(ProgressBar)findViewById(R.id.progressBar1);
				 
		//Visualizza il numero di campioni registrati
		varcamp=(TextView)findViewById(R.id.campioni);

        
        //Diabilito i pulsanti all'inizio
        pause_resume.setEnabled(false);
        stop.setEnabled(false);
        avan.setEnabled(false);
        rec.setEnabled(true);
        
        
        //Toggle premuto
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
					Display disp = wm.getDefaultDisplay();
					int orientation = disp.getRotation();
					
					if(orientation==0) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  
					if(orientation==1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					//Non è supportato nelle API8, perchè nelle API8 non era previsto il reverse landscape
					if(orientation==3) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
								
				}
				else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						
			}
		});
       
        //Tasto Pausa/Resume premuto
        pause_resume.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!in_pausa){
            		pause_resume.setText("Riprendi");
            		in_pausa=true;
            		prec=timer.last;
            		timer.cancel();
            		stopService(intentToSer);
            	}
            	else {
            		pause_resume.setText("Pausa");
            		intentToSer.putExtra("fromUI3", true);
            		intentToSer.putExtra("VecchioX", datoX);
            		intentToSer.putExtra("VecchioY", datoY);
            		intentToSer.putExtra("VecchioZ", datoZ);
            		intentToSer.putExtra("attFreq", freq_curr);
            		intentToSer.putExtra("attFineTempo", end_time);
            		intentToSer.putExtra("attCamp", i);
            		in_pausa=false;
            		         		
            		timer=new MyCounter(end_time*1000-prec,100);
            		timer.end=end_time*1000-prec;
            		timer.previous=prec;
            		timer.start();
            		startService(intentToSer);
            	}
            }
        });
        

        //Tasto Stop premuto
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            		avan.setEnabled(true);
            		pause_resume.setEnabled(false);
            		stop.setEnabled(false);
            		timer.cancel();
            		
            		stopService(intentToSer);
            		
            	}
        });
         
        //Tasto Record premuto
        rec.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { 
            	if(toggle.isChecked()){
            		
            		
            		avan.setEnabled(false);
            		pause_resume.setEnabled(true);
            		stop.setEnabled(true);
            		rec.setEnabled(false);
            		toggle.setEnabled(false);
            		end_time=prefs.getInt("duratadef", 50);
            		pb.setMax(end_time);
            		
            		timer=new MyCounter(end_time*1000,100);
            		timer.end=end_time*1000;	
            		timer.start();
            		
            		intentToSer.putExtra("fromUI3", true);
            		startService(intentToSer);
            	}
            	else Toast.makeText(getApplicationContext(), "Devi bloccare lo Schermo", Toast.LENGTH_SHORT).show();
            }
        });
        
        //Tasto Avanti premuto
        avan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	pkg=getPackageName();
            	String nomeinserito=nome_music.getText().toString();
            	
            	//Se il nome inserito e la stringa vuota o  gia presente nel DB allora	
            	if((nomeinserito.equals("")) || sameName(nomeinserito))
            		Toast.makeText(getApplicationContext(), "Immetere un nome valido!", Toast.LENGTH_SHORT).show();
            		
            	else {	
            		nome = nome_music.getText().toString();
            		ts = DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
            	
            		dbHelper.open();           	
        		
            		long id_to_ui2=dbHelper.createRecord(nome, "", datoX.toString(), datoY.toString(), datoZ.toString(),
            				""+ prefs.getBoolean("Xselect", true),""+ prefs.getBoolean("Yselect", true), ""+prefs.getBoolean("Zselect", true),
        					i, UI5.campToString(prefs.getInt("sovrdef", 0)), ts, ts, null);
            		
            		String cod=DataRecord.codifica(datoX.toString(),datoY.toString(), datoY.toString(), ts, id_to_ui2);
        		
            		//Update dei dati immagine
            		dbHelper.updateImageCode(id_to_ui2, cod);
            		
            		//Chiusura del DB
            		dbHelper.close();
        		
            		intent.putExtra(pkg+".myIdToUi2", id_to_ui2);
            
            		startActivity(intent);
            		finish();
            	}
            	
          }
       });
               
    }    //FINE onCreate()
    
 
     @Override
     public void onDestroy() {
         this.unregisterReceiver(receiver);
         super.onDestroy();
     }

	
	//Metodo che controlla se e gia presente un NOME di una music session nel DB
	public boolean sameName(String s){
			dbHelper.open();
			Cursor cursor=dbHelper.fetchRecordByFilter(s);
			while (cursor.moveToNext()) {
				String rNAME = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME) );
				if(s.equals(rNAME)) {
					cursor.close();
					dbHelper.close();
					return true;
				}
			} 
			cursor.close();
			dbHelper.close();
			return false;
	}
	 
	//Menu Option, passa alla UI5
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Preferenze").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
            		Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
                    startActivity(prefIntentUI5);
                    return true;
            }
		});;

	        return true;
	}
	
	  public class MyCounter extends CountDownTimer{
		 private long end;
		 private long last;
		 private long previous=0;
	        public MyCounter(long millisInFuture, long countDownInterval) {
	            super(millisInFuture, countDownInterval);
	        }
	 
	        @Override
	        public void onFinish() {
	            t.setText((float)((end+previous)/100)/10+"");
	            pb.setProgress((int)(end+previous)/1000);
	            Toast.makeText(UI3.this, "Registrazione Terminata", Toast.LENGTH_SHORT).show();
	            stopService(intentToSer);
	            avan.setEnabled(true);
        		pause_resume.setEnabled(false);
        		stop.setEnabled(false);
	        }
	 
	        @Override
	        public void onTick(long millisUntilFinished) {
	            t.setText((float)((previous+end-millisUntilFinished)/100)/10 +"");
	            pb.setProgress((int)(previous+end-millisUntilFinished)/1000);
	            last=previous+end-millisUntilFinished;
	        }
	    }
	

	   public class MyUI3Receiver extends BroadcastReceiver{

		   public static final String PROCESS_RESPONSE = "team.bugbusters.acceleraudio.intent.action.PROCESS_RESPONSE";
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            pbX.setProgress(intent.getIntExtra("intPbX", 0));
	            pbY.setProgress(intent.getIntExtra("intPbY", 0));
	            pbZ.setProgress(intent.getIntExtra("intPbZ", 0));
	            
	            i=intent.getIntExtra("serCamp",0);
	            varcamp.setText(""+i);
	            datoX=intent.getStringExtra("ValoreX");
	            datoY=intent.getStringExtra("ValoreY");
	            datoZ=intent.getStringExtra("ValoreZ");
	            freq_curr=intent.getStringExtra("serFreq");
	            end_time=intent.getIntExtra("serDur",0);
        
	           
	            
	        }


	        }

	//Questo metodo per la duplicazione va messo nella UI1 quando ci sara'
	   private void duplica(long id){
		   dbHelper.open();
		   Cursor c=dbHelper.fetchRecordById(id);
		   c.moveToNext();
		   String n=c.getString(c.getColumnIndex(DbAdapter.KEY_NAME));
	       String d=c.getString(c.getColumnIndex(DbAdapter.KEY_DURATION));
	       String asseX=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEX));
	       String asseY=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEY));
	       String asseZ=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEZ));
	       boolean checkX=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKX)));
	       boolean checkY=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKY)));
	       boolean checkZ=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKZ)));
	       int ncamp=c.getInt(c.getColumnIndex(DbAdapter.KEY_NUMCAMP));
	       String sovrac=c.getString(c.getColumnIndex(DbAdapter.KEY_UPSAMPLE));
	       String datar=c.getString(c.getColumnIndex(DbAdapter.KEY_DATE));
	       String dataul=DateFormat.format("dd-MM-yyyy kk:mm", new java.util.Date()).toString();
	       
	       if(checkX==true) checkX=!checkX;
	       if(checkY==false) checkY=!checkY;
	       if(checkZ==true) checkZ=!checkZ;
	       if(sovrac.equals("Scelta 0")) sovrac="Scelta 1";
	       if(sovrac.equals("Scelta 1")) sovrac="Scelta 2";
	       if(sovrac.equals("Scelta 2")) sovrac="Scelta 3";
	       if(sovrac.equals("Scelta 3")) sovrac="Scelta 4";
	       if(sovrac.equals("Scelta 4")) sovrac="Scelta 0";
	       
	       long id_new=dbHelper.createRecord(n+"_", d, asseX, asseY, asseZ, ""+checkX, ""+checkY, ""+checkZ, ncamp, sovrac,	datar, dataul, null);
			String code = DataRecord.codifica(asseX, asseY, asseX, dataul, id_new);
			dbHelper.updateRecordNameAndImage(id, n+id, code);
			
		   c.close();
		   dbHelper.close();
	   }
	   
	
	}