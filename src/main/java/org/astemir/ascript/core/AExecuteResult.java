package org.astemir.ascript.core;

import org.astemir.ascript.core.values.AValue;

public class AExecuteResult {
    public static final AExecuteResult VOID = new AExecuteResult("void");
    public static final AExecuteResult BREAK = new AExecuteResult("break");
    public static final AExecuteResult CONTINUE = new AExecuteResult("continue");
    private String name;
    private AValue value;

    public AExecuteResult(String name) {
        this.name = name;
    }

    public AExecuteResult(String name, AValue value) {
        this.name = name;
        this.value = value;
    }

    public AValue getValue() {
        return value;
    }

    public static AExecuteResult returnResult(AValue value){
        return new AExecuteResult("return",value);
    }

    @Override
    public String toString() {
        if (value != null){
            return name+"->"+value;
        }
        return name;
    }
}
