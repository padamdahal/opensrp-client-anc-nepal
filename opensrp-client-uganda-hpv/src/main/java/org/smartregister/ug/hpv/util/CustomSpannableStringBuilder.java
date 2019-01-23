package org.smartregister.ug.hpv.util;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

/**
 * Created by samuelgithengi on 11/13/17.
 */

public class CustomSpannableStringBuilder extends SpannableStringBuilder {

    public SpannableStringBuilder append(CharSequence text, ForegroundColorSpan colorSpan) {
        int start = length();
        append(text);
        setSpan(CharacterStyle.wrap(colorSpan), start, length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return this;
    }
}
