package org.smartregister.ug.hpv.widgets;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.widgets.EditTextFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.DBConstants;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ndegwamartin on 19/03/2018.
 */
public class HpvEditTextFactory extends EditTextFactory {

    @Override
    public void attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText) throws Exception {
        super.attachJson(stepName, context, formFragment, jsonObject, editText);
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        if (jsonObject.has(DBConstants.KEY.NUMBER_PICKER) && jsonObject.get(DBConstants.KEY.NUMBER_PICKER).toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
            List<View> views = new ArrayList<>(1);

            RelativeLayout rootLayout = (RelativeLayout) LayoutInflater.from(context).inflate(
                    R.layout.item_edit_text_number_picker, null);
            final MaterialEditText editText = (MaterialEditText) rootLayout.findViewById(R.id.edit_text);

            attachJson(stepName, context, formFragment, jsonObject, editText);

            JSONArray canvasIds = new JSONArray();
            rootLayout.setId(ViewUtil.generateViewId());
            canvasIds.put(rootLayout.getId());
            editText.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());

            ((JsonApi) context).addFormDataView(editText);
            views.add(rootLayout);

            Button plusbutton = (Button) rootLayout.findViewById(R.id.addbutton);
            Button minusbutton = (Button) rootLayout.findViewById(R.id.minusbutton);

            plusbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String edittesxtstring = editText.getText().toString();
                    if (edittesxtstring.equalsIgnoreCase("")) {
                        editText.setText("0");
                    } else {
                        edittesxtstring = "" + (Integer.parseInt(edittesxtstring) + 1);
                        editText.setText(edittesxtstring);
                    }
                }
            });
            minusbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String edittesxtstring = editText.getText().toString();
                    if (edittesxtstring.equalsIgnoreCase("")) {
                        editText.setText("0");
                    } else {
                        edittesxtstring = "" + (Integer.parseInt(edittesxtstring) - 1);
                        editText.setText(edittesxtstring);
                    }
                }
            });

            editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);


            return views;
        } else {
            return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);
        }


    }

}
