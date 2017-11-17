package com.ithaibo.zbar;


import android.text.TextUtils;
import android.util.Log;

import com.andy.zbar.Config;
import com.andy.zbar.Image;
import com.andy.zbar.ImageScanner;
import com.andy.zbar.Symbol;
import com.andy.zbar.SymbolSet;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;

//import net.sourceforge.zbar.Config;
//import net.sourceforge.zbar.Image;
//import net.sourceforge.zbar.ImageScanner;
//import net.sourceforge.zbar.Symbol;
//import net.sourceforge.zbar.SymbolSet;

/**
 * Created by Andy on 2017/11/15.
 */

public class ZbarDecoder implements Decoder {
    static {
        System.loadLibrary("iconv");
    }

    private ImageScanner scanner;

    public ZbarDecoder() {
        initScanner();
    }

    private void initScanner() {
        scanner = new ImageScanner();
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        SymbolSet symbolSet = scanner.getResults();
        for (Symbol symbol : symbolSet) {
            scanner.setConfig(symbol.getType(), Config.ENABLE, 0);
        }
        scanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
//        scanner.setConfig(Symbol.CODE39, Config.ENABLE, 1);
//        scanner.setConfig(Symbol.CODE93, Config.ENABLE, 1);
//        scanner.setConfig(Symbol.I25, Config.ENABLE, 1);
    }

    @Override
    public Result decode(LuminanceSource source) {
        Result result = null;
        if (source !=null) {
            Log.i("ZbarDecoder", "source width: " + source.getWidth() + ", height: " + source.getHeight());
            Image image = new Image(source.getWidth(), source.getHeight(), "Y800");
            image.setData(source.getMatrix());
            int resultInt = scanner.scanImage(image);
            if (resultInt!=0) {
                String resultStr = null;
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
//                    if (sym.getType()!=Symbol.CODE128 && sym.getType()!=Symbol.CODE39) {
//                        continue;
//                    }
                    resultStr = sym.getData();
                }
                if (!TextUtils.isEmpty(resultStr)) {
                    result = new Result(resultStr, null, null);
                }
            }
        }
        return result;
    }
}
