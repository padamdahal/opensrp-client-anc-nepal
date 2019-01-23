package org.smartregister.ug.hpv.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.adapter.ServiceLocationsAdapter;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 03/03/2017
 */
public class LocationPickerView extends CustomFontTextView implements View.OnClickListener {

    private final Context context;
    private Dialog locationPickerDialog;
    private ServiceLocationsAdapter serviceLocationsAdapter;
    private OnLocationChangeListener onLocationChangeListener;

    public LocationPickerView(Context context) {
        super(context);
        this.context = context;
    }

    public LocationPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public LocationPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void init() {
        locationPickerDialog = new Dialog(context);
        locationPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        locationPickerDialog.setContentView(R.layout.location_picker_dialog);

        ListView locationsLV = (ListView) locationPickerDialog.findViewById(R.id.locations_lv);

        String defaultLocation = LocationHelper.getInstance().getDefaultLocation();
        serviceLocationsAdapter = new ServiceLocationsAdapter(context, getLocations(defaultLocation));
        locationsLV.setAdapter(serviceLocationsAdapter);
        locationsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HpvApplication.getInstance().getContext().allSharedPreferences().saveCurrentLocality(serviceLocationsAdapter
                        .getLocationAt(position));
                LocationPickerView.this.setText(LocationHelper.getInstance().getOpenMrsReadableName(
                        serviceLocationsAdapter.getLocationAt(position)));
                if (onLocationChangeListener != null) {
                    onLocationChangeListener.onLocationChange(serviceLocationsAdapter
                            .getLocationAt(position));
                }
                locationPickerDialog.dismiss();
            }
        });
        this.setText(LocationHelper.getInstance().getOpenMrsReadableName(getSelectedItem()));

        setClickable(true);
        setOnClickListener(this);
    }

    public String getSelectedItem() {
        String selectedLocation = HpvApplication.getInstance().getContext().allSharedPreferences().fetchCurrentLocality();
        if (TextUtils.isEmpty(selectedLocation) || !serviceLocationsAdapter.getLocationNames().contains(selectedLocation)) {
            selectedLocation = LocationHelper.getInstance().getDefaultLocation();
            HpvApplication.getInstance().getContext().allSharedPreferences().saveCurrentLocality(selectedLocation);
        }
        return selectedLocation;
    }

    public void setOnLocationChangeListener(final OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    private ArrayList<String> getLocations(String defaultLocation) {
        ArrayList<String> locations = LocationHelper.getInstance().locationNamesFromHierarchy(defaultLocation);

        if (locations.contains(defaultLocation)) {
            locations.remove(defaultLocation);
        }
        Collections.sort(locations);
        locations.add(0, defaultLocation);

        return locations;
    }


    @Override
    public void onClick(View v) {
        showDialog();
    }

    private void showDialog() {
        serviceLocationsAdapter.setSelectedLocation(getSelectedItem());


        Window window = locationPickerDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        int[] coords = new int[2];
        LocationPickerView.this.getLocationInWindow(coords);
        wlp.x = coords[0]
                + (int) (LocationPickerView.this.getWidth() * 0.5)
                - (int) (Utils.convertDpToPx(context, 780) * 0.5);

        locationPickerDialog.show();
    }

    public interface OnLocationChangeListener {
        void onLocationChange(String newLocation);
    }

}
