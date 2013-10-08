package dif.clogic.druzic;

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
            sqLiteDatabase.execSQL(SongDatum.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            //To change body of implemented methods use File | Settings | File Templates.
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ SongDatum.CreateDB._TABLENAME);
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

    public long insertColumn(String name, String melodysequence, boolean ismelody){
        ContentValues values = new ContentValues();
        values.put(SongDatum.CreateDB.NAME, name);
        values.put(SongDatum.CreateDB.ORIGINRECORD, melodysequence);
        values.put(SongDatum.CreateDB.ISMELODY, ismelody?1:0);
        return mDB.insert(SongDatum.CreateDB._TABLENAME, null, values);
    }

    // Update DB
    public boolean updateColumn(long id , String name, String melodysequence, boolean ismelody){
        ContentValues values = new ContentValues();
        values.put(SongDatum.CreateDB.NAME, name);
        values.put(SongDatum.CreateDB.ORIGINRECORD, melodysequence);
        values.put(SongDatum.CreateDB.ISMELODY, ismelody?1:0);
        return mDB.update(SongDatum.CreateDB._TABLENAME, values, "_id="+id, null) > 0;
    }

    // Delete ID
    public boolean deleteColumn(long id){
        return mDB.delete(SongDatum.CreateDB._TABLENAME, "_id="+id, null) > 0;
    }

    // Delete Contact
    public boolean deleteColumn(String number){
        return mDB.delete(SongDatum.CreateDB._TABLENAME, "contact="+number, null) > 0;
    }

    public boolean deleteAll() {
        return mDB.delete(SongDatum.CreateDB._TABLENAME, null, null) > 0;
    }

    public void deleteTable() {
        String sql = "drop table " + SongDatum.CreateDB._TABLENAME;
        mDB.execSQL(sql);
    }

    public void createTable() {
        mDB.execSQL(SongDatum.CreateDB._CREATE);
    }

    // Select All
    public Cursor getAllColumns(){
        return mDB.query(SongDatum.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    // ID 컬럼 얻어 오기
    public Cursor getColumn(long id){
        Cursor c = mDB.query(SongDatum.CreateDB._TABLENAME, null,
                "_id="+id, null, null, null, null);
        if(c != null && c.getCount() != 0)
            c.moveToFirst();
        return c;
    }

    // 이름 검색 하기 (rawQuery)
    public Cursor getMatchName(String name){
        Cursor c = mDB.rawQuery( "select * from songdata where name=" + "'" + name + "'" , null);
        return c;
    }
}