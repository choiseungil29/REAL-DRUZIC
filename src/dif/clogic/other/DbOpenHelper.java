package dif.clogic.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: choeseung-il
 * Date: 13. 9. 25.
 * Time: 오전 4:41
 * To change this template use File | Settings | File Templates.
 */

public class DbOpenHelper {

    private static final String DATABASE_NAME = "songdata.db";
    private static final int DATABASE_VERSION = 1;
    public SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //To change body of implemented methods use File | Settings | File Templates.
            sqLiteDatabase.execSQL(AccompanimentData.CreateDB._CREATE);
            sqLiteDatabase.execSQL(MelodyData.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            //To change body of implemented methods use File | Settings | File Templates.
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ AccompanimentData.CreateDB._TABLENAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ MelodyData.CreateDB._TABLENAME);
            onCreate(sqLiteDatabase);
        }
    }

    public DbOpenHelper(Context context) {
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if(mDB != null) {
            mDB.close();
            mDB = null;
        }
    }

    public long insertAccompanimentColumn(String name, String melodysequence) {
        ContentValues values = new ContentValues();
        values.put(AccompanimentData.CreateDB.NAME, name);
        values.put(AccompanimentData.CreateDB.ORIGINRECORD, melodysequence);
        return mDB.insert(AccompanimentData.CreateDB._TABLENAME, null, values);
    }

    public long insertMelodyColumn(String name, String melodysequence, String accompaniment) {
        ContentValues values = new ContentValues();
        values.put(MelodyData.CreateDB.NAME, name);
        values.put(MelodyData.CreateDB.ORIGINRECORD, melodysequence);
        values.put(MelodyData.CreateDB.ACCOMPANIMENTLIST, accompaniment);
        return mDB.insert(MelodyData.CreateDB._TABLENAME, null, values);
    }

    // Update DB
    public boolean updateAccompanimentColumn(long id , String name, String melodysequence){
        ContentValues values = new ContentValues();
        values.put(AccompanimentData.CreateDB.NAME, name);
        values.put(AccompanimentData.CreateDB.ORIGINRECORD, melodysequence);
        return mDB.update(AccompanimentData.CreateDB._TABLENAME, values, "_id=" + id, null) > 0;
    }

    public boolean updateMelodyColumn(long id, String name, String melodysequence, String accompaniment) {
        ContentValues values = new ContentValues();
        values.put(MelodyData.CreateDB.NAME, name);
        values.put(MelodyData.CreateDB.ORIGINRECORD, melodysequence);
        values.put(MelodyData.CreateDB.ACCOMPANIMENTLIST, accompaniment);
        return mDB.update(MelodyData.CreateDB._TABLENAME, values, "_id=" + id, null) > 0;
    }

    // Delete ID
    public boolean deleteAccompanimentColumn(long id){
        return mDB.delete(AccompanimentData.CreateDB._TABLENAME, "_id="+id, null) > 0;
    }

    public boolean deleteMelodyColumn(long id) {
        return mDB.delete(MelodyData.CreateDB._TABLENAME, "_id="+id, null) > 0;
    }

    public boolean deleteAccompanimentAll() {
        return mDB.delete(AccompanimentData.CreateDB._TABLENAME, null, null) > 0;
    }

    public void deleteTable() {
        String sql = "drop table " + AccompanimentData.CreateDB._TABLENAME;
        mDB.execSQL(sql);
    }

    public void createTable() {
        mDB.execSQL(AccompanimentData.CreateDB._CREATE);
        mDB.execSQL(MelodyData.CreateDB._CREATE);
    }

    // Select All
    public Cursor getAllColumns(){
        return mDB.query(AccompanimentData.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    // ID 컬럼 얻어 오기
    public Cursor getColumn(long id){
        Cursor c = mDB.query(AccompanimentData.CreateDB._TABLENAME, null,
                "_id=" + id, null, null, null, null);
        if(c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }
}