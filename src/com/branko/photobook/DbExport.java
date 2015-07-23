package com.branko.photobook;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbExport extends SQLiteOpenHelper{

	public static final String DATABASE_NAME = "DbPhotoBookExport.db";
	
	public DbExport(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table data "
				+ "(id integer primary key, elementTitle text,elementText text,elementPicPath text," +
				"isFullScreen text,elementRotation text,bookId integer)");
		//db.execSQL("create table books (id integer primary key, bookTitle text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS data");
		onCreate(db);
	}
	
	public static void exportBookData(Context con,List<ArrayList<String>> bookData){
		DbExport dbExpClass=new DbExport(con);
		SQLiteDatabase dbExp=dbExpClass.getWritableDatabase();
		dbExp.execSQL("delete from data");
		//dbExp.execSQL("delete * from books");
		for(int row=0;row<bookData.get(0).size();row++){
			ContentValues contentValues = new ContentValues();

			contentValues.put("id",row+200);
			contentValues.put("elementTitle",  bookData.get(2).get(row));
			contentValues.put("elementText",  bookData.get(3).get(row));
			contentValues.put("elementPicPath", bookData.get(1).get(row));
			contentValues.put("isFullScreen", bookData.get(4).get(row));
			contentValues.put("elementRotation", bookData.get(5).get(row));
			contentValues.put("bookId",MainActivity.getCurrentBookId());

			dbExp.insert("data", null, contentValues);
		}
	}
	
}
