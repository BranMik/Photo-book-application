package com.branko.photobook;

import java.io.File;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.branko.photobook.MainActivity.Orientation;

@SuppressLint("DefaultLocale") 
public class BookFragment extends Fragment {
	public static final String ELEMENT_TITLE = "ELEMENT_TITLE";
	public static final String ELEMENT_TEXT = "ELEMENT_TEXT";
	public static final String ELEMENT_PIC_PATH = "ELEMENT_PIC_PATH";
	public static final String ELEMENT_PIC_FULL_SCREEN = "ELEMENT_PIC_FULL_SCREEN";
	public static final String ELEMENT_PIC_ROTATION = "ELEMENT_PIC_ROTATION";
	public static final String ELEMENT_ELEMENT_ID = "ELEMENT_ELEMENT_ID";
	private TextView tvTitle;
	private TextView tvText;
	private EditTextBackEv etText;
	private EditTextBackEv etTitle;
	private TouchImageView ivPic;
	private RelativeLayout parentRelLay2;
	private Button fullUnfull;
	private Button delElement;
	private Button rotateRight;
	private MainActivity mainInstance;
	private String elRotation;
	private String elFullScr;
	private String elTitle;
	private String elText;
	private String elPath;
	private int elementDbId;
	private View v;

	public static final BookFragment newInstance(String title, String text,
			String picPath, String isFullScreen, String rotation, int elId,
			int num) {
		BookFragment f = new BookFragment();
		Bundle bdl = new Bundle(5);
		bdl.putString(ELEMENT_TITLE, title);
		bdl.putString(ELEMENT_TEXT, text);
		bdl.putString(ELEMENT_PIC_PATH, picPath);
		bdl.putString(ELEMENT_PIC_FULL_SCREEN, isFullScreen);
		bdl.putString(ELEMENT_PIC_ROTATION, rotation);
		bdl.putInt(ELEMENT_ELEMENT_ID,elId);
		bdl.putInt("num", num);
		f.setArguments(bdl);
		return f;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindDrawables(parentRelLay2);

		parentRelLay2 = null;
		ivPic.setImageBitmap(null);
		ivPic.setOnClickListener(null);
		ivPic = null;
		etTitle.setText(null);
		etTitle.setOnClickListener(null);
		etTitle.setOnFocusChangeListener(null);
		etTitle = null;
		fullUnfull.setOnClickListener(null);
		fullUnfull = null;
		tvTitle.setText(null);
		tvTitle.setOnClickListener(null);
		tvTitle = null;
		tvText.setText(null);
		tvText.setOnClickListener(null);
		tvText = null;
		etText.setText(null);
		etText.setOnClickListener(null);
		etText.setOnFocusChangeListener(null);
		etText = null;
		delElement.setOnClickListener(null);
		delElement = null;
		rotateRight.setOnClickListener(null);
		rotateRight = null;
		elFullScr = null;
		elPath = null;
		elRotation = null;
		elText = null;
		elTitle = null;
		v = null;
		System.gc();
		Runtime.getRuntime().gc();
	}

