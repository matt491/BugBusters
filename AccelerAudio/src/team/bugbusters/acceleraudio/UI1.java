package team.bugbusters.acceleraudio;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
		 * Alla pressione di un elemento della ListView si passa alla UI#2 dove sarˆ possibile visualizzarne (ed eventualmente modificarne) il dettaglio. 
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
			finish();
		}
		});
		
		registerForContextMenu(lv);
	}
	
	/*
	 * Attraverso questo metodo vengono recuperati dal database ID, stringa di codifica dell'immagine, nome, data di ultima modifica e durata(?) di ciascuna music session presente nel database.
	 * L'ID, che non viene visualizzato, sarˆ passato alla UI#2 tramite il relativo intent.
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
		Intent intent = new Intent(UI1.this , UI3.class);
		startActivity(intent);
		finish();
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
		
	
		String[] dati;
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		
		case R.id.Duplica:
			dati = (String[]) lv.getAdapter().getItem(info.position);
			duplica(Long.parseLong(dati[0]));
			
			List<String[]> data = dataToFill();
			CustomList cl = new CustomList(UI1.this, data);
			lv.setAdapter(cl);
			
			return(true);
			
		case R.id.Rinomina:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.renameAlert);
			alert.setMessage(R.string.renameAlertMessage);
			dati = (String[]) lv.getAdapter().getItem(info.position);
			final long id_to_rename=Long.parseLong(dati[0]);
			final EditText input = new EditText(this);
			input.setImeOptions(EditorInfo.IME_ACTION_SEND);
			input.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
			alert.setView(input);
			
			alert.setPositiveButton(R.string.okAlert, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			
			alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			final AlertDialog dialog = alert.create();
			dialog.show();
			
			dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String nuovoNome = input.getText().toString();
					if(nuovoNome.contains("'")) {
						Toast.makeText(getApplicationContext(), R.string.apiceNonConsentito, Toast.LENGTH_LONG).show();
					}
					else if(sameName(db, nuovoNome)) {
						Toast toast = Toast.makeText(getApplicationContext(), R.string.ToastAlertSameName, Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else {
						db.open();
						db.updateNameOnly(id_to_rename,nuovoNome);
						db.close();
						List<String[]> data1 = dataToFill();
						CustomList cl = new CustomList(UI1.this, data1);
						lv.setAdapter(cl);
						dialog.dismiss();
					}
					
				}
			});
			return(true);
		
		case R.id.Elimina:
			
			AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
			alert2.setTitle(R.string.Delete);
			alert2.setMessage(R.string.DeleteMessage);
			dati = (String[]) lv.getAdapter().getItem(info.position);
			final long id_to_delete=Long.parseLong(dati[0]);
			
			alert2.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					db.open();
					db.deleteRecord(id_to_delete);
					db.close();
					
					List<String[]> data2 = dataToFill();
					CustomList cl2 = new CustomList(UI1.this, data2);
					lv.setAdapter(cl2);	
				}
			});
			
			alert2.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			alert2.show();
			return(true);
			
		case R.id.Riproduci:
			String[] dati_sessione = (String[]) lv.getAdapter().getItem(info.position);
			Intent toUi4 = new Intent(this, UI4.class);
			pkg = getPackageName();
			toUi4.putExtra(pkg + ".myServiceID", Long.parseLong(dati_sessione[0]));
			startActivity(toUi4);
			finish();
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
	       String sovrac_new;
	       
	       if(checkX==true) checkX=!checkX;
	       if(checkY==false) checkY=!checkY;
	       if(checkZ==true) checkZ=!checkZ;
	       if(sovrac.equals("Scelta 0")) sovrac_new="Scelta 1";
	       if(sovrac.equals("Scelta 1")) sovrac_new="Scelta 2";
	       if(sovrac.equals("Scelta 2")) sovrac_new="Scelta 3";
	       if(sovrac.equals("Scelta 3")) sovrac_new="Scelta 4";
	       else sovrac_new="Scelta 0";
	       
	       long id_new=db.createRecord(n+"_", d, asseX, asseY, asseZ, ""+checkX, ""+checkY, ""+checkZ, ncamp, sovrac_new, datar, dataul, null);
		   String code = DataRecord.codifica(asseX, asseY, asseZ, dataul, id_new);
		   db.updateRecordNameAndImage(id_new, n+"_"+id_new, code);
			
		   c.close();
		   db.close();
	   }
	
	/*
	 * Metodo che controlla se e gia' presente un NOME di una music session nel DB
	 * --Preso in prestito da Loris-- :-)
	 */
		public static boolean sameName(DbAdapter db,String s){
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
				finish();
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


