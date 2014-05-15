package team.bugbusters.acceleraudio;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class UI1 extends Activity {

	private ListView lv;
	private DbAdapter db;
	private String pkg;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui1_layout);
		
		lv = (ListView) findViewById(R.id.listView1);
		
		db = new DbAdapter(UI1.this);
		
		List<String[]> data = dataToFill();
		
		//View header = getLayoutInflater().inflate(R.layout.header, null);
		//lv.addHeaderView(header);
		
		
		lv.setEmptyView(findViewById(R.id.empty));
		
		CustomList cl = new CustomList(UI1.this, data);
		lv.setAdapter(cl);
		
		/*
		 * Alla pressione di un elemento della ListView si passa alla UI#2 dove sar� possibile visualizzarne (ed eventualmente modificarne) il dettaglio. 
		 */
		lv.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			String[] s = (String[])adapter.getItemAtPosition(position);
			long musicSessionsId = Long.parseLong(s[0]);
			Intent intent = new Intent(getApplicationContext(), UI2.class);
			pkg = getPackageName();
			intent.putExtra(pkg + ".myIdToUi2", musicSessionsId);
			startActivity(intent);
		}
		});
		
		registerForContextMenu(lv);
	}
	
	/*
	 * Attraverso questo metodo vengono recuperati dal database ID, stringa di codifica dell'immagine, nome, data di ultima modifica e durata(?) di ciascuna music session presente nel database.
	 * L'ID, che non viene visualizzato, sar� passato alla UI#2 tramite il relativo intent.
	 * La durata (di riproduzione, non di registrazione) al momento non viene visualizzata. 
	 */
	private List<String[]> dataToFill() {
		db.open();
		
		Cursor c = db.fetchAllRecord();
		
		List<String[]> myList = new ArrayList<String[]>();
		
		
		int idIndex = c.getColumnIndex(DbAdapter.KEY_RECORDID);
		int thumbnailIndex = c.getColumnIndex(DbAdapter.KEY_IMM);
		int nameIndex = c.getColumnIndex(DbAdapter.KEY_NAME);
		int lastIndex = c.getColumnIndex(DbAdapter.KEY_LAST);
		int durationIndex = c.getColumnIndex(DbAdapter.KEY_DURATION);
		
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String[] row = new String[5];
			row[0] = c.getString(idIndex);
			row[1] = c.getString(thumbnailIndex);
			row[2] = c.getString(nameIndex);
			row[3] = c.getString(lastIndex);
			row[4] = c.getString(durationIndex);
			myList.add(row);
		}
		
		db.close();
		
		return myList;
	}
	
	/*
	 * Alla pressione del tasto +, si passa alla UI#3 dove e' possibile registrare una nuova sessione
	 */
	public void onClick(View view) {
		Intent intent = new Intent(getApplicationContext(), UI3.class);
		startActivity(intent);
	}
	
	/*
	 * Creazione del context menu definito nell'apposito file XML
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu_ui1, menu);
	}
	
	/*
	 * 
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		
		case R.id.Duplica:
			String[] s = (String[]) lv.getAdapter().getItem(info.position);
			duplica(Long.parseLong(s[0]));
			((ArrayAdapter<String[]>)lv.getAdapter()).notifyDataSetChanged();
			return(true);
			
		case R.id.Rinomina:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.renameAlert);
			alert.setMessage(R.string.renameAlertMessage);
			
			final EditText input = new EditText(this);
			alert.setView(input);
			
			alert.setPositiveButton(R.string.okAlert, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String nuovoNome = input.getText().toString();
					if(sameName(nuovoNome)) {
						Toast.makeText(getApplicationContext(), R.string.ToastAlertSameName, Toast.LENGTH_LONG).show();
					}
					else {
						//qui bidogna aggiornare il nome nel database e nella listview
					}
	
				}
			});
			
			alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alert.show();
			return(true);
		
		case R.id.Elimina:
			String[] dati = (String[]) lv.getAdapter().getItem(info.position);
			db.open();
			db.deleteRecord(Long.parseLong(dati[0]));
			db.close();
			return(true);
			
		case R.id.Riproduci:
			String[] dati_sessione = (String[]) lv.getAdapter().getItem(info.position);
			Intent toUi4 = new Intent(this, UI4.class);
			pkg = getPackageName();
			//.myServiceID???
			toUi4.putExtra(pkg + ".myServiceID", Long.parseLong(dati_sessione[0]));
			startActivity(toUi4);
			return(true);
		}
		
		
		return (super.onOptionsItemSelected(item));
	}
	
	/*
	 * Questo metodo per la duplicazione va messo nella UI1 quando ci sara'
	 */
	   private void duplica(long id){
		   db.open();
		   Cursor c=db.fetchRecordById(id);
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
	       
	       long id_new=db.createRecord(n+"_", d, asseX, asseY, asseZ, ""+checkX, ""+checkY, ""+checkZ, ncamp, sovrac,	datar, dataul, null);
			String code = DataRecord.codifica(asseX, asseY, asseX, dataul, id_new);
			db.updateRecordNameAndImage(id, n+id, code);
			
		   c.close();
		   db.close();
	   }
	
	/*
	 * Metodo che controlla se e gia' presente un NOME di una music session nel DB
	 * --Preso in prestito da Loris-- :-)
	 */
		public boolean sameName(String s){
				db.open();
				Cursor cursor=db.fetchRecordByFilter(s);
				while (cursor.moveToNext()) {
					String rNAME = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME) );
					if(s.equals(rNAME)) {
						cursor.close();
						db.close();
						return true;
					}
				} 
				cursor.close();
				db.close();
				return false;
		}
	
	/*
	 * Alla pressione del tasto back viene notificato all'utente che l'applicazione sta per chiudersi.
	 * Se la risposta alla domanda "Sei sicuro di voler terminare l'app?" e' "Si", l'activity viene terminata;
	 * altrimenti, se la risposta e' "No", l'activity rimane in vita.
	 * 
	 */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UI1.this);
		
		
		alertDialogBuilder.setTitle(R.string.alertTitle);
		alertDialogBuilder.setMessage(R.string.alertMessage);
		
		alertDialogBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				UI1.this.finish();
			}
		});
		
		alertDialogBuilder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
