package org.smartregister.ug.hpv.view;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.smartregister.ug.hpv.R;

/**
 * Created by ndegwamartin on 24/05/2018.
 */

public class CopyToClipboardDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private String content;
    private static final String TAG = CopyToClipboardDialog.class.getCanonicalName();

    public CopyToClipboardDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public CopyToClipboardDialog(@NonNull Context context, int style) {
        super(context, style);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.widget_copy_to_clipboard);
        findViewById(R.id.copyToClipboardMessage).setOnClickListener(this);
        ((TextView) findViewById(R.id.copyToClipboardHeader)).setText(content);
    }

    @Override
    public void onClick(View v) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.copy_to_clipboard), this.content);
            clipboard.setPrimaryClip(clip);

            dismiss();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void setContent(String content) {
        this.content = content;
    }
}
