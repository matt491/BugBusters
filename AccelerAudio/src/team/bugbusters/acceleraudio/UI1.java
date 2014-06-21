package team.bugbusters.acceleraudio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
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
	public static int way;
	private ListView lv;
	private DbAdapter db;
	private String pkg;
	private SharedPreferences prefs;
	private static final int BY_INSERTION = -1; //Default
	private static final int BY_NAME = 0;
	private static final int BY_DATE = 1;
	private static final int BY_DURATION = 2;
	
	/*-- Called when the activity is first created. --*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui1_layout);
		
		lv = (ListView) findViewById(R.id.listView1);		
		db = new DbAdapter(UI1.this);		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		/*-- Here it's checked which is the current ListView's sorting in order to populate it properly. --*/
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
		
		/*-- If the ListView is empty an empty View is set as a temporary layout. --*/
		lv.setEmptyView(findViewById(R.id.empty));
		
		CustomList cl = new CustomList(UI1.this, data);
		lv.setAdapter(cl);
		
		
		/*-- When an item of the list is pressed UI2 is launched. There user can visualize and/or modify details about the item pressed.  --*/
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
	
	/*-- onResume method checks if new sessions have been recorded from the widgets and updates the ListView properly.  --*/
	@Override
	public void onResume() {
		CustomList runningCl = (CustomList) lv.getAdapter();
		List<String[]> toAdd = runningCl.getList();
	    try {
			db.open();
			Cursor c = db.fetchAllRecord();
			if(toAdd.size() != c.getCount()) {
				for(c.move(toAdd.size() + 1); !c.isAfterLast(); c.moveToNext()) {
					String[] nuova = new String[5];
					nuova[0] = c.getString(c.getColumnIndex(DbAdapter.KEY_RECORDID));
					nuova[1] = c.getString(c.getColumnIndex(DbAdapter.KEY_IMM));
					nuova[2] = c.getString(c.getColumnIndexOrThrow(DbAdapter.KEY_NAME));
					nuova[3] = c.getString(c.getColumnIndex(DbAdapter.KEY_LAST));
					nuova[4] = c.getString(c.getColumnIndex(DbAdapter.KEY_DURATION));
					toAdd.add(nuova);
				}
				ordinaLista(toAdd);
				runningCl.notifyDataSetChanged();
			}
			c.close();
			db.close();
		} catch (SQLException e) {
			Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		super.onResume();
	}
	
	/*-- This method retrieves from database music session's ID, name, last-modified date, duration and thumbnail's encoding so that the ListView can be fill properly. --*/
	private List<String[]> dataToFill(int way) {	
		List<String[]> myList = new ArrayList<String[]>();
		
		try {
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
			
			
		} catch (SQLException e) {
			Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
		return myList;
	}
	
	
	/*-- Pressing the "plus button" UI3 is launched.  --*/
	public void onClick(View view) {
		
		switch(view.getId()) {
		
		case R.id.addNew:
			Intent intent = new Intent(UI1.this , UI3.class);
			startActivity(intent);
			finish();
		}
	}
	
	/*-- This method sets the contextual menu which is displayed after a long click on a ListView's item.  --*/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		String[] dati = (String[]) lv.getAdapter().getItem(info.position);
		menu.setHeaderTitle(dati[2]);
		menu.setHeaderIcon(R.drawable.ic_action_expand);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu_ui1, menu);
	}
	
	/*-- This code performs different actions depending on which contextual menu item has been selected.  --*/
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final String[] dati = (String[]) lv.getAdapter().getItem(info.position);
		final CustomList runningCl = (CustomList) lv.getAdapter();
		final List<String[]> nuovaLista = runningCl.getList();
		
		switch(item.getItemId()) {
		
		case R.id.Duplica:
			
			String[] nuoviDati = duplica(Long.parseLong(dati[0]));
			nuovaLista.add(nuoviDati);
			
			ordinaLista(nuovaLista);
			
			int pos = nuovaLista.indexOf(nuoviDati);
			
			runningCl.notifyDataSetChanged();
			lv.setSelection(pos);
			
			return(true);
			
		case R.id.Rinomina:
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.Rename);
			alert.setIcon(R.drawable.ic_action_edit);
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
						Toast toast= Toast.makeText(UI1.this, R.string.apiceNonConsentito, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else if(sameName(db, nuovoNome) || nuovoNome.equals("")) {
						Toast toast = Toast.makeText(UI1.this, R.string.ToastAlertSameName, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else {
						try {
							String nuovaData=DateFormat.format("dd-MM-yyyy kk:mm:ss", new java.util.Date()).toString();
							int dove = nuovaLista.indexOf(dati);
							nuovaLista.get(dove)[2] = nuovoNome;
							nuovaLista.get(dove)[3] = nuovaData;
							ordinaLista(nuovaLista);
							int pos = nuovaLista.indexOf(dati);
							runningCl.notifyDataSetChanged();
							lv.setSelection(pos);
							
							db.open();
							db.updateNameandDate(id_to_rename,nuovoNome,nuovaData);
							db.close();
							
							if(Long.parseLong(dati[0])==widget_big.currid) {
								Intent notifica = new Intent(UI1.this,widget_big.class);
								notifica.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
								widget_big.rename_running_record=true;
								sendBroadcast(notifica);							
							}
							
						} catch (SQLException e) {
							Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						
						dialog.dismiss();
					}
					
					else dialog.dismiss();
				}
			});
			return(true);
		
		case R.id.Elimina:
			
			AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
			alert2.setTitle(R.string.Delete);
			alert2.setIcon(R.drawable.ic_action_discard);
			alert2.setMessage(R.string.DeleteMessage);
			final long id_to_delete=Long.parseLong(dati[0]);
			
			alert2.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
					if((id_to_delete==widget_big.currid))
						if(!widget_big.pause)
							Toast.makeText(UI1.this, R.string.cannoteDelete , Toast.LENGTH_SHORT).show();
				
						else {
							nuovaLista.remove(dati);
							Intent notifica = new Intent(UI1.this,widget_big.class);
							notifica.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
							widget_big.delete_running_record=true;
							sendBroadcast(notifica);
							runningCl.notifyDataSetChanged();
						}
					
					else {
						try {
							nuovaLista.remove(dati);
							db.open();
							db.deleteRecord(id_to_delete);
							db.close();
							runningCl.notifyDataSetChanged();
						} catch (SQLException e) {
							Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
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
			if(widget_big.pause){
				stopService(new Intent(this, PlayRecord.class));
				String[] dati_sessione = (String[]) lv.getAdapter().getItem(info.position);
				Intent toUi4 = new Intent(this, UI4.class);
				pkg = getPackageName();
				toUi4.putExtra(pkg + ".myServiceID", Long.parseLong(dati_sessione[0]));
				startActivity(toUi4);
				return(true);
			}
			else Toast.makeText(UI1.this, R.string.alreadyPlaying, Toast.LENGTH_SHORT).show();
				return(true);
		}
		
		
		return (super.onOptionsItemSelected(item));
	}
	
	
	/*-- Method used to duplicate a music session --*/
	private String[] duplica(long id){
		String[] ret = new String[5];
		ret[0] = "";
		ret[1] = "";
		ret[2] = "";
		ret[3] = "";
		ret[4] = "";
		
	    try {
		   db.open();
		   Cursor c=db.fetchRecordById(id);
		   c.moveToNext();
		   String n=c.getString(c.getColumnIndex(DbAdapter.KEY_NAME));
		   String asseX=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEX));
		   String asseY=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEY));
		   String asseZ=c.getString(c.getColumnIndex(DbAdapter.KEY_ASSEZ));
		   boolean checkX=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKX)));
		   boolean checkY=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKY)));
		   boolean checkZ=Boolean.parseBoolean(c.getString(c.getColumnIndex(DbAdapter.KEY_CHECKZ)));
		   int ncampx=c.getInt(c.getColumnIndex(DbAdapter.KEY_NUMCAMPX));
		   int ncampy=c.getInt(c.getColumnIndex(DbAdapter.KEY_NUMCAMPY));
		   int ncampz=c.getInt(c.getColumnIndex(DbAdapter.KEY_NUMCAMPZ));
		   int sovrac=Integer.parseInt(c.getString(c.getColumnIndex(DbAdapter.KEY_UPSAMPLE)));
		   String datar=c.getString(c.getColumnIndex(DbAdapter.KEY_DATE));
		   String dataul=DateFormat.format("dd-MM-yyyy kk:mm:ss", new java.util.Date()).toString();
		   int sovrac_new;
		   boolean cX=checkX, cY=checkY, cZ=checkZ;
		       
		   Random r=new Random();
		   while((cX==checkX && cY==checkY && cZ==checkZ) || (!cX && !cY && !cZ)){
			   if(r.nextDouble()>=0.5) cX = !cX;
			   if(r.nextDouble()>=0.5) cY = !cY;
			   if(r.nextDouble()>=0.5) cZ = !cZ;
		   }
  
		   sovrac_new = (int)(Math.random()*100);
		   if(sovrac_new==sovrac && sovrac<=90) sovrac_new = sovrac+10;
		   else if (sovrac_new==sovrac) sovrac_new = sovrac-80;
		    
		   String dur = DataRecord.calcoloTempo(ncampx,ncampy,ncampz,cX,cY,cZ,sovrac_new);
		       
		   long id_new = db.createRecord(n+"_", dur, asseX, asseY, asseZ, ""+cX, ""+cY, ""+cZ, ncampx,ncampy,ncampz, ""+sovrac_new, datar, dataul, null);
		   String code = DataRecord.codifica(asseX, asseY, asseZ, dataul, id_new);
		   db.updateRecordNameAndImage(id_new, n+"_"+id_new, code);
			
		   c.close();
		   db.close();
		    
		   ret[0] = "" + id_new;
		   ret[1] = code;
		   ret[2] = n + "_" + id_new;
		   ret[3] = dataul;
		   ret[4] = ""+dur;
		   
		   
	} catch (NumberFormatException e) {
		Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
		e.printStackTrace();
	} catch (SQLException e) {
		Toast.makeText(UI1.this, R.string.dbError, Toast.LENGTH_SHORT).show();
		e.printStackTrace();
	}
	   return ret;
   }
	
	/*-- Method which check if another record with same name is already on Database --*/
	public static boolean sameName(DbAdapter this_db,String s){
			try {
				this_db.open();
				Cursor cursor=this_db.fetchRecordByFilter(s);
				while (cursor.moveToNext()) {
					String rNAME = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_NAME) );
					if(s.equals(rNAME)) {
						cursor.close();
						this_db.close();
						return true;
					}
				} 
				cursor.close();
				this_db.close();
			} catch (SQLException e) {
				Log.w("Exception", "Errore nel Database");
				e.printStackTrace();
			}
			return false;
	}
	
	/*-- This method sorts the ListView.  --*/
	public void ordinaLista(List<String[]> nuovaLista) {
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
						return s2[3].compareTo(s1[3]);
					}
				});
			}
			else if(prefs.getBoolean("sortedByDuration", false)) {
				Collections.sort(nuovaLista, new Comparator<String[]>() {
					@Override
					public int compare(String[] s1, String[] s2) {
						float a = Float.parseFloat(s1[4])/1000;
						float b = Float.parseFloat(s2[4])/1000;
						return Float.compare(a, b);
					}
				});
			}
	}

	/*-- This method creates and sets option's menu.  --*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.option_menu, menu);
		return true;
		}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Editor prefsEditor = prefs.edit();
		final CustomList runningCl = (CustomList) lv.getAdapter();
		final List<String[]> nuovaLista = runningCl.getList();
		switch(item.getItemId()) {
		
		case R.id.Preferenze:
			Intent prefIntentUI5 = new Intent(getApplicationContext(), UI5.class);
			prefIntentUI5.putExtra("prefFromWidget", false);
            startActivity(prefIntentUI5);
            return(true);
            
		case R.id.Ordina:
			if(runningCl.isEmpty() || runningCl.getCount() == 1) {
				Toast.makeText(UI1.this, R.string.addSessionPlease, Toast.LENGTH_LONG).show();
				break;
			}
			if(prefs.getBoolean("sortedByName", false)) {
				Toast.makeText(UI1.this, R.string.alreadySortedByName, Toast.LENGTH_SHORT).show();
				break;
			}
			prefsEditor.putBoolean("sortedByName", true).commit();
			prefsEditor.putBoolean("sortedByDate", false).commit();
			prefsEditor.putBoolean("sortedByDuration", false).commit();
			
			
			Collections.sort(nuovaLista, new Comparator<String[]>() {
				@Override
				public int compare(String[] s1, String[] s2) {
					return s1[2].compareToIgnoreCase(s2[2]);
				}
			});
			
			runningCl.notifyDataSetChanged();
			return(true);
		
		case R.id.Numera:
			if(runningCl.isEmpty() || runningCl.getCount() == 1) {
				Toast.makeText(UI1.this, R.string.addSessionPlease, Toast.LENGTH_LONG).show();
				break;
			}
			if(!prefs.getBoolean("sortedByName", false) && !prefs.getBoolean("sortedByDate", false) && !prefs.getBoolean("sortedByDuration", false)) {
				Toast.makeText(UI1.this, R.string.alreadySortedByInsertion, Toast.LENGTH_SHORT).show();
				break;
			}
			prefsEditor.putBoolean("sortedByName", false).commit();
			prefsEditor.putBoolean("sortedByDate", false).commit();
			prefsEditor.putBoolean("sortedByDuration", false).commit();
			
			Collections.sort(nuovaLista, new Comparator<String[]>() {
				@Override
				public int compare(String[] s1, String[] s2) {
					return (Integer.valueOf(s1[0])).compareTo(Integer.valueOf(s2[0]));
				}
			});
			
			runningCl.notifyDataSetChanged();
			return(true);
			
		case R.id.OrdinaData:
			if(runningCl.isEmpty() || runningCl.getCount() == 1) {
				Toast.makeText(UI1.this, R.string.addSessionPlease, Toast.LENGTH_LONG).show();
				break;
			}
			if(prefs.getBoolean("sortedByDate", false)) {
				Toast.makeText(UI1.this, R.string.alreadySortedByDate, Toast.LENGTH_SHORT).show();
				break;
			}
			prefsEditor.putBoolean("sortedByDate", true).commit();
			prefsEditor.putBoolean("sortedByName", false).commit();
			prefsEditor.putBoolean("sortedByDuration", false).commit();
			
			Collections.sort(nuovaLista, new Comparator<String[]>() {
				@Override
				public int compare(String[] s1, String[] s2) {
					return s2[3].compareTo(s1[3]);
				}
			});
			
			runningCl.notifyDataSetChanged();
			return(true);
			
		case R.id.OrdinaDurata:
			if(runningCl.isEmpty() || runningCl.getCount() == 1) {
				Toast.makeText(UI1.this, R.string.addSessionPlease, Toast.LENGTH_LONG).show();
				break;
			}
			if(prefs.getBoolean("sortedByDuration", false)){
				Toast.makeText(UI1.this, R.string.alreadySortedByDuration, Toast.LENGTH_SHORT).show();
				break;
			}
			prefsEditor.putBoolean("sortedByDuration", true).commit();
			prefsEditor.putBoolean("sortedByName", false).commit();
			prefsEditor.putBoolean("sortedByDate", false).commit();
			
			Collections.sort(nuovaLista, new Comparator<String[]>() {
				@Override
				public int compare(String[] s1, String[] s2) {
					float a = Float.parseFloat(s1[4])/1000;
					float b = Float.parseFloat(s2[4])/1000;
					return Float.compare(a, b);
				}
			});
			
			runningCl.notifyDataSetChanged();
			return(true);
		}
		
		return (super.onOptionsItemSelected(item));
	}	
	
		
	/*-- This method warns the user that the application is going to close itself.  --*/
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UI1.this);
		
		
		alertDialogBuilder.setTitle(R.string.alertTitle);
		alertDialogBuilder.setIcon(R.drawable.ic_action_warning);
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