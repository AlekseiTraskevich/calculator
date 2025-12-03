package org.acme.DTO;

import java.util.HashMap;
import java.util.Map;

public record CalcState(
        String a, String b, String c, String d,
        String opAB, String opBC, String opCD,
        String result,
        String error,
        String roundResult,
        String roundMode
) {
    public CalcState() {
        this(
                "0", "0", "0", "0",
                "add", "add", "add",
                "0",
                "0",
                "0",
                "math"
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("a", a);
        map.put("b", b);
        map.put("c", c);
        map.put("d", d);
        map.put("opAB", opAB);
        map.put("opBC", opBC);
        map.put("opCD", opCD);
        map.put("result", result);
        map.put("error", error);
        map.put("roundResult", roundResult);
        map.put("roundMode", roundMode);
        return map;
    }
}