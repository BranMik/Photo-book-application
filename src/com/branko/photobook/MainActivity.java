package com.branko.photobook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.branko.photobook.ConcurentTasks.ExportImportTask;

@SuppressLint("DefaultLocale")
public class MainActivity extends FragmentActivity {

	private static int maxPagerId;
	private static final String FTYPE = ".jpg";
	private static int currentBookId = 0;
	private String[] mFileList;
	private File mPath;
	private String mChosenFile;
	private DbHandler photoBookDb;
	private ArrayList<String> arrayLWSlike;
	private ArrayAdapter<String> lwAdapter;
	private ArrayList<String> arrayLWAlbumi;
	private MyPageAdapter pageAdapter;
	private List<ArrayList<String>> bookData;// 0=index,1=path,2=title,3=text,4=isFullScreen,5=rotation
												// (float)
	private List<ArrayList<String>> albumData;// 0=index,1=title
	private SearchView s;
	private static ViewPager pager;
	private Button bList;
	private ListView lPic;
	private HorizontalScrollView galHor = null;
	private ScrollView galVer = null;
	private LinearLayout lay;
	private static Orientation orientation;
	public static Boolean firstStart = false;
	private ImageView startHint;
	
	public static int BOOK_DATA_ARRAY_INDX_INDX=0;
	public static int BOOK_DATA_ARRAY_INDX_PATH=1;
	public static int BOOK_DATA_ARRAY_INDX_TITLE=2;
	public static int BOOK_DATA_ARRAY_INDX_TEXT=3;
	public static int BOOK_DATA_ARRAY_INDX_ISFULLSCREEN=4;
	public static int BOOK_DATA_ARRAY_INDX_ROTATION=5;
	
	public static int ALBUM_DATA_ARRAY_INDX_INDX=0;
	public static int ALBUM_DATA_ARRAY_INDX_TITLE=1;
	
	private enum PozivateljDijaloga {
		OPEN_PIC, CREATE_ALBUM, OPEN_ALBUM, DELETE_ALBUM;
	}

	public static enum Orientation {
		HORIZONTAL, VERTICAL;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initAll();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("item", pager.getCurrentItem());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater i = getMenuInflater();
		i.inflate(R.menu.meni, menu);
		s = (SearchView) menu.findItem(R.id.menu_search).getActionView();

		s.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				search(arg0);
				s.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
				return false;
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_pic:
			openInsertElementDialog();
			break;
		case R.id.menu_open_album:
			if (arrayLWAlbumi.size() > 0) {
				openListDialog(getResources().getString(R.string.open_d_album),
						getResources().getString(R.string.open_album),
						PozivateljDijaloga.OPEN_ALBUM, albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE));
			} else {
				Toast.makeText(this, R.string.no_album_open, Toast.LENGTH_LONG)
						.show();
			}

			break;
		case R.id.menu_create_album:
			createAlbum();
			break;
		case R.id.menu_delete_album:
			if (arrayLWAlbumi.size() > 0) {
				openListDialog(getResources()
						.getString(R.string.deleting_album), getResources()
						.getString(R.string.delete),
						PozivateljDijaloga.DELETE_ALBUM, arrayLWAlbumi);
			} else {
				Toast.makeText(this, R.string.no_album_del, Toast.LENGTH_LONG)
						.show();
			}
			break;
		case R.id.menu_export_album:
			startExportTask();
			break;
		case R.id.menu_import_album:
			importBook();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void initAll() {
		maxPagerId = -1;
		mPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		fillData();
		initPageAdapter();
		initWidgets();
		createGallery();
		if (firstStart) {
			firstStart = false;
			startHint.setVisibility(View.VISIBLE);
		}
	}

