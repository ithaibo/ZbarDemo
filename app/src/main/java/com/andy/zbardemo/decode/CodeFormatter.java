package com.andy.zbardemo.decode;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Andy on 2017/11/15.
 */

public class CodeFormatter {
    private final static CodeFormatter instance = new CodeFormatter();
    private final List<Integer> positiveList = new CopyOnWriteArrayList<>();
    private final List<Integer> negativeList = new CopyOnWriteArrayList<>();
    private CodeFormatter(){}

    public static CodeFormatter getInstance() {
        return instance;
    }

    /**
     *
     * @param formatter check for {@link net.sourceforge.zbar.Symbol}
     */
    public void addAvailableFormatter(Integer formatter) {
        if (formatter == null)
            return;

        if (!positiveList.contains(formatter)) {
            positiveList.add(formatter);
        }

        if (negativeList.contains(formatter)) {
            negativeList.remove(formatter);
        }
    }

    public void removeAvalableFormmater(Integer formatter) {
        if (formatter == null)
            return;

        if (positiveList.contains(formatter)) {
            positiveList.remove(formatter);
        }

        if (!negativeList.contains(formatter)) {
            negativeList.add(formatter);
        }
    }

    public boolean isFormatterAvailable(Integer formatter) {
        if (formatter == null) {
            return false;
        } else {
            return (positiveList.contains(formatter));
        }
    }
}
