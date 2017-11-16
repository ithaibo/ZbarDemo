package com.andy.zbardemo.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import com.andy.zbardemo.R;
import com.andy.zbardemo.databinding.ActZbarBinding;
import com.andy.zbardemo.utils.SoundUtils;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy on 2017/11/15.
 */

public class ZbarActivity extends AppCompatActivity {
    ActZbarBinding zbarBinding;
    private List<String> dataList;

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
        zbarBinding.barcodeScanner.resume();
        if (type == 1) {
            zbarBinding.barcodeScanner.decodeSingle(barcodeCallback);
        } else {
            zbarBinding.barcodeScanner.getBarcodeView().decodeContinuous(barcodeCallback);
        }
    }

    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            pause();

            if (result != null && !TextUtils.isEmpty(result.getText())) {
                SoundUtils.getInstance().playSuccess();
                dataList.add(result.getText());
                zbarBinding.scanList.setAdapter(new ArrayAdapter<String>(ZbarActivity.this, R.layout.item_scan, dataList));

                /*zbarBinding.barcodeScanner.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resume(2);
                    }
                }, 300);*/
            }
        }
    };
}
