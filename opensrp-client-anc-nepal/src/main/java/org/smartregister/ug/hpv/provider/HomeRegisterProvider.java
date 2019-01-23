package org.smartregister.ug.hpv.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.domain.DoseStatus;
import org.smartregister.ug.hpv.fragment.BaseRegisterFragment;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.DOSE;
import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.ID;
import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.NAME;
import static org.smartregister.ug.hpv.util.Constants.VIEW_CONFIGS.COMMON_REGISTER_ROW;
import static org.smartregister.util.Utils.getName;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class HomeRegisterProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    private Context context;


    public HomeRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, View convertView) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, convertView);
            populateIdentifierColumn(pc, convertView);
            populateDoseColumn(pc, convertView);

            return;
        }
        for (org.smartregister.configurableviews.model.View columnView : visibleColumns) {
            switch (columnView.getIdentifier()) {
                case ID:
                    populatePatientColumn(pc, client, convertView);
                    break;
                case NAME:
                    populateIdentifierColumn(pc, convertView);
                    break;
                case DOSE:
                    populateDoseColumn(pc, convertView);
                    break;
                default:
            }
        }

        Map<String, Integer> mapping = new HashMap();
        mapping.put(ID, R.id.patient_column);
        mapping.put(DOSE, R.id.identifier_column);
        mapping.put(NAME, R.id.dose_column);
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().processRegisterColumns(mapping, convertView, visibleColumns, R.id.register_columns);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, View view) {

        String firstName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String patientName = getName(firstName, lastName);

        fillValue((TextView) view.findViewById(R.id.patient_name), WordUtils.capitalize(patientName));
        fillValue((TextView) view.findViewById(R.id.caretaker_name), WordUtils.capitalize(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.CARETAKER_NAME, false)));

        String dobString = Utils.getDuration(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        fillValue((TextView) view.findViewById(R.id.age), dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString);

        View patient = view.findViewById(R.id.patient_column);
        attachPatientOnclickListener(patient, client);
    }


    private void populateIdentifierColumn(CommonPersonObjectClient pc, View view) {

        fillValue((TextView) view.findViewById(R.id.opensrp_id), org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, false));
    }


    private void populateDoseColumn(CommonPersonObjectClient pc, View view) {


        DoseStatus doseStatus = Utils.getCurrentDoseStatus(pc);

        Button patient = (Button) view.findViewById(R.id.dose_button);

        LinearLayout completeView = (LinearLayout) view.findViewById(R.id.completedView);

        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            patient.setVisibility(View.GONE);
            completeView.setVisibility(View.VISIBLE);
        } else {

            patient.setVisibility(View.VISIBLE);
            completeView.setVisibility(View.GONE);
            patient.setText(getDoseButtonText(doseStatus));
            patient.setBackground(Utils.getDoseButtonBackground(context, Utils.getRegisterViewButtonStatus(doseStatus)));
            patient.setTextColor(Utils.getDoseButtonTextColor(context, Utils.getRegisterViewButtonStatus(doseStatus)));
            attachDosageOnclickListener(patient, pc);
        }
    }

    private String getDoseButtonText(DoseStatus doseStatus) {

        String doseText;

        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            doseText = context.getResources().getString(R.string.complete);
        } else {

            if (StringUtils.isNotBlank(doseStatus.getDoseTwoDate())) {
                doseText = "D2 Due \n" + Utils.formatDate(doseStatus.getDoseTwoDate()) + " \n D1: " + Utils.formatDate(doseStatus.getDateDoseOneGiven());

            } else {
                doseText = "D1 Due \n" + Utils.formatDate(doseStatus.getDoseOneDate());

            }
        }

        return doseText;
    }

    private void adjustLayoutParams(View view, TextView details) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        params = details.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        details.setLayoutParams(params);
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_NORMAL);
    }

    private void attachDosageOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        View view;
        view = inflater.inflate(R.layout.register_home_list_row, null);
        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {

            ViewConfiguration viewConfiguration = helper.getViewConfiguration(Constants.VIEW_CONFIGS.HOME_REGISTER_ROW);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_ROW);

            if (viewConfiguration != null) {
                return helper.inflateDynamicView(viewConfiguration, commonConfiguration, view, R.id.register_columns, false);
            }
        }
        return view;
    }

    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }

}
