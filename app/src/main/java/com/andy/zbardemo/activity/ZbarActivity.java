package com.andy.zbardemo.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import com.andy.zbardemo.R;
import com.andy.zbardemo.databinding.ActZbarBinding;
import com.andy.zbardemo.utils.SoundUtils;
import com.ithaibo.zbar.BarcodeCallback;
import com.ithaibo.zbar.BarcodeResult;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 2017/11/15.
 */

public class ZbarActivity extends AppCompatActivity {
    ActZbarBinding zbarBinding;
    private List<String> dataList;
    private ArrayAdapter<String> adapter;
    private MyBarCodeScanCallback callback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zbarBinding = DataBindingUtil.setContentView(this, R.layout.act_zbar);
        SoundUtils.getInstance().init(this);

        zbarBinding.btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resume(1);
            }
        });
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(ZbarActivity.this, R.layout.item_scan, dataList);
        zbarBinding.scanList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        zbarBinding.barcodeScanner.getBarcodeView().stopDecoding();
    }

    private void resume(int type) {
        if (null == callback) {
            callback = new MyBarCodeScanCallback(ZbarActivity.this);
        }
        zbarBinding.barcodeScanner.resume();
        if (type == 1) {
            zbarBinding.barcodeScanner.decodeSingle(callback);
        } else {
            zbarBinding.barcodeScanner.getBarcodeView().decodeContinuous(callback);
        }
    }

    private static class MyBarCodeScanCallback implements BarcodeCallback {
        WeakReference<ZbarActivity> ref;
        MyBarCodeScanCallback(@NonNull ZbarActivity activity) {
            ref = new WeakReference<>(activity);
        }
        @Override
        public void barcodeResult(BarcodeResult result) {
            ZbarActivity activity = ref.get();
            if (null == activity) return;

            activity.pause();
            if (result != null && !TextUtils.isEmpty(result.getText())) {
                SoundUtils.getInstance().playSuccess();
                activity.dataList.add(result.getText());
                activity.refreshDataList();
            }
        }
    }

    private void refreshDataList() {
        adapter.notifyDataSetChanged();
    }
}
