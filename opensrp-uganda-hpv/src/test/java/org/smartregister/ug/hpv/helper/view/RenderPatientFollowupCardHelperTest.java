package org.smartregister.ug.hpv.helper.view;

import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.activity.PatientDetailActivity;

import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(RobolectricTestRunner.class)
public class RenderPatientFollowupCardHelperTest {

    @Mock
    private CommonPersonObjectClient client;

    @Mock
    private VaccineRepository vaccineRepository;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private RenderPatientFollowupCardHelper followupCardHelper;

    @Before
    public void setUp() {

        BasePatientDetailActivity activity = Robolectric.buildActivity(PatientDetailActivity.class).get();
        followupCardHelper = new RenderPatientFollowupCardHelper(activity, client);

        Mockito.when(client.entityId()).thenReturn("noString");
    }

    @Test
    public void testIsValidForUndo_hpv2IsSynced_shouldReturnFalse() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Synced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertFalse(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndo_hpv1AndHpv2AreNotSynced_shouldReturnTrue() {
        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testIsValidForUndo_hpv1IsSyncedHpv2IsNotSynced_shouldReturnTrue() {
        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Synced", "Unsynced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndo_hpv1IsNotSyncedAndHpv2DoesNotExist_shouldReturnTrue() {
        try {
            ArrayList<Vaccine> vaccines = generateVaccines(1, "Unsynced", null);

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndo_hpv1IsSyncedAndHpv2DoesNotExist_shouldReturnFalse() {
        try {
            ArrayList<Vaccine> vaccines = generateVaccines(1, "Synced", null);

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertFalse(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRenderUndoVaccinationButton_buttonShouldBeVisible() {

        ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

        Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);
        followupCardHelper.setVaccineRepository(vaccineRepository);

        Button undoBtn = new Button(RuntimeEnvironment.application);
        undoBtn.setVisibility(View.GONE);

        try {
            Whitebox.invokeMethod(followupCardHelper, "renderUndoVaccinationButton", true, undoBtn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert(undoBtn.getVisibility() == View.VISIBLE);
    }

    @Test
    public void testRenderUndoVaccinationButton_buttonShouldNotBeVisible() {

        ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

        Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);
        followupCardHelper.setVaccineRepository(vaccineRepository);

        Button undoBtn = new Button(RuntimeEnvironment.application);
        undoBtn.setVisibility(View.VISIBLE);

        try {
             Whitebox.invokeMethod(followupCardHelper, "renderUndoVaccinationButton", false, undoBtn);
        } catch (Exception e) {
             e.printStackTrace();
        }
        assert(undoBtn.getVisibility() == View.GONE);
    }

    private ArrayList<Vaccine> generateVaccines(int numDoses, String hpv1Status, String hpv2Status) {

        ArrayList vaccines = new ArrayList();
        for (int i = 1; i < numDoses + 1; i++) {

            Vaccine vaccine = new Vaccine();
            vaccine.setName("hpv " + i);
            if (i == 1) {
                vaccine.setSyncStatus(hpv1Status);
            } else {
                vaccine.setSyncStatus(hpv2Status);
            }
            vaccines.add(vaccine);
        }
        return vaccines;
    }
}