	public void fillData() {
		photoBookDb = new DbHandler(this);
		currentBookId = photoBookDb.getCurrentBookId();
		bookData = photoBookDb.getAllElements(currentBookId);
		albumData = photoBookDb.getAllBooks();
		int indx = albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).indexOf(String.valueOf(currentBookId));
		getActionBar().setTitle(albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).get((indx > -1) ? indx : 0));
		arrayLWAlbumi = new ArrayList<String>();
		if (albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).size() > 1) {
			for (int i = 1; i < albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).size(); ++i) {
				arrayLWAlbumi.add(albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).get(i));
			}
		}
	}

	private void initPageAdapter() {
		pageAdapter = new MyPageAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.viewPager);
		pager.setAdapter(pageAdapter);
	}

	private void initWidgets() {
		Display display = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
			orientation = Orientation.VERTICAL;
			galHor = (HorizontalScrollView) findViewById(R.id.gallElementPhotosHor);
		} else {
			orientation = Orientation.HORIZONTAL;
			galVer = (ScrollView) findViewById(R.id.gallElementPhotos);
		}

		lay = (LinearLayout) findViewById(R.id.galLayout);
		bList = (Button) findViewById(R.id.buttonList);
		lPic = (ListView) findViewById(R.id.listPic);
		startHint = (ImageView) findViewById(R.id.imageStartHint);

		arrayLWSlike = new ArrayList<String>();
		if (bookData.get(BOOK_DATA_ARRAY_INDX_INDX).size() > 0) {
			for (int i = 0; i < bookData.get(BOOK_DATA_ARRAY_INDX_INDX).size(); ++i) {
				arrayLWSlike.add(bookData.get(BOOK_DATA_ARRAY_INDX_TITLE).get(i));
			}
		}
		lwAdapter = new ArrayAdapter<String>(this, R.layout.lw_text_style,
				arrayLWSlike);
		lPic.setAdapter(lwAdapter);

		bList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lPic.getVisibility() == View.INVISIBLE) {
					lPic.setVisibility(View.VISIBLE);
				} else {
					lPic.setVisibility(View.INVISIBLE);
				}
			}
		});

		lPic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				goToElement(arg2);
				highlightGalleryElement();
				lPic.setVisibility(View.INVISIBLE);
			}
		});
		lPic.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					lPic.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	private void openListDialog(String naslovProzora, String button1Text,
			final PozivateljDijaloga poz, final List<String> lista) {

		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(naslovProzora);
		final ArrayList<String> zaBrisat = new ArrayList<String>();// naziv
		switch (poz) {
		case DELETE_ALBUM:
			b.setMultiChoiceItems(
					lista.toArray(new CharSequence[lista.size()]), null,
					new OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1,
								boolean arg2) {
							String naziv = lista.get(arg1);
							if (arg2) {
								zaBrisat.add(naziv);
							} else {
								zaBrisat.remove(naziv);
							}
						}
					});

			b.setPositiveButton(button1Text,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String albumi = "";
							for (String al : zaBrisat) {
								albumi = albumi + " " + al + ", ";
							}
							if (albumi.length() > 0) {
								// openDialog(
								// getResources().getString(
								// R.string.question_delete), albumi,
								// getResources().getString(R.string.brisi),
								// PozivateljDijaloga.DELETE_ALBUM);
								brisiAlbume(albumi);
							}
							arg0.dismiss();
						}
					});
			break;
		case OPEN_ALBUM:
			b.setSingleChoiceItems(
					lista.toArray(new CharSequence[lista.size()]), -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							openAlbum(arg1);
							arg0.dismiss();
						}
					});
			break;
		default:
		}
		AlertDialog d = b.create();
		d.show();
	}

	private void openDialog(String naslovProzora, final String poruka,
			String button1Text, final PozivateljDijaloga poz) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(naslovProzora);
		b.setMessage(poruka);
		final MainActivity mainInstance = this;
		final EditText et = new EditText(this);
		if (poz != PozivateljDijaloga.DELETE_ALBUM) {
			b.setView(et);
		}
		b.setPositiveButton(button1Text, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String naslov = "";
				if (poz != PozivateljDijaloga.DELETE_ALBUM) {
					if (et.getText().toString() == null
							|| et.getText().toString() == ""
							|| et.getText().toString().length() < 1) {
						Toast.makeText(
								mainInstance,
								getResources().getString(R.string.no_pic_title),
								Toast.LENGTH_LONG).show();
					} else {
						naslov = (et.getText().toString() == null) ? "" : et
								.getText().toString();
					}
				}
				switch (poz) {
				case OPEN_PIC:
					if (naslov.length() > 0) {
						if (startHint.getVisibility() == View.VISIBLE) {
							startHint.setVisibility(View.INVISIBLE);
						}
						openFileChooserDialog(naslov);
					} else {
						openInsertElementDialog();
					}
					break;
				case DELETE_ALBUM:
					brisiAlbume(poruka);
					break;
				case CREATE_ALBUM:
					currentBookId = Integer.parseInt(albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).get(
							albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).size() - 1)) + 1;
					photoBookDb.updateCurrentBookId(String
							.valueOf(currentBookId));
					albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).add(String.valueOf(currentBookId));
					albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).add(naslov);
					photoBookDb.insertBook(naslov, currentBookId);
					initAll();
					break;
				default:
				}
			}
		});

		b.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
		AlertDialog d = b.create();
		d.show();
	}

	private void openFileChooserDialog(String naslov) {
		final String tip = (naslov.equals("IMPORT BOOK FOR UNZIP")) ? "import"
				: "picture";
		loadFileList(tip);

		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("Choose your file");
		final String naslovFin = naslov;
		if (mFileList == null) {
			builder.create();
		}
		builder.setItems(mFileList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mChosenFile = mFileList[which];
				if (!mChosenFile.equals("..")) {
					String naslovChecked = (naslovFin.length() < 1) ? mChosenFile
							.toUpperCase() : naslovFin.toUpperCase();
					File sel = new File(mPath + "/" + mChosenFile);
					if (sel.isDirectory()) {
						mPath = sel;
						openFileChooserDialog(naslovFin);
					} else {
						// UNOS SQL ELEMENT
						if (tip.equals("import")) {
							startImportTask(sel.getAbsolutePath());
						} else {
							photoBookDb.insertElement(
									naslovChecked.endsWith("JPG") ? naslovChecked
											.substring(0, naslovChecked
													.lastIndexOf("."))
											: naslovChecked, sel
											.getAbsolutePath(), currentBookId);
							bookData = photoBookDb
									.getAllElements(currentBookId);
							addElement();
						}
						dialog.dismiss();
					}
				} else {
					int indx = mPath.getAbsolutePath().lastIndexOf("/");
					mPath = new File(mPath.getAbsolutePath().substring(0, indx));
					openFileChooserDialog(naslovFin);
				}
			}
		});
		builder.show();
	}

	private void loadFileList(final String pozivatelj) {
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			System.out.println("unable to write on the sd card ");
		}
		if (mPath.exists()) {

			FilenameFilter filter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean bol = (pozivatelj.equals("picture")) ? filename
							.contains(FTYPE) : filename.contains(".zip");
					return bol || sel.isDirectory();
				}

			};
			mFileList = new String[(mPath.list(filter) != null) ? mPath
					.list(filter).length + 1 : 1];
			System.arraycopy(new String[] { ".." }, 0, mFileList, 0, 1);
			System.arraycopy(mPath.list(filter), 0, mFileList, 1,
					mPath.list(filter).length);
			// mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
	}

	private void createGallery() {
		final float scale = getResources().getDisplayMetrics().density;
		int pixels = (int) (55 * scale + 0.5f);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				pixels, pixels);
		params.gravity = Gravity.CENTER;
		params.setMargins(0, 5, 3, 0);
		// params.leftMargin = 1;
		lay.removeAllViews();
		int indx = 0;
		for (String path : bookData.get(BOOK_DATA_ARRAY_INDX_PATH)) {
			path = (path == null) ? "" : path;
			File imgFile = new File(path);

			if (imgFile.exists()) {
				final ImageView galImgTmp = new ImageView(this);
				Bitmap myBitmap = PhotoUtils.decodeSampledBitmapFromFile(
						imgFile.getAbsolutePath(), pixels, pixels);

				galImgTmp.setLayoutParams(params);
				// PhotoUtils.loadBitmap(imgFile.getAbsolutePath(), galImgTmp,
				// pixels,pixels);
				galImgTmp.setImageBitmap(myBitmap);
				galImgTmp.setPadding(2, 2, 2, 2);
				galImgTmp.setScaleType(ScaleType.FIT_XY);
				galImgTmp.setRotation(Float.parseFloat(bookData.get(BOOK_DATA_ARRAY_INDX_ROTATION)
						.get(indx)));
				galImgTmp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						lPic.setVisibility(View.INVISIBLE);
						goToElement(lay.indexOfChild(galImgTmp));
						highlightGalleryElement();
					}
				});
				lay.addView(galImgTmp);
				lay.requestLayout();
				if (galVer == null) {
					galHor.requestLayout();
				} else {
					galVer.requestLayout();
				}
				maxPagerId++;
			}
			indx++;
		}
		if (galVer == null) {
			galHor.fullScroll(HorizontalScrollView.FOCUS_LEFT);
		} else {
			galVer.fullScroll(ScrollView.FOCUS_UP);
		}

	}

	private void addElement() {
		maxPagerId++;
		File imgFile = new File(bookData.get(BOOK_DATA_ARRAY_INDX_PATH).get(maxPagerId));
		String path = (imgFile.exists()) ? imgFile.getAbsolutePath() : null;
		if (path != null) {
			final float scale = getResources().getDisplayMetrics().density;
			int pixels = (int) (60 * scale + 0.5f);
			pageAdapter.notifyDataSetChanged();
			addElementToLwAdapter(bookData.get(BOOK_DATA_ARRAY_INDX_TITLE).get(maxPagerId));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					pixels, pixels);
			params.gravity = Gravity.CENTER;
			params.leftMargin = 5;
			final ImageView galImgTmp = new ImageView(this);
			if (path != "") {
				Bitmap myBitmap = PhotoUtils.decodeSampledBitmapFromFile(path,
						pixels, pixels);
				galImgTmp.setImageBitmap(myBitmap);
				// PhotoUtils.loadBitmap(path, galImgTmp, pixels, pixels);
			}
			galImgTmp.setLayoutParams(params);
			galImgTmp.setPadding(2, 2, 2, 2);
			galImgTmp.setTag(path);
			galImgTmp.setScaleType(ScaleType.FIT_CENTER);
			galImgTmp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lPic.setVisibility(View.INVISIBLE);
					goToElement(lay.indexOfChild(galImgTmp));
					highlightGalleryElement();
				}
			});
			lay.addView(galImgTmp);
			lay.requestLayout();

			if (galVer == null) {
				galHor.requestLayout();
			} else {
				galVer.requestLayout();
			}
			goToElement(maxPagerId);
			highlightGalleryElement();
		}
	}

	private void addElementToLwAdapter(String el) {
		arrayLWSlike.add(el);
		lwAdapter.notifyDataSetChanged();
	}

	public void delElementFromLwAdapter(int el) {
		arrayLWSlike.remove(el);
		lwAdapter.notifyDataSetChanged();
	}

	private void createAlbum() {
		openDialog(getResources().getString(R.string.creating_album),
				getResources().getString(R.string.enter_title_for_album),
				getResources().getString(R.string.confirm),
				PozivateljDijaloga.CREATE_ALBUM);
	}

	private void openAlbum(int redni) {
		currentBookId = Integer.parseInt(albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).get(redni));
		photoBookDb.updateCurrentBookId(String.valueOf(currentBookId));
		initAll();
	}

	private void brisiAlbume(String albumi) {
		ArrayList<String> alb = new ArrayList<String>();
		String album = "";
		for (int i = 0; i < albumi.length(); i++) {
			if (albumi.charAt(i) == ',') {
				if (album.length() > 0) {
					alb.add(album.trim());
					album = "";
				}
			} else {
				album += albumi.charAt(i);
			}
		}
		boolean reset = false;
		for (int i = 0; i < alb.size(); i++) {
			int indx = albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).indexOf(alb.get(i));
			photoBookDb
					.deleteBook(Integer.parseInt(albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).get(indx)));
			arrayLWAlbumi.remove(alb.get(i));
			if (currentBookId == Integer.parseInt(albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).get(indx))) {
				currentBookId = 0;
				photoBookDb.updateCurrentBookId(String.valueOf(currentBookId));
				reset = true;
			}
			albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).remove(indx);
			albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE).remove(indx);
		}
		if (reset) {
			initAll();
		}
	}

	public void highlightGalleryElement() {
		try {
			int id = getPager().getCurrentItem();
			int scrollTo = 0;
			for (int i = 0; i < lay.getChildCount(); i++) {
				if (i == id) {
					lay.getChildAt(i).setBackgroundResource(
							R.drawable.gal_pic_background);
					final Handler handler = new Handler();
					final int scrollX = scrollTo;
					handler.postDelayed(new Runnable() {
						public void run() {
							if (galVer == null) {
								galHor.scrollTo(scrollX, 0);
							} else {
								galVer.scrollTo(0, scrollX);
							}
						}
					}, 100L);
				} else {
					lay.getChildAt(i).setBackgroundResource(0);
				}
				scrollTo += lay.getChildAt(i).getWidth();
			}
		} catch (Exception e) {
			for (StackTraceElement el : e.getStackTrace()) {
				System.out.println(el);
			}
		}
	}

	public void exportBookToZip() {
		DbExport.exportBookData(this, bookData);
		File dbFile = getDatabasePath(DbExport.DATABASE_NAME);
		String exportPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/PhotoBook";
		File folder = new File(exportPath);
		if (!folder.exists()) {
			folder.mkdir();
		}
		exportPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/PhotoBook/export";
		folder = new File(exportPath);
		if (!folder.exists()) {
			folder.mkdir();
		}
		exportPath += "/"
				+ albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE)
						.get(albumData.get(ALBUM_DATA_ARRAY_INDX_INDX).indexOf(
								String.valueOf(currentBookId))) + ".zip";
		try {
			FileOutputStream fos = new FileOutputStream(exportPath);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (String filePath : bookData.get(BOOK_DATA_ARRAY_INDX_PATH)) {
				String path = (filePath != null) ? filePath : "";
				File imgFile = new File(path);
				if (imgFile.exists()) {
					addFileToZip(path, zos);
				}
			}
			String path = dbFile.getAbsolutePath();
			addFileToZip(path, zos);
			zos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private void addFileToZip(String path, ZipOutputStream zos) {
		byte[] buffer = new byte[1024];
		try {
			ZipEntry ze = new ZipEntry(path.substring(path.lastIndexOf("/")));
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(path);

			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}

			in.close();
			zos.closeEntry();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void importBook() {
		openFileChooserDialog("IMPORT BOOK FOR UNZIP");
	}

	public void unzipBookForImport(String zipFilePath) {
		String albumName = zipFilePath.substring(
				zipFilePath.lastIndexOf("/") + 1, zipFilePath.lastIndexOf("."));
		boolean ima = true;
		int num = 0;
		while (ima) {
			ima = false;
			for (String al : albumData.get(ALBUM_DATA_ARRAY_INDX_TITLE)) {
				if (al.equals(albumName)) {
					ima = true;
					num++;
					int lastInd = (albumName.contains("_")) ? albumName
							.lastIndexOf("_") : albumName.length();
					albumName = albumName.substring(0, lastInd) + "_" + num;
					break;
				}
			}
		}
		String outputFolder = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/photoBook/" + albumName;
		byte[] buffer = new byte[1024];

		try {
			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(
					zipFilePath));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		photoBookDb.importDataFromDb(this, new DbImport(this, outputFolder
				+ "/DbPhotoBookExport.db"), albumName);
	}

	private void search(String str) {
		int currId = pager.getCurrentItem();
		int goTo = bookData.get(BOOK_DATA_ARRAY_INDX_TITLE).indexOf(str.toUpperCase());
		if (goTo == -1) {
			Toast.makeText(this, getResources().getString(R.string.no_title),
					Toast.LENGTH_LONG).show();
			goTo = currId;
		}
		goToElement(goTo);
		highlightGalleryElement();
	}

	public void openInsertElementDialog() {
		openDialog(getResources().getString(R.string.add_pic), getResources()
				.getString(R.string.enter_pic_title),
				getResources().getString(R.string.choose_pic),
				PozivateljDijaloga.OPEN_PIC);
	}

	public void goToElement(int id) {
		try {
			pager.setCurrentItem(id);
		} catch (Exception e) {
			System.out.println("NEMA ITEMA " + id);
		}
	}

	private void startExportTask() {
		ExportImportTask task = new ExportImportTask(
				ConcurentTasks.TaskType.EXPORT, this, Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/PhotoBook/export");
		task.execute(/* resId */);
	}

	private void startImportTask(String path) {
		ExportImportTask task = new ExportImportTask(
				ConcurentTasks.TaskType.IMPORT, this, path);
		task.execute(/* resId */);
	}

	public List<ArrayList<String>> getAlbumData() {
		return albumData;
	}

	public static int getCurrentBookId() {
		return currentBookId;
	}

	public static void setCurrentBookId(int id) {
		currentBookId = id;
	}

	public ListView getLPic() {
		return lPic;
	}

	public void getShownElementIndex() {
		pager.getCurrentItem();
	}

	public MyPageAdapter getPageAdapter() {
		return pageAdapter;
	}

	public LinearLayout getLay() {
		return lay;
	}

	public List<ArrayList<String>> getBookData() {
		return bookData;
	}

	public void setBookData(List<ArrayList<String>> bookData) {
		this.bookData = bookData;
	}

	public DbHandler getPhotoBookDb() {
		return photoBookDb;
	}

	public void setPhotoBookDb(DbHandler photoBookDb) {
		this.photoBookDb = photoBookDb;
	}

	public static int getMaxPagerId() {
		return maxPagerId;
	}

	public static ViewPager getPager() {
		return pager;
	}

	public static Orientation getOrientation() {
		return orientation;
	}

	class MyPageAdapter extends FragmentStatePagerAdapter {
		// private List<Fragment> fragments;
		private long baseId = 0;

		public MyPageAdapter(FragmentManager fm/* , List<Fragment> fragments */) {
			super(fm);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Object o = super.instantiateItem(container, position);
			return o;
		}

		@Override
		public Fragment getItem(int position) {
			// return this.fragments.get(position);
			String path = (bookData.get(BOOK_DATA_ARRAY_INDX_PATH).get(position) != null) ? bookData
					.get(1).get(position) : "";
			return BookFragment.newInstance(bookData.get(BOOK_DATA_ARRAY_INDX_TITLE).get(position),
					bookData.get(BOOK_DATA_ARRAY_INDX_TEXT).get(position), path,
					bookData.get(BOOK_DATA_ARRAY_INDX_ISFULLSCREEN).get(position), bookData.get(BOOK_DATA_ARRAY_INDX_ROTATION)
							.get(position), Integer.parseInt(bookData.get(BOOK_DATA_ARRAY_INDX_INDX)
							.get(position)), position);
		}

		@Override
		public int getCount() {
			// return this.fragments.size();
			return bookData.get(BOOK_DATA_ARRAY_INDX_INDX).size();
		}

		// this is called when notifyDataSetChanged() is called
		@Override
		public int getItemPosition(Object object) {
			// refresh all fragments when data set changed
			return PagerAdapter.POSITION_NONE;
		}

		public long getItemId(int position) {
			// give an ID different from position when position has been changed
			return baseId + position;
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		/**
		 * Notify that the position of a fragment has been changed. Create a new
		 * ID for each position to force recreation of the fragment
		 * 
		 * @param n
		 *            number of items which have been changed
		 */
		public void notifyChangeInPosition(int n) {
			// shift the ID returned by getItemId outside the range of all
			// previous fragments
			baseId += getCount() + n;
		}
	}

}
