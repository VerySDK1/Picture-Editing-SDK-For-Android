package com.pesdk.uisdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseRoot extends SQLiteOpenHelper {
    private final static int NEWVERSION = 4;

    public DatabaseRoot(Context context) {
        super(context, "pesdk.db", null, NEWVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DraftData.createTable(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4 && newVersion >= 4) {
            DraftData.createTable(db);
        }
    }
}
