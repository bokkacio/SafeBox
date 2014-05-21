package com.Alexander.SafeBox.DbProcessing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EntryDatabaseHelper {

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private final Context mCtx;

    private static final int ATTEMPTS_TO_ENTER = 10;
    private static final String DB_NAME = "dbSafeEntry";
    private static final int DB_VERSION = 1;

    private static final String ENTRANCE_TABLE = "entrance";
    public static final String ENTRANCE_COLUMN_ID = "_id";
    public static final String MD5_COLUMN_NAME = "secret_phrase";
    public static final String ATTEMPT_COLUMN_NAME = "attempt_amount";
    private static final String ENTRANCE_TABLE_CREATE = "create table "
            + ENTRANCE_TABLE + "(" + ENTRANCE_COLUMN_ID
            + " integer primary key autoincrement, " + MD5_COLUMN_NAME + " blob, " + ATTEMPT_COLUMN_NAME + " integer" +  ");";

    public EntryDatabaseHelper(Context ctx) {
        mCtx = ctx;
    }

    // открываем подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрываем подключение
    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }

    public boolean IsEntryFirstTime()
    {
        Cursor cr = mDB.query(ENTRANCE_TABLE, null, null, null, null, null, null);
        return !cr.moveToFirst();
    }

    public byte[] GetEncryptedPassword()
    {
        Cursor cr = mDB.query(ENTRANCE_TABLE, null, null, null, null, null, null);
        if(cr.moveToFirst())
            return cr.getBlob(cr.getColumnIndex(MD5_COLUMN_NAME));
        else
            return null;
    }

    public int GetAttemptsAmount()
    {
        Cursor cr = mDB.query(ENTRANCE_TABLE, null, null, null, null, null, null);
        if(cr.moveToFirst())
            return cr.getInt(cr.getColumnIndex(ATTEMPT_COLUMN_NAME));
        else
            return 0;
    }

    public void UpdateAttemptsAmount(int attemptsLeft)
    {
        ContentValues cv = new ContentValues();
        cv.put(ATTEMPT_COLUMN_NAME, attemptsLeft);
        mDB.update(ENTRANCE_TABLE, cv, null, null);
    }

    public void FreeMd5Password(byte[] funnyPassword)
    {
        ContentValues cv = new ContentValues();
        cv.put(ATTEMPT_COLUMN_NAME, 100000);
        cv.put(MD5_COLUMN_NAME, funnyPassword);
        mDB.update(ENTRANCE_TABLE, cv, null, null);
    }

    public void RestoreAttemptsAmount()
    {
        ContentValues cv = new ContentValues();
        cv.put(ATTEMPT_COLUMN_NAME, ATTEMPTS_TO_ENTER);
        mDB.update(ENTRANCE_TABLE, cv, null, null);

    }

    public void SetPasswordFirstTime(byte[] password)
    {
        ContentValues values = new ContentValues();

        values.put(MD5_COLUMN_NAME, password);
        values.put(ATTEMPT_COLUMN_NAME, ATTEMPTS_TO_ENTER);
        mDB.insert(ENTRANCE_TABLE, null, values);
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(ENTRANCE_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
