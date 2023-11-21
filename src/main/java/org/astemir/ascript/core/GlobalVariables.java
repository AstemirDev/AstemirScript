package org.astemir.ascript.core;

import org.astemir.ascript.core.values.AValue;
import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {
    private static Map<String, AValue> globalVariables = new HashMap<>();
    public static <T extends AValue> T globalVar(String name,T value){
        globalVariables.put(name,value);
        return value;
    }

    public static Map<String, AValue> getGlobalVariables() {
        return globalVariables;
    }
}
