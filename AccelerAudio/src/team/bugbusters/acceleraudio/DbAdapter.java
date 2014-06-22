package team.bugbusters.acceleraudio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

 
public class DbAdapter {
        
  private Context context;
  private SQLiteDatabase database;
  private DatabaseHelper dbHelper;
 
  /*-- Database fields --*/
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
  public static final String KEY_NUMCAMPX = "numcampx";
  public static final String KEY_NUMCAMPY = "numcampy";
  public static final String KEY_NUMCAMPZ = "numcampz";
  public static final String KEY_UPSAMPLE = "upsample";
  public static final String KEY_DATE = "datacreaz";
  public static final String KEY_LAST = "dataulti";
  public static final String KEY_IMM = "datimm";
  
  
  public DbAdapter(Context context) {
	  this.context = context;
  }
 
  /*-- Method which is used to open the Database (needed before insert/modify an entry) --*/
  public DbAdapter open() throws SQLException {
	  dbHelper = new DatabaseHelper(context);
	  database = dbHelper.getWritableDatabase();
	  return this;
  }
 
  /*-- Method which is used to open the Database --*/
  public void close() {
	  dbHelper.close();
  }
 

  private ContentValues createContentValues(String name, String dur, String assex, String assey, String assez, String checkx,
	  String checky , String checkz, int numcampx, int numcampy, int numcampz, String upsample, String datacreaz, String dataulti, String datimm) {
	  ContentValues values = new ContentValues();
	  values.put( KEY_NAME, name );
	  values.put( KEY_DURATION, dur );
	  values.put( KEY_ASSEX, assex );
	  values.put( KEY_ASSEY, assey );
	  values.put( KEY_ASSEZ, assez );
	  values.put( KEY_CHECKX, checkx );
	  values.put( KEY_CHECKY, checky );
	  values.put( KEY_CHECKZ, checkz );
	  values.put( KEY_NUMCAMPX, numcampx );
	  values.put( KEY_NUMCAMPY, numcampy );
	  values.put( KEY_NUMCAMPZ, numcampz );
	  values.put( KEY_UPSAMPLE, upsample );
	  values.put( KEY_DATE, datacreaz );
	  values.put( KEY_LAST, dataulti );
	  values.put( KEY_IMM, datimm );
	  return values;
  }
  
 
  private ContentValues updateContentValues(String name,String dur,String checkx, String checky , String checkz, String upsample, String dataulti) {
	  ContentValues values = new ContentValues();
	  values.put( KEY_NAME, name );
	  values.put( KEY_DURATION, dur );
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
  
  
  /*-- Method which is used to update an existing entry
   *-- Updates: image
  --*/
  private ContentValues updateContentValuesImageCodeOnly(String imm) {
	  ContentValues values = new ContentValues();
	  values.put( KEY_IMM, imm ); 	    
	  return values;
  }
  
  
  /*-- Method which is used to update an existing entry
   *-- Updates: name
  --*/
  private ContentValues updateContentValuesNameandDate(String name, String dataulti) {
	  ContentValues values = new ContentValues();  
	  values.put( KEY_NAME, name );  
	  values.put( KEY_LAST, dataulti );
	  return values;
  }
  
  /*-- Method which is used to create and insert a new entry into Database --*/
  public long createRecord(String name, String dur, String assex, String assey, String assez, String checkx,
		  				   String checky , String checkz, int numcampx, int numcampy, int numcampz, String upsample, String datacreaz, String dataulti, String datimm ) {
	  ContentValues initialValues = createContentValues(name, dur, assex, assey, assez, checkx, checky , checkz, numcampx, numcampy, numcampz,
	  										upsample, datacreaz, dataulti, datimm);
	  return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
  }
 
  /*-- Method which is used to update an existing entry
   *-- Updates: name, duration, axes checkboxes, upsampling, last modified date
  --*/
  public boolean updateRecord(long recordID, String name, String dur, String checkx, String checky , String checkz, String upsample, String dataulti ) {
	  ContentValues updateValues = updateContentValues(name, dur, checkx, checky , checkz, upsample, dataulti);
	  return database.update(DATABASE_TABLE, updateValues, KEY_RECORDID + "==" + recordID, null) > 0;
  }
                 
  /*-- Method which is used to delete an entry --*/   
  public boolean deleteRecord(long recordID) {
	  return database.delete(DATABASE_TABLE, KEY_RECORDID + "==" + recordID, null) > 0;
  }
 
  /*-- Method which query Database and return all entries (ordered by insertion) --*/
  public Cursor fetchAllRecord() {
	  return database.query(DATABASE_TABLE, new String[] { KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    			KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM}, null, null, null, null, null);
    }
  
  /*-- Method which query Database and return all entries ordered by name --*/
  public Cursor fetchAllRecordSortedByName() {
	  return database.query(DATABASE_TABLE, new String[] { KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    			KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM}, null, null, null, null, KEY_NAME + " ASC");
  }
 
  /*-- Method which query Database and return all entries ordered by date --*/
  public Cursor fetchAllRecordSortedByDate() {
	  return database.query(DATABASE_TABLE, new String[] { KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    			KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM}, null, null, null, null, KEY_LAST + " DESC");
  }
  
  /*-- Method which query Database and return all entries ordered by duration --*/
  public Cursor fetchAllRecordSortedByDuration() {
	  return database.query(DATABASE_TABLE, new String[] { KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    			KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM}, null, null, null, null, KEY_DURATION + " ASC");
  }
  
  /*-- Method which query Database and return the entry that has the given name --*/
  public Cursor fetchRecordByFilter(String filter) {
	  return database.query(true, DATABASE_TABLE, new String[] {KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
    		KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM },
                                    KEY_NAME + " = '"+ filter + "'", null, null, null, null, null);
  }
     
  /*-- Method which query Database and return the entry which the given ID corresponding to --*/
   public Cursor fetchRecordById(long id) {
	   return database.query(true, DATABASE_TABLE, new String[] {KEY_RECORDID, KEY_NAME, KEY_DURATION, KEY_ASSEX,KEY_ASSEY,KEY_ASSEZ,
        		KEY_CHECKX,KEY_CHECKY,KEY_CHECKZ,KEY_NUMCAMPX,KEY_NUMCAMPY,KEY_NUMCAMPZ,KEY_UPSAMPLE,KEY_DATE,KEY_LAST,KEY_IMM },
        							KEY_RECORDID + " == "+ id , null, null, null, null, null);
  }
   
   
   /*-- Method which is used to update an existing entry
    *-- Updates: name, image
   --*/
   public boolean updateRecordNameAndImage(long recordID, String name, String imm) {
			ContentValues update = updateContentValuesNameImage(name, imm);
			return database.update(DATABASE_TABLE, update, KEY_RECORDID + "==" + recordID, null) > 0;
}
   
   /*-- Method which is used to update an existing entry
    *-- Updates: image
   --*/
   public boolean updateImageCode(long recordID, String name) {
			ContentValues updateValuesImageCode = updateContentValuesImageCodeOnly(name);
			return database.update(DATABASE_TABLE, updateValuesImageCode, KEY_RECORDID + "==" + recordID, null) > 0;
}
   
   /*-- Method which is used to update an existing entry
    *-- Updates: name, last modified date
   --*/
   public boolean updateNameandDate(long recordID, String name, String dataulti) {
			ContentValues updateNameandDate = updateContentValuesNameandDate(name, dataulti);
			return database.update(DATABASE_TABLE, updateNameandDate, KEY_RECORDID + "==" + recordID, null) > 0;
}

  
  
}