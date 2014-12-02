package edu.barella4730.transmitlater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_MONTH = "month";
	public static final String KEY_DAY = "day";
	public static final String KEY_YEAR = "year";
	public static final String KEY_TIME = "time";
	public static final String KEY_RECIPIENT = "recipient";
	public static final String KEY_MESSAGE = "message";
	private static final String TAG = "DBAdapter";
	
	public static final String DATABASE_NAME = "messages";
	public static final String DATABASE_TABLE = "queuedMsg";
	public static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE =
	        "create table queuedMsg (_id integer primary key autoincrement, "
	        + "month int not null, day int not null, " 
	        + "year int not null, recipient text not null, "
	        + "time text not null, message text not null);";
	
	private final Context context;
	
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + "to " + newVersion + ", which will destroy all previous data");
			db.execSQL("DROP TABLE IF EXISTS queuedMsg");
			onCreate(db);
		}			
	}
	
	public DBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		DBHelper.close();
	}
	
	public long insertMsg(String month, String day, String year, String time, String recipient, String message)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_MONTH, month);
		initialValues.put(KEY_DAY, day);
		initialValues.put(KEY_YEAR, year);
		initialValues.put(KEY_TIME, time);
		initialValues.put(KEY_RECIPIENT, recipient);
		initialValues.put(KEY_MESSAGE, message);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	public boolean deleteMsg(long rowId)
	{
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor getAllMsg()
	{
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DAY, KEY_YEAR, KEY_TIME, KEY_RECIPIENT, KEY_MESSAGE}, null, null, null, null, null, null);
	}
	
	public Cursor getMsg(long rowId) throws SQLException
	{
		Cursor mCursor = db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DAY, KEY_YEAR, KEY_TIME, KEY_RECIPIENT, KEY_MESSAGE}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		
		if (mCursor != null){
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public long updateMsg(long rowId, String month, String day, String year, String time, String recipient, String message)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_MONTH, month);
		args.put(KEY_DAY, day);
		args.put(KEY_YEAR, year);
		args.put(KEY_TIME, time);
		args.put(KEY_RECIPIENT, recipient);
		args.put(KEY_MESSAGE, message);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
	}
}