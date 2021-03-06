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
	private static final String TAG = Main.TAG;

	public HistoryDb(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE history (" +
				     " `_id`        INTEGER PRIMARY KEY AUTOINCREMENT," +
				     " `from`       TEXT," +
	        		 " `from_type`  TEXT," +
				     " `from_hide`  BOOLEAN NOT NULL DEFAULT 0," +
				     " `to`         TEXT," +
        		     " `to_type`    TEXT," +
				     " `to_hide`    BOOLEAN NOT NULL DEFAULT 0," +
				     " `created_at` INTEGER," +
				     " `updated_at` INTEGER" +
				     ")";
		//Log.d(TAG, sql);
        db.execSQL(sql);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException();
	}

	public Cursor getCursor() {
		String sql = "SELECT `_id`, `from`, `from_type`, `to`, `to_type`, `from` || ' - ' || `to` AS trip FROM history WHERE `from_hide` = 0 OR `to_hide` = 0 GROUP BY `from`, `from_type`, `to`, `to_type` ORDER BY MAX(updated_at) DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}
	public Cursor getFrom() {
		String sql = "SELECT `_id`, `from`, `from_type` FROM history WHERE `from_hide` = 0 GROUP BY `from`, `from_type` ORDER BY MAX(updated_at) DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}
	public Cursor getTo() {
		String sql = "SELECT `_id`, `to`, `to_type` FROM history WHERE `to_hide` = 0 GROUP BY `to`, `to_type` ORDER BY MAX(updated_at) DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}

	public void touch(CharSequence from, CharSequence from_type, CharSequence to, CharSequence to_type) {
		Object[] where = new Object[]{from, from_type, to, to_type};
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL("INSERT INTO history (`from`, `from_type`, `to`, `to_type`, `created_at`, `updated_at`) VALUES(?, ?, ?, ?, DATETIME('now'), DATETIME('now'))", where);
		} catch(SQLiteConstraintException e) {
			db.execSQL("UPDATE history SET updated_at = DATETIME('now') WHERE `from` = ? AND `from_type` = ? AND `to = ? AND `to_type` = ?", where);
		}
	}

	public void clear(CharSequence from, CharSequence from_type, CharSequence to, CharSequence to_type) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM history WHERE `from` = ? AND `from_type` = ? AND `to` = ? AND `to_type` = ?",
				   new Object[]{from, from_type, to, to_type});
		//gc(db);
	}
	public void clearFrom(CharSequence from, CharSequence from_type) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE history SET `from_hide` = 1 WHERE `from` = ? AND `from_type`=?",
				   new Object[]{from, from_type});
		gc(db);
	}
	public void clearTo(CharSequence to, CharSequence to_type) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE history SET `to_hide` = 1 WHERE `to` = ? AND `to_type` = ?", new Object[]{to, to_type});
		gc(db);
	}
	public void gc(SQLiteDatabase db) {
		db.execSQL("DELETE FROM history WHERE `from_hide` = 1 AND `to` = 1");
	}
}
