package se.mog.tfl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HistoryDb extends SQLiteOpenHelper {
	private static final String NAME = "history";
	private static final int VERSION = 1;
	private static final String TAG = Planner.TAG;

	public HistoryDb(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE history (" +
				     " `_id`        INTEGER PRIMARY KEY AUTOINCREMENT," +
				     " `from`       TEXT," +
	        		 " `from_type`  TEXT," +
				     " `to`         TEXT," +
        		     " `to_type`    TEXT," +
				     " `created_at` INTEGER," +
				     " `updated_at` INTEGER" +
				     ")";
		Log.d(TAG, sql);
        db.execSQL(sql);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		throw new RuntimeException();
	}

	public Cursor getFrom() {
		String sql = "SELECT `_id`,`from`,`from_type` FROM history WHERE `from` IS NOT NULL GROUP BY `from` ORDER BY MAX(updated_at) DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}
	public Cursor getTo() {
		String sql = "SELECT `_id`,`to`,`to_type` FROM history WHERE `to` IS NOT NULL GROUP BY `to` ORDER BY MAX(updated_at) DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}

	public void touch(CharSequence from, CharSequence from_type, CharSequence to, CharSequence to_type) {
		Object[] where = new Object[]{from, from_type, to, to_type};
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL("INSERT INTO history (`from`, `from_type`, `to`, `to_type`, `created_at`, `updated_at`) VALUES(?, ?, ?, ?, DATETIME('now'), DATETIME('now'))", where);
		} catch(SQLiteConstraintException e) {
			db.execSQL("UPDATE history SET updated_at=DATETIME('now') WHERE `from`=? AND `to=?,", where);
		}
	}

	public void clearFrom(CharSequence from) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE history SET `from`=NULL WHERE `from`=?", new Object[]{from});
		gc(db);
	}

	public void clearTo(CharSequence to) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE history SET `to`=NULL WHERE `to`=?", new Object[]{to});
		gc(db);
	}
	public void gc(SQLiteDatabase db) {
		db.execSQL("DELETE FROM history WHERE `from`=NULL AND `to`=NULL");
	}
}
