package com.branko.photobook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

@SuppressLint("DefaultLocale") public class DbHandler extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "DbPhotoBook.db";
	public static final String DB_DATA_TABLE_NAME = "data";
	public static final String DB_BOOK_TABLE_NAME = "books";
	public static final String DB_POSTAVKE_TABLE_NAME = "postavke";
	public static final String DB_POSTAVKE_COLUMN_TYPE = "type";
	public static final String DB_POSTAVKE_COLUMN_VALUE = "value";
	public static final String DB_POSTAVKE_COLUMN_ID = "id";
	public static final String DB_BOOK_COLUMN_TITLE = "bookTitle";
	public static final String DB_BOOK_ID = "id";
	public static final String DB_COLUMN_ID = "id";
	public static final String DB_COLUMN_TITLE = "elementTitle";
	public static final String DB_COLUMN_TEXT = "elementText";
	public static final String DB_COLUMN_PATH = "elementPicPath";
	public static final String DB_COLUMN_BOOK_ID = "bookId";
	public static final String DB_COLUMN_IS_FULL_SCR = "isFullScreen";
	public static final String DB_COLUMN_ROTATION = "elementRotation";

	public DbHandler(Context context) {
		super(context, DATABASE_NAME, null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table data "
				+ "(id integer primary key, elementTitle text,elementText text,elementPicPath text," +
				"isFullScreen text,elementRotation text,bookId integer)");
		db.execSQL("create table books (id integer primary key, bookTitle text)");
		db.execSQL("create table postavke (id integer primary key, type text,value text)");
		db.execSQL("insert into books(id,bookTitle) values(0,'Default book')");
		db.execSQL("insert into postavke(id,type,value) values(0,'currentBookId','0')");
		MainActivity.firstStart=true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS data");
		db.execSQL("DROP TABLE IF EXISTS books");
		db.execSQL("DROP TABLE IF EXISTS postavke");
		onCreate(db);
	}

	public boolean insertElement(String title, String path,int bookId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put("elementPicPath", path);
		contentValues.put("elementTitle", title);
		contentValues.put("bookId", bookId);
		contentValues.put("isFullScreen", "false");
		contentValues.put("elementRotation", "0f");

		db.insert(DB_DATA_TABLE_NAME, null, contentValues);
		return true;
	}

	public Cursor getData(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db
				.rawQuery("select * from data where id=" + id + "", null);
		return res;
	}

	public int numberOfRows() {
		SQLiteDatabase db = this.getReadableDatabase();
		int numRows = (int) DatabaseUtils.queryNumEntries(db, DB_DATA_TABLE_NAME);
		return numRows;
	}

	public boolean updateText(Integer id, String text) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("elementText", text);
		db.update(DB_DATA_TABLE_NAME, contentValues, "id = ? ",
				new String[] { Integer.toString(id) });
		return true;
	}

	public boolean updateTitle(Integer id, String title) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("elementTitle", title.toUpperCase());
		db.update(DB_DATA_TABLE_NAME, contentValues, "id = ? ",
				new String[] { Integer.toString(id) });
		return true;
	}
	
	public boolean updateCurrentBookId(String value) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("value", value);
		db.update(DB_POSTAVKE_TABLE_NAME, contentValues, "id = ? ",
				new String[]{"0"});
		return true;
	}
	
	public boolean updateRotation(Integer id, String rot) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("elementRotation", rot);
		db.update(DB_DATA_TABLE_NAME, contentValues, "id = ? ",
				new String[] { Integer.toString(id) });
		return true;
	}
	
	public boolean updateFullScr(Integer id, String fullScr) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("isFullScreen", fullScr);
		db.update(DB_DATA_TABLE_NAME, contentValues, "id = ? ",
				new String[] { Integer.toString(id) });
		return true;
	}

	public Integer deleteElement(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(DB_DATA_TABLE_NAME, "id = ? ",
				new String[] { Integer.toString(id) });
	}
	
	public boolean insertBook(String title, int bookId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("bookTitle", title);
		contentValues.put("id", bookId);
		db.insert(DB_BOOK_TABLE_NAME, null, contentValues);
		return true;
	}
	
	public Integer deleteBook(Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(DB_DATA_TABLE_NAME, "bookId = ? ",
				new String[] { Integer.toString(id) });
		return db.delete(DB_BOOK_TABLE_NAME, "id = ? ",
				new String[] { Integer.toString(id) });
	}
	
	@SuppressWarnings("unchecked")
	public List<ArrayList<String>> getAllBooks(){
		List<ArrayList<String>> array_list = Arrays.asList(new ArrayList<String>(),new ArrayList<String>());
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from books order by id", null);
		res.moveToFirst();
		while (res.isAfterLast() == false) {
			array_list.get(0).add(String.valueOf(res.getInt(res.getColumnIndex(DB_BOOK_ID))));
			array_list.get(1).add(res.getString(res.getColumnIndex(DB_BOOK_COLUMN_TITLE)).toUpperCase());
			res.moveToNext();
		}
		return array_list;
	}

	@SuppressWarnings("unchecked")
	public List<ArrayList<String>> getAllElements(int bookId) {
		List<ArrayList<String>> array_list = Arrays.asList(new ArrayList<String>(),new ArrayList<String>()
				,new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>());
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from data where bookId="+bookId+" order by bookId", null);
		res.moveToFirst();
		while (res.isAfterLast() == false) {
			array_list.get(0).add(String.valueOf(res.getInt(res.getColumnIndex(DB_COLUMN_ID))));
			array_list.get(1).add(res.getString(res.getColumnIndex(DB_COLUMN_PATH)));
			array_list.get(2).add(res.getString(res.getColumnIndex(DB_COLUMN_TITLE)).toUpperCase());
			array_list.get(3).add(res.getString(res.getColumnIndex(DB_COLUMN_TEXT)));
			array_list.get(4).add(res.getString(res.getColumnIndex(DB_COLUMN_IS_FULL_SCR)));
			array_list.get(5).add(res.getString(res.getColumnIndex(DB_COLUMN_ROTATION)));
			res.moveToNext();
		}
		return array_list;
	}
	
	public void importDataFromDb(MainActivity mainInstance,DbImport dbImp,String albumName){
		int newBookId = Integer.parseInt(mainInstance.getAlbumData().get(0).get(
				mainInstance.getAlbumData().get(0).size() - 1)) + 1;
		//treba maxId u books tablici i maxId u data tablici, pa onda sljedeæe - NE TREBA JER AUTOMATSKI!!!!
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		SQLiteDatabase dbImported=dbImp.getReadableDatabase();
		Cursor res=dbImported.rawQuery("select * from data", null);
		res.moveToFirst();
		while(res.isAfterLast()==false){
			String resPath=res.getString(res.getColumnIndex(DB_COLUMN_PATH));
			String path=Environment.getExternalStorageDirectory()
					.getAbsolutePath()+ "/photoBook/"+albumName+"/"+resPath.substring(resPath.lastIndexOf("/")+1);
			contentValues.put("elementPicPath",path);
			contentValues.put("elementTitle", res.getString(res.getColumnIndex(DB_COLUMN_TITLE)));
			contentValues.put("elementText", res.getString(res.getColumnIndex(DB_COLUMN_TEXT)));
			contentValues.put("bookId",newBookId);
			contentValues.put("isFullScreen", "false");
			contentValues.put("elementRotation", "0f");
			db.insert(DB_DATA_TABLE_NAME, null, contentValues);
			res.moveToNext();
		}
		ContentValues contentVal2=new ContentValues();
		//contentVal2.put("id", newBookId);
		contentVal2.put("bookTitle", albumName);
		db.insert(DB_BOOK_TABLE_NAME, null, contentVal2);
		MainActivity.setCurrentBookId(newBookId);
		updateCurrentBookId(String.valueOf(newBookId));
	}
	
	public int getCurrentBookId() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from postavke where id=0", null);
		res.moveToFirst();
		return Integer.parseInt(res.getString(res.getColumnIndex(DB_POSTAVKE_COLUMN_VALUE)));
	}
}
