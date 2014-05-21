package com.Alexander.SafeBox.DbProcessing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.Alexander.SafeBox.Encryption.Scrambler;


public class DatabaseHelper {

    private static final String DB_NAME = "dbSafe";
    private static final int DB_VERSION = 1;
    private Scrambler _localScrambler = null;

    //Groups
    private static final String GROUP_TABLE = "groups";
    public static final String GROUP_COLUMN_ID = "_id";
    public static final String GROUP_COLUMN_NAME = "group_title";
    private static final String GROUP_TABLE_CREATE = "create table "
            + GROUP_TABLE + "(" + GROUP_COLUMN_ID
            + " integer primary key autoincrement, " + GROUP_COLUMN_NAME + " text" + ");";

    //Elements
    private static final String ELEMENT_TABLE = "elements";
    public static final String ELEMENT_COLUMN_ID = "_id";
    public static final String ELEMENT_COLUMN_VALUE= "element_value";
    public static final String ELEMENT_COLUMN_GROUP = "group_id";
    private static final String ELEMENT_TABLE_CREATE = "create table "
            + ELEMENT_TABLE + " ( " + ELEMENT_COLUMN_ID
            + " integer primary key autoincrement, " + ELEMENT_COLUMN_VALUE +  " text, " + ELEMENT_COLUMN_GROUP + " integer" + ");";

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DatabaseHelper(Context ctx) {
        mCtx = ctx;
    }

    // открываем подключение
    public void open(String userPassword) {
        _localScrambler = new Scrambler(userPassword);
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрываем подключение
    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }

    public Cursor GetGroupData() {
        return mDB.query(GROUP_TABLE, null, null, null, null, null, null);
    }

    public Cursor GetElementData(long groupID) {
        return mDB.query(ELEMENT_TABLE, null, ELEMENT_COLUMN_GROUP + " = "
                + groupID, null, null, null, null);
    }

    public void InsertElement(Long elementGroupId, String elementTitle, String elementValue)
    {
        ContentValues cv = new ContentValues();
        cv.put(ELEMENT_COLUMN_GROUP, elementGroupId);
        String encryptedValue = _localScrambler.Encrypt(elementTitle + "  :  " + elementValue);
        cv.put(ELEMENT_COLUMN_VALUE, encryptedValue);
        mDB.insert(ELEMENT_TABLE, null, cv);
    }

    public void InsertGroup(String groupValue)
    {
        ContentValues cv = new ContentValues();
        String encryptedValue = _localScrambler.Encrypt(groupValue);
        cv.put(GROUP_COLUMN_NAME, encryptedValue);
        mDB.insert(GROUP_TABLE, null, cv);
    }

    public long InsertGroupResult(String groupValue)
    {
        ContentValues cv = new ContentValues();
        String encryptedValue = _localScrambler.Encrypt(groupValue);
        cv.put(GROUP_COLUMN_NAME, encryptedValue);
        return mDB.insert(GROUP_TABLE, null, cv);
    }

    public void DeleteGroup(Long groupId)
    {
        mDB.delete(ELEMENT_TABLE, ELEMENT_COLUMN_GROUP + " = " + groupId, null);
        mDB.delete(GROUP_TABLE, GROUP_COLUMN_ID + " = " + groupId, null);
    }

    public void DeleteElement(Long elementId)
    {
        mDB.delete(ELEMENT_TABLE, ELEMENT_COLUMN_ID + " = " + elementId, null);
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(GROUP_TABLE_CREATE);
            db.execSQL(ELEMENT_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}
