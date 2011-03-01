package se.mog.tfl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDb extends SQLiteOpenHelper {
	private static final String NAME = "history";
	private static final int VERSION = 1;

	public HistoryDb(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history (" +
        		   " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
        		   " name TEXT," +
        		   " type TEXT," +
        		   " last_from INTEGER," +
        		   " last_to   INTEGER," +
        		   " UNIQUE (name)" +
        		   ")");
	}

	@Override public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		throw new RuntimeException();
	}

	public Cursor getFrom() {
		String sql = "SELECT * FROM history ORDER BY last_from DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}
	public Cursor getTo() {
		String sql = "SELECT * FROM history ORDER BY last_to DESC";
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(sql, null);
	}

	public void touch(CharSequence from, CharSequence fromType, CharSequence to, CharSequence toType) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL("INSERT INTO history (name, type, last_from) VALUES(?, ?, DATETIME('now'))", new Object[]{from, fromType});
		} catch(SQLiteConstraintException e) {
			db.execSQL("UPDATE history SET last_from=DATETIME('now') WHERE name=? AND type=?", new Object[]{from, fromType});
		}
		try {
			db.execSQL("INSERT INTO history (name, type, last_to) VALUES(?, ?, DATETIME('now'))", new Object[]{to, toType});
		} catch(SQLiteConstraintException e) {
			db.execSQL("UPDATE history SET last_to=DATETIME('now') WHERE name=? AND type=?", new Object[]{to, toType});
		}
	}
}
