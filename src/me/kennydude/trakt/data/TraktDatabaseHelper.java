package me.kennydude.trakt.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TraktDatabaseHelper extends SQLiteOpenHelper {
	public static final int DB_VERSION = 11;
	
	public static final String CREATE_ITEM_TABLE = "CREATE TABLE `items` (" + 
			"  `id` text NOT NULL," +
			"  `url` text NOT NULL," +
			"  `type` varchar(1) NOT NULL," +
			"  `json` text NOT NULL," +
			"  `time` integer NOT NULL" +
			")";
	
	public TraktDatabaseHelper(Context context, String dbName, CursorFactory object,
			int i) {
		super(context, dbName, object, i);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ITEM_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DELETE FROM `items`");
		db.execSQL("DROP TABLE `items`");
		onCreate(db);
	}

}
