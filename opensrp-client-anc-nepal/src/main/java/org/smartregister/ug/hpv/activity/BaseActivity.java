package org.smartregister.ug.hpv.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.event.LanguageConfigurationEvent;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.util.Log;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.activity.SecuredActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by ndegwamartin on 09/10/2017.
 */

public abstract class BaseActivity extends SecuredActivity {

    private static final int MINIUM_LANG_COUNT = 2;
    protected Toolbar toolbar;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (ConfigurableViewsLibrary.getJsonSpecHelper().getAvailableLanguages().size() < MINIUM_LANG_COUNT) {
            invalidateOptionsMenu();
            MenuItem item = menu.findItem(R.id.action_language);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_language) {
            this.showLanguageDialog();
            return true;
        } else if (id == R.id.action_logout) {
            logOutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOutUser() {
        try {
            DrishtiApplication application = (DrishtiApplication) getApplication();
            application.logoutCurrentUser();
            finish();
        } catch (Exception e) {
            Log.logError(e.getMessage());
        }
    }

    public Context getOpenSRPContext() {
        return ConfigurableViewsLibrary.getInstance().getContext();
    }

    public void showLanguageDialog() {


        final List<String> displayValues = ConfigurableViewsLibrary.getJsonSpecHelper().getAvailableLanguages();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, displayValues.toArray(new String[displayValues.size()])) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(ConfigurableViewsLibrary.getInstance().getContext().getColorResource(org.smartregister.ug.hpv.R.color.customAppThemeBlue));

                return view;
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.select_language));
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = displayValues.get(which);
                Map<String, String> langs = ConfigurableViewsLibrary.getJsonSpecHelper().getAvailableLanguagesMap();
                Utils.saveLanguage(getKeyByValue(langs, selectedItem));
                Utils.postEvent(new LanguageConfigurationEvent(false));
                Utils.showToast(getApplicationContext(), selectedItem + " selected");
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void onCreation() {
        //Overrides
    }

    @Override
    protected void onResumption() {
        //Overrides
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.saving_dialog_title));
        progressDialog.setMessage(getString(R.string.please_wait_message));
    }

    public void showProgressDialog(String title, String message) {
        if (progressDialog != null) {
            if (StringUtils.isNotBlank(title)) {
                progressDialog.setTitle(title);
            }

            if (StringUtils.isNotBlank(message)) {
                progressDialog.setMessage(message);
            }
            progressDialog.show();
        }
    }

    public void showProgressDialog() {
        showProgressDialog(getString(R.string.saving_dialog_title), getString(R.string.please_wait_message));
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
