package com.astra.shared;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExportRow {

    private ExportRow() {
    }

    public static Map<String, Object> of(Object... keysAndValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            row.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return row;
    }
}
