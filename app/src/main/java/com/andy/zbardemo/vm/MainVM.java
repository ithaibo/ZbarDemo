package com.andy.zbardemo.vm;

import android.content.Intent;
import android.databinding.ObservableField;
import android.view.View;

import com.andy.zbardemo.R;
import com.andy.zbardemo.activity.ZbarActivity;


public class MainVM {
    public final ObservableField<View.OnClickListener> clicker = new ObservableField<View.OnClickListener>(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.zbar) {
                Intent intent = new Intent(v.getContext(), ZbarActivity.class);
                v.getContext().startActivity(intent);
            }
        }
    });
}
