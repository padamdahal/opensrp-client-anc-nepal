package org.smartregister.ug.hpv.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.FadingCircle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.CursorCommonObjectFilterOption;
import org.smartregister.cursoradapter.CursorCommonObjectSort;
import org.smartregister.cursoradapter.CursorSortOption;
import org.smartregister.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.smartregister.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BaseRegisterActivity;
import org.smartregister.ug.hpv.activity.HomeRegisterActivity;
import org.smartregister.ug.hpv.activity.PatientDetailActivity;
import org.smartregister.ug.hpv.event.SyncEvent;
import org.smartregister.ug.hpv.provider.HomeRegisterProvider;
import org.smartregister.ug.hpv.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.ug.hpv.servicemode.HpvServiceModeOption;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ServiceTools;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.smartregister.ug.hpv.activity.BaseRegisterActivity.TOOLBAR_TITLE;
import static org.smartregister.ug.hpv.util.Constants.VIEW_CONFIGS.COMMON_REGISTER_HEADER;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public abstract class BaseRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment implements SyncStatusBroadcastReceiver.SyncStatusListener {

    protected Set<org.smartregister.configurableviews.model.View> visibleColumns = new TreeSet<>();
    protected CommonPersonObjectClient patient;
    protected RegisterActionHandler registerActionHandler = new RegisterActionHandler();

    private String viewConfigurationIdentifier;

    private LocationPickerView facilitySelection;

    private static final String TAG = BaseRegisterFragment.class.getCanonicalName();
    private Snackbar syncStatusSnackbar;
    private View rootView;
    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";

    private TextView initialsTextView;
    private ProgressBar syncProgressBar;

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {


            @Override
            public ServiceModeOption serviceMode() {
                return new HpvServiceModeOption(null, "Linda Clinic", new int[]{
                        R.string.name, R.string.opensrp_id, R.string.dose_d
                }, new int[]{4, 3, 2});
            }

            @Override
            public FilterOption villageFilter() {
                return new CursorCommonObjectFilterOption("no village filter", "");
            }

            @Override
            public SortOption sortOption() {
                return new CursorCommonObjectSort(getResources().getString(R.string.alphabetical_sort), DBConstants.KEY.LAST_INTERACTED_WITH + " DESC");
            }

            @Override
            public String nameInShortFormForTitle() {
                return context().getStringResource(R.string.hpv);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                        new CursorCommonObjectSort(getResources().getString(R.string.alphabetical_sort), DBConstants.KEY.FIRST_NAME),
                        new CursorCommonObjectSort(getResources().getString(R.string.opensrp_id), DBConstants.KEY.OPENSRP_ID)
                };
            }

            @Override
            public String searchHint() {
                return context().getStringResource(R.string.str_search_hint);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_register, container, false);
        rootView = view;//handle to the root

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.register_toolbar);
        AppCompatActivity activity = ((AppCompatActivity) getActivity());

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(activity.getIntent().getStringExtra(TOOLBAR_TITLE));
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        activity.getSupportActionBar().setLogo(R.drawable.round_white_background);
        activity.getSupportActionBar().setDisplayUseLogoEnabled(false);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        setupViews(view);
        return view;
    }

    protected void processViewConfigurations() {
        ViewConfiguration viewConfiguration = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getViewConfiguration(getViewConfigurationIdentifier());
        if (viewConfiguration == null)
            return;
        RegisterConfiguration config = (RegisterConfiguration) viewConfiguration.getMetadata();
        if (config.getSearchBarText() != null && getView() != null)
            ((EditText) getView().findViewById(R.id.edt_search)).setHint(config.getSearchBarText());
        visibleColumns = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getRegisterActiveColumns(getViewConfigurationIdentifier());

    }

    protected void updateSearchView() {
        if (getSearchView() != null) {
            getSearchView().removeTextChangedListener(textWatcher);
            getSearchView().addTextChangedListener(textWatcher);
            getSearchView().setOnKeyListener(hideKeyboard);
        }
    }

    public void setSearchTerm(String searchText) {
        if (getSearchView() != null) {
            getSearchView().setText(searchText);
        }
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        getSearchCancelView().setVisibility(isEmpty(filterString) ? View.INVISIBLE : View.VISIBLE);
        CountExecute();
        filterandSortExecute();
    }

    public void onQRCodeSucessfullyScanned(String qrCode) {
        Log.i(TAG, "QR code: " + qrCode);
        if (StringUtils.isNotBlank(qrCode)) {

            filter(qrCode.replace("-", ""), "", getMainCondition());
        }
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        clientsView.setVisibility(View.VISIBLE);
        clientsProgressView.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.sorted_by_bar).setVisibility(View.GONE);
        processViewConfigurations();
        initializeQueries();
        updateSearchView();
        populateClientListHeaderView(view);
        setServiceModeViewDrawableRight(null);

        View qrCode = view.findViewById(R.id.scan_qr_code);
        qrCode.setOnClickListener(registerActionHandler);

        initialsTextView = view.findViewById(R.id.name_initials);

        AllSharedPreferences allSharedPreferences = context().allSharedPreferences();
        String preferredName = allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM());
        if (!preferredName.isEmpty()) {
            String[] preferredNameArray = preferredName.split(" ");
            String initials = "";
            if (preferredNameArray.length > 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0)) + String.valueOf(preferredNameArray[1].charAt(0));
            } else if (preferredNameArray.length == 1) {
                initials = String.valueOf(preferredNameArray[0].charAt(0));
            }
            initialsTextView.setText(initials);
        }

        facilitySelection = view.findViewById(R.id.facility_selection);
        if (facilitySelection != null) {
            facilitySelection.init();
        }

        syncProgressBar = view.findViewById(R.id.sync_progress_bar);
        if (syncProgressBar != null) {
            FadingCircle circle = new FadingCircle();
            syncProgressBar.setIndeterminateDrawable(circle);
        }
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        renderView();
    }

    private void renderView() {
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        processViewConfigurations();
        updateLocationText();
        refreshSyncProgressSpinner();
    }

    protected void initializeQueries() {

        String tableName = DBConstants.PATIENT_TABLE_NAME;

        HomeRegisterProvider registerProvider = new HomeRegisterProvider(getActivity(), visibleColumns, registerActionHandler);
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, registerProvider, context().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        setTablename(tableName);
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.SelectInitiateMainTableCounts(tableName);
        mainCondition = getMainCondition();
        countSelect = countQueryBuilder.mainCondition(mainCondition);
        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        String[] columns = new String[]{
                tableName + ".relationalid",
                tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                tableName + "." + DBConstants.KEY.BASE_ENTITY_ID,
                tableName + "." + DBConstants.KEY.FIRST_NAME,
                tableName + "." + DBConstants.KEY.LAST_NAME,
                tableName + "." + DBConstants.KEY.CARETAKER_NAME,
                tableName + "." + DBConstants.KEY.CARETAKER_PHONE,
                tableName + "." + DBConstants.KEY.VHT_NAME,
                tableName + "." + DBConstants.KEY.VHT_PHONE,
                tableName + "." + DBConstants.KEY.DOB,
                tableName + "." + DBConstants.KEY.OPENSRP_ID,
                tableName + "." + DBConstants.KEY.CLASS,
                tableName + "." + DBConstants.KEY.SCHOOL,
                tableName + "." + DBConstants.KEY.SCHOOL_NAME,
                tableName + "." + DBConstants.KEY.Location,
                tableName + "." + DBConstants.KEY.DOSE_ONE_DATE,
                tableName + "." + DBConstants.KEY.DATE_DOSE_ONE_GIVEN,
                tableName + "." + DBConstants.KEY.DOSE_TWO_DATE,
                tableName + "." + DBConstants.KEY.DATE_DOSE_TWO_GIVEN,
                tableName + "." + DBConstants.KEY.GENDER,
                tableName + "." + DBConstants.KEY.DATE_REMOVED,
                tableName + "." + DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION,
                tableName + "." + DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION};
        String[] allColumns = ArrayUtils.addAll(columns, getAdditionalColumns(tableName));
        queryBUilder.SelectInitiateMainTable(tableName, allColumns);
        mainSelect = queryBUilder.mainCondition(mainCondition);
        Sortqueries = ((CursorSortOption) getDefaultOptionsProvider().sortOption()).sort();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

        refresh();

    }

    protected abstract void populateClientListHeaderView(View view);

    protected void populateClientListHeaderView(View view, View headerLayout_, String viewConfigurationIdentifier) {
        LinearLayout clientsHeaderLayout = view.findViewById(org.smartregister.R.id.clients_header_layout);
        clientsHeaderLayout.setVisibility(View.GONE);

        View headerLayout = headerLayout_;

        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {
            ViewConfiguration viewConfiguration = helper.getViewConfiguration(viewConfigurationIdentifier);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_HEADER);
            if (viewConfiguration != null)
                headerLayout = helper.inflateDynamicView(viewConfiguration, commonConfiguration, headerLayout, R.id.register_headers, true);
        }
        if (!visibleColumns.isEmpty()) {
            Map<String, Integer> mapping = new HashMap();
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.NAME, R.id.patient_header);
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.ID, R.id.id_header);
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.DOSE, R.id.dose_header);
            helper.processRegisterColumns(mapping, headerLayout, visibleColumns, R.id.register_headers);
        }

        clientsView.addHeaderView(headerLayout);
        clientsView.setEmptyView(getActivity().findViewById(R.id.empty_view));

    }

    protected final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //Overriden Do something before Text Changed
        }

        @Override
        public void onTextChanged(final CharSequence cs, int start, int before, int count) {
            filter(cs.toString(), "", getMainCondition());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //Overriden Do something after Text Changed
        }
    };

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {//Implement Abstract Method
    }

    @Override
    protected void startRegistration() {
        ((HomeRegisterActivity) getActivity()).startFormActivity(Constants.JSON_FORM.PATIENT_REGISTRATION, null, null);
    }

    @Override
    protected void onCreation() {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            boolean isRemote = extras.getBoolean(Constants.IS_REMOTE_LOGIN);
            if (isRemote) {
                startSync();
            }
        }
    }

    protected abstract String getMainCondition();


    protected abstract String[] getAdditionalColumns(String tableName);

    protected String getViewConfigurationIdentifier() {
        return viewConfigurationIdentifier;
    }

    private void goToPatientDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        Map<String, String> patientDetails = patient.getDetails();
        Intent intent = null;
        String registerToken = "";
        intent = new Intent(getActivity(), PatientDetailActivity.class);
        registerToken = Constants.VIEW_CONFIGS.HOME_REGISTER;

        String registerTitle = Utils.readPrefString(getActivity(), TOOLBAR_TITLE + registerToken, "");
        intent.putExtra(Constants.INTENT_KEY.REGISTER_TITLE, registerTitle);
        intent.putExtra(Constants.INTENT_KEY.PATIENT_DETAIL_MAP, (HashMap) patientDetails);
        intent.putExtra(Constants.INTENT_KEY.CLIENT_OBJECT, patient);
        intent.putExtra(Constants.INTENT_KEY.OPENSRP_ID, patientDetails.get(Constants.INTENT_KEY.OPENSRP_ID));
        intent.putExtra(Constants.INTENT_KEY.LAUNCH_VACCINE_DIALOG, launchDialog);
        startActivity(intent);
    }

    class RegisterActionHandler implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.scan_qr_code) {
                ((HomeRegisterActivity) getActivity()).startQrCodeScanner();
            } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                patient = (CommonPersonObjectClient) view.getTag();
                goToPatientDetailActivity(patient, false);
            } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS) {

                patient = (CommonPersonObjectClient) view.getTag();
                goToPatientDetailActivity(patient, true);

            }

        }
    }

    protected void updateLocationText() {
        if (facilitySelection != null) {
            facilitySelection.setText(LocationHelper.getInstance().getOpenMrsReadableName(
                    facilitySelection.getSelectedItem()));
            String locationId = LocationHelper.getInstance().getOpenMrsLocationId(facilitySelection.getSelectedItem());
            context().allSharedPreferences().savePreference(Constants.CURRENT_LOCATION_ID, locationId);

        }
    }

    public LocationPickerView getFacilitySelection() {
        return facilitySelection;
    }


    private void registerSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
    }

    private void unregisterSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        Utils.postEvent(new SyncEvent(fetchStatus));
        refreshSyncStatusViews(fetchStatus);
    }

    @Override
    public void onSyncStart() {
        refreshSyncStatusViews(null);
    }


    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
    }

    private void refreshSyncStatusViews(FetchStatus fetchStatus) {


        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
            syncStatusSnackbar = Snackbar.make(rootView, R.string.syncing,
                    Snackbar.LENGTH_LONG);
            syncStatusSnackbar.show();
        } else {
            if (fetchStatus != null) {
                if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
                if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed, Snackbar.LENGTH_INDEFINITE);
                    syncStatusSnackbar.setActionTextColor(getResources().getColor(R.color.snackbar_action_color));
                    syncStatusSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startSync();
                        }
                    });
                } else if (fetchStatus.equals(FetchStatus.fetched)
                        || fetchStatus.equals(FetchStatus.nothingFetched)) {

                    setRefreshList(true);
                    renderView();

                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_complete, Snackbar.LENGTH_LONG);
                } else if (fetchStatus.equals(FetchStatus.noConnection)) {
                    syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed_no_internet, Snackbar.LENGTH_LONG);
                }
                syncStatusSnackbar.show();
            }

        }

        refreshSyncProgressSpinner();
    }

    private void startSync() {
        ServiceTools.startSyncService(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSyncStatusBroadcastReceiver();
    }

    @Override
    public void onPause() {
        unregisterSyncStatusBroadcastReceiver();
        super.onPause();
    }

    protected View.OnKeyListener hideKeyboard = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Utils.hideKeyboard(getActivity());
                return true;
            }
            return false;
        }
    };

    private void refreshSyncProgressSpinner() {
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            syncProgressBar.setVisibility(View.VISIBLE);
            initialsTextView.setVisibility(View.GONE);
        } else {
            syncProgressBar.setVisibility(View.GONE);
            initialsTextView.setVisibility(View.VISIBLE);
        }
    }
}



