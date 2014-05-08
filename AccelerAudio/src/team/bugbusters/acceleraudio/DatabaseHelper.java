package team.bugbusters.acceleraudio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	 private static final String DATABASE_NAME = "mydatabase.db";
     private static final int DATABASE_VERSION = 1;

     // Lo statement SQL di creazione del database
     private static final String DATABASE_CREATE = "CREATE TABLE musicsessions"+
    		 "(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,dur TEXT NOT NULL,assex TEXT NOT NULL,"+
    		 "assey TEXT NOT NULL,assez TEXT NOT NULL,checkx TEXT NOT NULL,checky TEXT NOT NULL,checkz TEXT NOT NULL,"+
    		 "numcamp INTEGER NOT NULL, upsample TEXT NOT NULL, datacreaz TEXT NOT NULL, dataulti TEXT NOT NULL, datimm TEXT NOT NULL);";

     // Costruttore
     public DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }

     // Questo metodo viene chiamato durante la creazione del database
     @Override
     public void onCreate(SQLiteDatabase database) {
             database.execSQL(DATABASE_CREATE);
     }

     // Questo metodo viene chiamato durante l'upgrade del database, ad esempio quando viene incrementato il numero di versione
     @Override
     public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
              
             database.execSQL("DROP TABLE IF EXISTS musicsessions");
             onCreate(database);
              
     }

}