	public static void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		try {
			super.setUserVisibleHint(isVisibleToUser);
			if (isVisibleToUser) {
				((MainActivity) getActivity()).highlightGalleryElement();
			}
		} catch (Exception e) {
			System.out.println("GREŠKA U setUserVisibleHint " + e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		elTitle = getArguments().getString(ELEMENT_TITLE).toUpperCase();
		elText = getArguments().getString(ELEMENT_TEXT);
		elPath = getArguments().getString(ELEMENT_PIC_PATH);
		elFullScr = getArguments().getString(ELEMENT_PIC_FULL_SCREEN);
		elRotation = getArguments().getString(ELEMENT_PIC_ROTATION);
		elementDbId=getArguments().getInt(ELEMENT_ELEMENT_ID);
		elPath = (elPath == null) ? "" : elPath;
		v = inflater.inflate(R.layout.book_fragment_layout, container, false);
		
		parentRelLay2 = (RelativeLayout) v.findViewById(R.id.relParent2);
		etTitle = (EditTextBackEv) v.findViewById(R.id.etElementTitle);
		fullUnfull = (Button) v.findViewById(R.id.buttonFullUnfull);
		tvTitle = (TextView) v.findViewById(R.id.tvElementTitle);
		tvText = (TextView) v.findViewById(R.id.tvElementText);
		ivPic = (TouchImageView) v.findViewById(R.id.ivElementPhoto);
		etText = (EditTextBackEv) v.findViewById(R.id.etElementText);
		delElement = (Button) v.findViewById(R.id.buttonDelEl);
		rotateRight = (Button) v.findViewById(R.id.buttonRotateRight);
		mainInstance = ((MainActivity) getActivity());
		
		tvTitle.setText(elTitle);
		tvText.setText(elText);
		
		etText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					PhotoUtils.showSoftKeyboard(mainInstance);
				}
			}
		});

		etText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unFocusEt();
			}
		});
		
		etText.setOnEditTextImeBackListener(new EditTextImeBackListener(){

			@Override
			public void onImeBack(EditTextBackEv ctrl, String text) {
				// TODO Auto-generated method stub
				etText.setVisibility(View.INVISIBLE);
			}
			
		});
		
		etTitle.setOnEditTextImeBackListener(new EditTextImeBackListener(){

			@Override
			public void onImeBack(EditTextBackEv ctrl, String text) {
				// TODO Auto-generated method stub
				etTitle.setVisibility(View.INVISIBLE);
			}
			
		});
		
		etTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				unFocusEt();
			}
		});

		etTitle.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					PhotoUtils.showSoftKeyboard(mainInstance);
				}
			}
		});

		rotateRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				endEtAndLw();
				rotateRight();
			}
		});
		delElement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				endEtAndLw();
				delElement();
			}
		});

		fullUnfull.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				endEtAndLw();
				fullUnfullElement();
			}
		});
		
		tvTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (etTitle.getVisibility() == View.VISIBLE || etText.getVisibility() == View.VISIBLE) {
					unFocusEt();
				}else if (tvText.getLayoutParams().height != 400) {
					tvTitle.setBackgroundResource(R.drawable.title_background_high);
					tvTitle.setAlpha(1f);
					expandTextView(true);
				} else{
					etTitle.setVisibility(View.VISIBLE);
					etTitle.requestFocus();
				}
				mainInstance.getLPic().setVisibility(View.INVISIBLE);
			}
		});
		
		tvText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (etTitle.getVisibility() == View.VISIBLE || etText.getVisibility() == View.VISIBLE) {
					unFocusEt();
				}else if (!(tvText.getLayoutParams().height == 400 && MainActivity.getOrientation()==Orientation.VERTICAL) &&
						!(tvText.getLayoutParams().width == 400 && MainActivity.getOrientation()==Orientation.HORIZONTAL)) {
					expandTextView(true);
				} else{
					etText.setVisibility(View.VISIBLE);
					etText.requestFocus();
				} 
				mainInstance.getLPic().setVisibility(View.INVISIBLE);
			}
		});

		ivPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (etText.getVisibility() == View.VISIBLE
						|| etTitle.getVisibility() == View.VISIBLE) {
					unFocusEt();
				}else{
					shrinkTextView(true);
				}
				mainInstance.getLPic().setVisibility(View.INVISIBLE);
				tvTitle.setBackgroundResource(R.drawable.title_background_high);
				tvTitle.setAlpha(1f);
			}
		});
		setImageView();
		checkTextField(false);
		return v;
	}
	
	private void endEtAndLw(){
		if (etText.getVisibility() == View.VISIBLE
				|| etTitle.getVisibility() == View.VISIBLE) {
			unFocusEt();
		}
		mainInstance.getLPic().setVisibility(View.INVISIBLE);
	}
	
	private void checkTextField(boolean clicked){
		if(elText!=null && elText.length()>80){
			tvText.setTypeface(null, Typeface.NORMAL);
			tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
			tvText.setTextColor(Color.parseColor("#ffffff"));
			expandTextView(clicked);
		}else{
			
			if(elText==null || elText.length()<1){
				elText=getResources().getString(R.string.text_field_empty);
				tvText.setTypeface(null, Typeface.BOLD);
				tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
				tvText.setTextColor(Color.parseColor("#707070"));
			}else{
				tvText.setTypeface(null, Typeface.NORMAL);
				tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
				tvText.setTextColor(Color.parseColor("#ffffff"));
			}
			shrinkTextView(false);
		}
		tvText.setText(elText);
	}
	
	private void checkFullScr(){
		if (elFullScr.equals("true")) {
			ivPic.setScaleType(ScaleType.FIT_XY);
			fullUnfull.setBackgroundResource(R.drawable.unfull_scr2);
		} else {
			ivPic.setScaleType(ScaleType.FIT_CENTER);
			fullUnfull.setBackgroundResource(R.drawable.full_scr2);
		}
	}

	private void expandTextView(boolean clicked){
		tvTitle.setBackgroundResource(R.drawable.title_background_high);
		tvText.setBackgroundResource(R.drawable.text_background_high);
		tvTitle.setAlpha(1f);
		tvText.setAlpha(1f);
		final float scale = getResources().getDisplayMetrics().density;
		if(MainActivity.getOrientation()==MainActivity.Orientation.VERTICAL){
			if(tvText.getHeight()==0 && clicked){
				int pixels = (int) (55 * scale + 0.5f);
				tvText.getLayoutParams().height = pixels;
			}else{
				tvText.getLayoutParams().height=400;
			}
		}else{
			if(tvText.getWidth()==0 && clicked){
				int pixels = (int) (100 * scale + 0.5f);
				tvText.getLayoutParams().width = pixels;
			}else{
				tvText.getLayoutParams().width=400;
			}
		}
		tvText.requestLayout();
	}
	
	private void shrinkTextView(boolean clicked){
		tvText.setBackgroundResource(R.drawable.text_background_low);
		tvTitle.setBackgroundResource(R.drawable.title_background_low);
		tvText.setAlpha(1f);
		final float scale = getResources().getDisplayMetrics().density;
		if(MainActivity.getOrientation()==MainActivity.Orientation.VERTICAL){
			if(tvText.getHeight()==400){
			int pixels = (int) (55 * scale + 0.5f);
			tvText.getLayoutParams().height = pixels;
			}else if(tvText.getHeight()!=0 && clicked){
				tvText.getLayoutParams().height=0;
			}
		}else{
			if(tvText.getWidth()==400){
				int pixels = (int) (100 * scale + 0.5f);
				tvText.getLayoutParams().width = pixels;
				}else if(tvText.getHeight()!=0 && clicked){
					tvText.getLayoutParams().width=0;
				}
		}
		
		tvText.requestLayout();
	}
	
	private void setImageView(){		
		File imgFile = new File(elPath);
		if (imgFile.exists()) {
			//PhotoUtils.loadBitmap(imgFile.getAbsolutePath(), ivPic, 640,400);
			 Bitmap myBitmap = PhotoUtils.decodeSampledBitmapFromFile(
			 imgFile.getAbsolutePath(), 400,250);
			 myBitmap=PhotoUtils.RotateBitmap(myBitmap, Float.parseFloat(elRotation));
			 ivPic.setImageBitmap(myBitmap);
			 ivPic.setScaleType(ScaleType.FIT_CENTER);
		} else {
			System.out.println("NEMA PATH");
		}
		checkFullScr();
	}

	private void rotateRight() {
		int id = MainActivity.getPager().getCurrentItem();
		float rotFl = Float.parseFloat(elRotation) + 90f;
		if (rotFl > 360f) {
			rotFl = rotFl - 360f;
		}
		Bitmap myBitmap=PhotoUtils.RotateBitmap(((BitmapDrawable)ivPic.getDrawable()).getBitmap(), 90f);
		ivPic.setImageBitmap(myBitmap);
		ivPic.setScaleType(ScaleType.FIT_CENTER);
		elRotation = String.valueOf(rotFl);
		mainInstance.getPhotoBookDb().updateRotation(elementDbId, elRotation);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ROTATION).set(id, elRotation);
		this.getArguments().putString(ELEMENT_PIC_ROTATION, elRotation);
		rotateTumbnail();
	}

	private void rotateTumbnail() {
		int id = MainActivity.getPager().getCurrentItem();
		ImageView iv = (ImageView) mainInstance.getLay().getChildAt(id);
		iv.setRotation(Float.parseFloat(elRotation));
	}

	private void delElement() {
		int id = MainActivity.getPager().getCurrentItem();
		int goTo = (id > 0) ? id - 1 : (id < MainActivity.getPager()
				.getChildCount() - 1) ? id + 1 : id;
		mainInstance.goToElement(goTo);
		mainInstance.getPhotoBookDb().deleteElement(elementDbId);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_INDX).remove(id);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_PATH).remove(id);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_TITLE).remove(id);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_TEXT).remove(id);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ISFULLSCREEN).remove(id);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ROTATION).remove(id);
		mainInstance.getPageAdapter().destroyItem(MainActivity.getPager(),
				MainActivity.getPager().getCurrentItem(), this);
		mainInstance.getPageAdapter().getItemPosition(null);
		mainInstance.getPageAdapter().notifyDataSetChanged();
		mainInstance.getLay().removeViewAt(id);
		mainInstance.getLay().requestLayout();
		mainInstance.delElementFromLwAdapter(id);
	}

	private void fullUnfullElement() {
		int id = MainActivity.getPager().getCurrentItem();
		if (mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ISFULLSCREEN).get(id).equals("false")) {
			mainInstance.getPhotoBookDb().updateFullScr(elementDbId, "true");
			mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ISFULLSCREEN).set(id, "true");
			elFullScr = "true";
			this.getArguments().putString(ELEMENT_PIC_FULL_SCREEN, "true");
		} else {
			mainInstance.getPhotoBookDb().updateFullScr(elementDbId, "false");
			mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_ISFULLSCREEN).set(id, "false");
			elFullScr = "false";
			this.getArguments().putString(ELEMENT_PIC_FULL_SCREEN, "false");
		}
		checkFullScr();
	}

	public void unFocusEt() {
		PhotoUtils.hideSoftKeyboard(mainInstance);
		if (etText.getVisibility() == View.VISIBLE) {
			updateTxt(false);
		} else if (etTitle.getVisibility() == View.VISIBLE) {
			updateTitle();
		}
	}

	public void updateTxt(boolean clicked) {
		String txt = etText.getText().toString();
		elText=txt;
		etText.setText("");
		etText.setVisibility(View.INVISIBLE);
		tvText.setText(elText);
		//tvText.requestFocus();
		int id = MainActivity.getPager().getCurrentItem();
		mainInstance.getPhotoBookDb().updateText(elementDbId, txt);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_TEXT).set(id, txt);
		this.getArguments().putString(ELEMENT_TEXT, txt);
		checkTextField(clicked);
	}

	@SuppressLint("DefaultLocale")
	public void updateTitle() {
		String txt = etTitle.getText().toString().toUpperCase();
		elTitle=txt;
		tvTitle.setText(elTitle);
		etTitle.setText("");
		etTitle.setVisibility(View.INVISIBLE);
		tvTitle.requestFocus();
		int id = MainActivity.getPager().getCurrentItem();
		mainInstance.getPhotoBookDb().updateTitle(elementDbId, txt);
		mainInstance.getBookData().get(MainActivity.BOOK_DATA_ARRAY_INDX_TITLE).set(id, txt);
		this.getArguments().putString(ELEMENT_TITLE, txt);
	}

	public ImageView getIvPic() {
		return ivPic;
	}

	public EditText getEtText() {
		return etText;
	}
	
	public String getElTitle() {
		return elTitle;
	}

}
