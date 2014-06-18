package team.bugbusters.acceleraudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	 private static final String DATABASE_NAME = "mydatabase.db";
     private static final int DATABASE_VERSION = 1;

     /*-- Creation SQL statement --*/
     private static final String DATABASE_CREATE = "CREATE TABLE musicsessions"+
    		 "(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,dur TEXT NOT NULL,assex TEXT NOT NULL,"+
    		 "assey TEXT NOT NULL,assez TEXT NOT NULL,checkx TEXT NOT NULL,checky TEXT NOT NULL,checkz TEXT NOT NULL,"+
    		 "numcampx INTEGER NOT NULL, numcampy INTEGER NOT NULL, numcampz INTEGER NOT NULL, upsample TEXT NOT NULL,"+
    		 "datacreaz TEXT NOT NULL, dataulti TEXT NOT NULL, datimm TEXT);";

     /*-- Constructor --*/
     public DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }

     /*-- Method which create Database --*/
     @Override
     public void onCreate(SQLiteDatabase database) {
             database.execSQL(DATABASE_CREATE);
     }

     /*-- Method which upgrade Database --*/
     @Override
     public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
              
             database.execSQL("DROP TABLE IF EXISTS musicsessions");
             onCreate(database);
              
     }

}
