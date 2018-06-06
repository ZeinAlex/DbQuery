package ru.khramov.myapplication.BusinessLayer;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static ru.khramov.myapplication.App.getAppContext;

public class Db extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "test.db";
    private static final int DATABASE_VERSION = 3;
    private static Db _instance = null;
    private boolean haveOpenTransaction = false;
    private boolean isOtherDbAttached = false;


    private String execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) {
        String status="";
        if (!this.haveOpenTransaction) {
            db.beginTransaction();
        }
        if (bindArgs == null) {
            try {
                db.execSQL(sql);
                status = "Success";
            } catch (SQLException ex) {
                Log.e("Db.execSQL", ex.toString());
                if (!this.haveOpenTransaction) {
                    db.endTransaction();
                    return ex.toString();
                }
                return ex.toString();
            } catch (Throwable th) {
                if (!this.haveOpenTransaction) {
                    db.endTransaction();
                }
            }
        } else {
            try {
                db.execSQL(sql, bindArgs);
                status = "Success";
            } catch (SQLException e) {
                status = e.toString();
            }
        }
        if (!this.haveOpenTransaction) {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return status;
    }


    public static Db getInstance() {
        if (_instance == null) {
            _instance = new Db(getAppContext());
        }
        return _instance;
    }

    public Db(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Db(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean createDataBase() throws IOException {
        boolean dbExist = canOpenDatabase();
        if (!dbExist) {
            this.getReadableDatabase();
        }
        //getReadableDatabase();
        return dbExist;
    }

    public boolean canOpenDatabase() {
        SQLiteDatabase checkDB = null;
        String dbPath = getAppContext().getFilesDir().getPath() + Db.DATABASE_NAME;

        if (new File(dbPath).exists()) {
            try {
                checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            if (checkDB != null) {
                checkDB.close();
            }
            if (checkDB != null) {
                return true;
            }
        }
        return false;
    }

    private Cursor selectSQL(SQLiteDatabase db, String sql) {
        Cursor curs = null;
        try {
            curs = db.rawQuery(sql, null);
            curs.moveToFirst();
            return curs;
        } catch (SQLException ex) {
            ex.printStackTrace();
            String str = "Db.selectSQL";
            StringBuilder append = new StringBuilder(String.valueOf(ex.toString())).append(" Query:");
            if (sql == null) {
                sql = "";
            }
            Log.e(str, append.append(sql).toString());
            return curs;
        }
    }

    public Cursor selectSQL(String sql) {
        String str;
        //Log.i("DB", sql);
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db != null) {
                return selectSQL(db, sql);
            }
            String str2 = "Db.selectSQL";
            StringBuilder stringBuilder = new StringBuilder("getWritableDatabase() returned null. Query:");
            if (sql == null) {
                str = "";
            } else {
                str = sql;
            }
            Log.e(str2, stringBuilder.append(str).toString());
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            str = "Db.selectSQL";
            StringBuilder append = new StringBuilder(String.valueOf(ex.toString())).append(" Query:");
            if (sql == null) {
                sql = "";
            }
            Log.e(str, append.append(sql).toString());
            return null;
        }
    }


    public void beginTransaction() {
        if (!this.haveOpenTransaction) {
            getWritableDatabase().beginTransaction();
            this.haveOpenTransaction = true;
        }
    }

    public void commitTransaction() {
        if (this.haveOpenTransaction) {
            getWritableDatabase().setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (this.haveOpenTransaction) {
            getWritableDatabase().endTransaction();
            this.haveOpenTransaction = false;
        }
    }

    public String execSQL(String sql) throws SQLException {
        return execSQL(getWritableDatabase(), sql, null);
    }

    public String execSQL(String sql, Object[] bindArgs) throws SQLException {
        return execSQL(getWritableDatabase(), sql, bindArgs);
    }

    public void attathDb(String dbPath, String dbAlias) throws SQLException {
        getWritableDatabase().execSQL("attach '" + dbPath + "' as " + dbAlias);
        this.isOtherDbAttached = true;
    }

    public void detachDb(String dbAlias) throws SQLException {
        getWritableDatabase().execSQL("detach database " + dbAlias);
        this.isOtherDbAttached = false;
    }

    public boolean isOtherDbAttached() {
        return this.isOtherDbAttached;
    }

    public long getDataLongValue(String sql, long defaultValue) {
        Cursor cursor = selectSQL(sql);
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToPosition(0)) {
            if (cursor != null) {
                cursor.close();
            }
            return defaultValue;
        }
        long value = cursor.getLong(0);
        cursor.close();
        return value;
    }

    public int getDataIntValue(String sql, int defaultValue) {
        Cursor cursor = selectSQL(sql);
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToPosition(0)) {
            if (cursor != null) {
                cursor.close();
            }
            return defaultValue;
        }
        int value = cursor.getInt(0);
        cursor.close();
        return value;
    }

    public String getDataStringValue(String sql, String defaultValue) {
        Cursor cursor = selectSQL(sql);
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToPosition(0)) {
            if (cursor != null) {
                cursor.close();
            }
            return defaultValue;
        }
        String value = cursor.getString(0);
        cursor.close();
        return value;
    }

    public double getDataDoubleValue(String sql, double defaultValue) {
        Cursor cursor = selectSQL(sql);
        if (cursor == null || cursor.getCount() == 0 || !cursor.moveToPosition(0)) {
            if (cursor != null) {
                cursor.close();
            }
            return defaultValue;
        }
        double value = cursor.getDouble(0);
        cursor.close();
        return value;
    }


    private boolean isTableEmpty(String table) {
        return getDataIntValue(" SELECT COUNT(1) FROM " + table, 0) == 0;
    }

    public ArrayList getDbTables() {
        ArrayList arrayList = new ArrayList();
        Cursor cursor = selectSQL("select name from SQLITE_MASTER WHERE type = 'table'");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                arrayList.add(cursor.getString(0));
            }
            cursor.close();
        }
        return arrayList;
    }

}

