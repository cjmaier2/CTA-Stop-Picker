package cjm.ctastoppicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

/*
 * content from StackOverflow
 * http://stackoverflow.com/questions/27086538/autocompletetextview-remove-soft-keyboard-on-back-press-instead-of-suggestions
 * Question asked by B T (http://stackoverflow.com/users/1386784/b-t)
 * Answer provided by Mostafa Gazar (http://stackoverflow.com/users/2874139/mostafa-gazar)
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(findFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
