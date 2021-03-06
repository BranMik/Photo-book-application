package com.branko.photobook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditTextBackEv extends EditText{

	    private EditTextImeBackListener mOnImeBack;

	    public EditTextBackEv(Context context) {
	        super(context);
	    }

	    public EditTextBackEv(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    public EditTextBackEv(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }

	    @Override
	    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
	        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
	        	
	            if (mOnImeBack != null) mOnImeBack.onImeBack(this, this.getText().toString());
	        }
	        return super.dispatchKeyEvent(event);
	    }

	    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
	        mOnImeBack = listener;
	    }
	}

