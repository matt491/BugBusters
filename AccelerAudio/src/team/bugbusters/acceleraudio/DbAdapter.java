package team.bugbusters.acceleraudio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

 
public class DbAdapter {
  @SuppressWarnings("unused")
  private static final String LOG_TAG = DbAdapter.class.getSimpleName();
         
  private Context context;
  private SQLiteDatabase database;
  private DatabaseHelper dbHelper;
 
  // Database fields
  private static final String DATABASE_TABLE = "musicsessions";
 
  public static final String KEY_RECORDID = "_id";
  public static final String KEY_NAME = "name";
  public static final String KEY_DURATION = "dur";
  public static final String KEY_ASSEX = "assex";
  public static final String KEY_ASSEY = "assey";
  public static final String KEY_ASSEZ = "assez";
  public static final String KEY_CHECKX = "checkx";
  public static final String KEY_CHECKY = "checky";
  public static final String KEY_CHECKZ = "checkz";
  public static final String KEY_NUMCAMP = "numcamp";
  public static final String KEY_UPSAMPLE = "upsample";
  public static final String KEY_DATE = "datacreaz";
  public static final String KEY_LAST = "dataulti";
  public static final String KEY_IMM = "datimm";
  
  public DbAdapter(Context context) {
    this.context = context;
  }
 
  public DbAdapter open() throws SQLException {
    dbHelper = new DatabaseHelper(context);
    database = dbHelper.getWritableDatabase();
    return this;
  }
 
  public void close() {
    dbHelper.close();
  }
 
  private ContentValues createContentValues(String name, String dur, String assex, String assey, String assez, String checkx,
		  String checky , String checkz, int numcamp, String upsample, String datacreaz, String dataulti, String datimm ) {
    ContentValues values = new ContentValues();
    values.put( KEY_NAME, name );
    values.put( KEY_DURATION, dur );
    values.put( KEY_ASSEX, assex );
    values.put( KEY_ASSEY, assey );
    values.put( KEY_ASSEZ, assez );
    values.put( KEY_CHECKX, checkx );
    values.put( KEY_CHECKY, checky );
    values.put( KEY_CHECKZ, checkz );
    values.put( KEY_NUMCAMP, numcamp );
    values.put( KEY_UPSAMPLE, upsample );
    values.put( KEY_DATE, datacreaz );
    values.put( KEY_LAST, dataulti );
    values.put( KEY_IMM, datimm );
    
   return values;
  }
  
  private ContentValues updateContentValues(String name,String checkx, String checky , String checkz, String upsample, String dataulti ) {
    ContentValues values = new ContentValues();
    values.put( KEY_NAME, name );
    values.put( KEY_CHECKX, checkx );
    values.put( KEY_CHECKY, checky );
    values.put( KEY_CHECKZ, checkz );
    values.put( KEY_UPSAMPLE, upsample );
    values.put( KEY_LAST, dataulti );
    
   return values;
  }
      
  private ContentValues updateContentValuesNameImage(String name, String imm) {
	    ContentValues values = new ContentValues();
	    values.put( KEY_NAME, name );
	    values.put( KEY_IMM, imm );    
	   return values;
	  }
  
  private ContentValues updateContentValuesImageCodeOnly(String name) {
	    ContentValues values = new ContentValues();
	    values.put( KEY_IMM, name );
	    	    
	   return values;
	  }
  
  //crea una music session
  public long createRecord(String name, String dur, String assex, String assey, String assez, String checkx,
		  				   String checky , String checkz, int numcamp, String upsample, String datacreaz, String dataulti, String datimm ) {
	  		ContentValues initialValues = createContentValues(name, dur, assex, assey, assez, checkx, checky , checkz, numcamp,
	  										upsample, datacreaz, dataulti, datimm );
	  		return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
  }
 
  //aggiorna un record
  public boolean updateRecord(long recordID, String name, String checkx, String checky , String checkz, String upsample, String dataulti ) {
	  			ContentValues updateValues = updateContentValues(name, checkx, checky , checkz, upsample, dataulti);
	  			return database.update(DATABASE_TABLE, updateValues, KEY_RECORDID + "==" + recordID, null) > 0;
  }
                 
  //Elimina un record    
  public boolean deleteRecord(long recordID) {
	  			return database.delete(DATABASE_TABLE, KEY_RECORDID + "==" + recordID, null) > 0;
  }
 
  //Query che restituisce tutti i record
  public Cursor fetchAllRecord() {
	  			return database.query(DATABASE_TABLE, new String[] { KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    			KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMP,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM}, null, null, null, null, null);
    }
 
  
  //Query che restituisce un record(cursor) dato un NOME
  public Cursor fetchRecordByFilter(String filter) {
	  	Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    		KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMP,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM },
                                    KEY_NAME + " like '%"+ filter + "%'", null, null, null, null, null);
	  	return mCursor;
    
  }
     
  //Query che restituisce un record(cursor) dato un ID
   public Cursor fetchRecordById(long id) {
        Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
        		KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMP,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM },
        							KEY_RECORDID + " == "+ id , null, null, null, null, null);
    
        return mCursor;
  }
   
   public boolean updateRecordNameAndImage(long recordID, String name, String imm) {
			ContentValues updateValuesNameAndImage = updateContentValuesNameImage(name, imm);
			return database.update(DATABASE_TABLE, updateValuesNameAndImage, KEY_RECORDID + "==" + recordID, null) > 0;
}
   
   
   public boolean updateImageCode(long recordID, String name) {
			ContentValues updateValuesImageCode = updateContentValuesImageCodeOnly(name);
			return database.update(DATABASE_TABLE, updateValuesImageCode, KEY_RECORDID + "==" + recordID, null) > 0;
}
   
   //Qua ci va il metodo di Mattia
  
  
}