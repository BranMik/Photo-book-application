package com.branko.photobook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbImport extends SQLiteOpenHelper{

	
	public DbImport(Context context,String name) {
		super(context, name, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table data "
				+ "(id integer primary key, elementTitle text,elementText text,elementPicPath text," +
				"isFullScreen text,elementRotation text,bookId integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS data");
		onCreate(db);
	}

}
