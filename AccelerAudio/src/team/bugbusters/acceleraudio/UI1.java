package team.bugbusters.acceleraudio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private SharedPreferences prefs;
	private static final int BY_INSERTION = -1; //Default
	private static final int BY_NAME = 0;
	private static final int BY_DATE = 1;
	private static final int BY_DURATION = 2;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui1_layout);
		
		lv = (ListView) findViewById(R.id.listView1);
		
		db = new DbAdapter(UI1.this);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int way;
		
		if(prefs.getBoolean("sortedByName", false)) {
			way = BY_NAME;
		}
		else if(prefs.getBoolean("sortedByDate", false)) {
			way = BY_DATE;
		}
		else if(prefs.getBoolean("sortedByDuration", false)) {
			way = BY_DURATION;
		}
		else {
			way = BY_INSERTION;  //Default
		}
		
		List<String[]> data = dataToFill(way);
		
		lv.setEmptyView(findViewById(R.id.empty));
		
		CustomList cl = new CustomList(UI1.this, data);
		lv.setAdapter(cl);
		
		/*
		 * Alla pressione di un elemento della ListView si passa alla UI#2 dove sara' possibile visualizzarne (ed eventualmente modificarne) il dettaglio. 
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
	 * L'ID, che non viene visualizzato, sar� passato alla UI#2 tramite il relativo intent.
	 * La durata (di riproduzione, non di registrazione) al momento non viene visualizzata. 
	 */
	private List<String[]> dataToFill(int way) {
		db.open();
		Cursor c;
		
		switch(way) {
		case BY_NAME:
			c = db.fetchAllRecordSortedByName();
			break;
		case BY_DATE:
			c = db.fetchAllRecordSortedByDate();
			break;
		case BY_DURATION:
			c = db.fetchAllRecordSortedByDuration();
			break;
		default:  //case BY_INSERTION
			c = db.fetchAllRecord();
			break;
		}
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
		
		c.close();
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		String[] dati = (String[]) lv.getAdapter().getItem(info.position);
		menu.setHeaderTitle(dati[2]);
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu_ui1, menu);
		
	}
	
	/*
	 * 
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final String[] dati = (String[]) lv.getAdapter().getItem(info.position);
		switch(item.getItemId()) {
		
		case R.id.Duplica:
			
			String[] nuoviDati = duplica(Long.parseLong(dati[0]));
			
			CustomList runningCl = (CustomList) lv.getAdapter();
			List<String[]> nuovaLista = new ArrayList<String[]>();
			for(int i = 0; i < runningCl.getCount(); i++) {
				nuovaLista.add(runningCl.getItem(i));
			}
			nuovaLista.add(nuoviDati);
			
			if(prefs.getBoolean("sortedByName", false)) {
			Collections.sort(nuovaLista, new Comparator<String[]>() {
				@Override
				public int compare(String[] s1, String[] s2) {
					return s1[2].compareToIgnoreCase(s2[2]);
				}
			});
			}
			else if(prefs.getBoolean("sortedByDate", false)) {
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[3].compareTo(s2[3]);
					}
				});
			}
			else if(prefs.getBoolean("sortedByDuration", false)) {
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[4].compareTo(s2[4]);
					}
				});
			}
			
			int pos = nuovaLista.indexOf(nuoviDati);
			
			runningCl.clear();
			runningCl.addAll(nuovaLista);
			runningCl.notifyDataSetChanged();
			//lv.smoothScrollToPosition(pos);
			lv.setSelection(pos);
			
			return(true);
			
		case R.id.Rinomina:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.Rename);
			alert.setIcon(android.R.drawable.ic_menu_edit);
			alert.setMessage(R.string.renameAlertMessage);
			final long id_to_rename=Long.parseLong(dati[0]);
			final String vecchioNome=dati[2];
			final EditText input = new EditText(this);
			input.setImeOptions(EditorInfo.IME_ACTION_SEND);
			input.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			input.setText(vecchioNome);
			input.setSelection(input.getText().length());
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
					if(!nuovoNome.equals(vecchioNome))
					if(nuovoNome.contains("'") || nuovoNome.contains("_")) {
						Toast.makeText(getApplicationContext(), R.string.apiceNonConsentito, Toast.LENGTH_SHORT).show();
					}
					else if(sameName(db, nuovoNome) || nuovoNome.equals("")) {
						Toast toast = Toast.makeText(getApplicationContext(), R.string.ToastAlertSameName, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else {
						CustomList runningCl = (CustomList) lv.getAdapter();
						runningCl.remove(dati);
						dati[2] = nuovoNome; 
						List<String[]> nuovaLista = new ArrayList<String[]>();
						for(int i = 0; i < runningCl.getCount(); i++) {
							nuovaLista.add(runningCl.getItem(i));
						}
						nuovaLista.add(dati);
						if(prefs.getBoolean("sortedByName", false)) {
						Collections.sort(nuovaLista, new Comparator<String[]>() {
							@Override
							public int compare(String[] s1, String[] s2) {
								return s1[2].compareToIgnoreCase(s2[2]);
							}
						});
						}
						else if(prefs.getBoolean("sortedByDate", false)) {
							Collections.sort(nuovaLista, new Comparator<String[]>() {
								@Override
								public int compare(String[] s1, String[] s2) {
									return s1[3].compareTo(s2[3]);
								}
							});
						}
						else if(prefs.getBoolean("sortedByDuration", false)) {
							Collections.sort(nuovaLista, new Comparator<String[]>() {
								@Override
								public int compare(String[] s1, String[] s2) {
									return s1[4].compareTo(s2[4]);
								}
							});
						}
						
						int pos = nuovaLista.indexOf(dati);
						
						runningCl.clear();
						runningCl.addAll(nuovaLista);
						runningCl.notifyDataSetChanged();
						
						lv.setSelection(pos);
						//In alternativa:
						//lv.smoothScrollToPosition(pos);
						
						db.open();
						db.updateNameOnly(id_to_rename,nuovoNome);
						db.close();
						
						dialog.dismiss();
					}
					
					else dialog.dismiss();
				}
			});
			return(true);
		
		case R.id.Elimina:
			
			AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
			alert2.setTitle(R.string.Delete);
			alert2.setIcon(android.R.drawable.ic_menu_delete);
			alert2.setMessage(R.string.DeleteMessage);
			final long id_to_delete=Long.parseLong(dati[0]);
			
			alert2.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CustomList runningCl = (CustomList) lv.getAdapter();
					runningCl.remove(dati);
					runningCl.notifyDataSetChanged();
					db.open();
					db.deleteRecord(id_to_delete);
					db.close();
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
			return(true);
		}
		
		
		return (super.onOptionsItemSelected(item));
	}
	
	
	/*
	 * Con questo metodo viene duplicata una music session nel database
	 */
	   private String[] duplica(long id){
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
	       
	       if(checkX) checkY=!checkY;
	       if(checkY) checkZ=!checkZ;
	       if(checkZ) checkX=!checkX;
	       if(sovrac.equals("Scelta 0")) sovrac_new="Scelta 1";
	       else if(sovrac.equals("Scelta 1")) sovrac_new="Scelta 2";
	       else if(sovrac.equals("Scelta 2")) sovrac_new="Scelta 3";
	       else if(sovrac.equals("Scelta 3")) sovrac_new="Scelta 4";
	       else sovrac_new="Scelta 0";
	       
	       long id_new=db.createRecord(n+"_", d, asseX, asseY, asseZ, ""+checkX, ""+checkY, ""+checkZ, ncamp, sovrac_new, datar, dataul, null);
		   String code = DataRecord.codifica(asseX, asseY, asseZ, dataul, id_new);
		   db.updateRecordNameAndImage(id_new, n+"_"+id_new, code);
			
		   c.close();
		   db.close();
		   
		   String[] ret = new String[5];
		   ret[0] = "" + id_new;
		   ret[1] = code;
		   ret[2] = n + "_" + id_new;
		   ret[3] = dataul;
		   ret[4] = d;
		   
		   return ret;
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
			MenuInflater menuInflater = getMenuInflater();
			menuInflater.inflate(R.menu.option_menu, menu);
			return true;
			}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			final Editor prefsEditor = prefs.edit();;
			final CustomList runningCl = (CustomList) lv.getAdapter();
			final List<String[]> nuovaLista = new ArrayList<String[]>();
			for(int i = 0; i < runningCl.getCount(); i++) {
				nuovaLista.add(i, runningCl.getItem(i));
			}
			
			switch(item.getItemId()) {
			
			case R.id.Preferenze:
				Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
	            startActivity(prefIntentUI5);
	            return(true);
	            
			case R.id.Ordina:
				if(prefs.getBoolean("sortedByName", false))
					break;
				prefsEditor.putBoolean("sortedByName", true).commit();
				prefsEditor.putBoolean("sortedByDate", false).commit();
				prefsEditor.putBoolean("sortedByDuration", false).commit();
				
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[2].compareToIgnoreCase(s2[2]);
					}
				});
				
				runningCl.clear();
				runningCl.addAll(nuovaLista);
				runningCl.notifyDataSetChanged();
				return(true);
			
			case R.id.Numera:
				if(!prefs.getBoolean("sortedByName", false) && !prefs.getBoolean("sortedByDate", false) && !prefs.getBoolean("sortedByDuration", false))
					break;
				prefsEditor.putBoolean("sortedByName", false).commit();
				prefsEditor.putBoolean("sortedByDate", false).commit();
				prefsEditor.putBoolean("sortedByDuration", false).commit();
				
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[0].compareTo(s2[0]);
					}
				});
				
				runningCl.clear();
				runningCl.addAll(nuovaLista);
				runningCl.notifyDataSetChanged();
				return(true);
				
			case R.id.OrdinaData:
				if(prefs.getBoolean("sortedByDate", false))
					break;
				prefsEditor.putBoolean("sortedByDate", true).commit();
				prefsEditor.putBoolean("sortedByName", false).commit();
				prefsEditor.putBoolean("sortedByDuration", false).commit();
				
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[3].compareTo(s2[3]);
					}
				});
				
				runningCl.clear();
				runningCl.addAll(nuovaLista);
				runningCl.notifyDataSetChanged();
				return(true);
				
			case R.id.OrdinaDurata:
				if(prefs.getBoolean("sortedByDuration", false))
					break;
				prefsEditor.putBoolean("sortedByDuration", true).commit();
				prefsEditor.putBoolean("sortedByName", false).commit();
				prefsEditor.putBoolean("sortedByDate", false).commit();
				
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						return s1[4].compareTo(s2[4]);
					}
				});
				
				runningCl.clear();
				runningCl.addAll(nuovaLista);
				runningCl.notifyDataSetChanged();
				return(true);
			}
			
			return (super.onOptionsItemSelected(item));
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
		alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
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


