package org.astemir.ascript.core;

public final class AFuncArgument {

    private final String name;
    private final AExpression value;

    public AFuncArgument(String name) {
        this(name, null);
    }

    public AFuncArgument(String name, AExpression value) {
        this.name = name;
        this.value = value;
    }


    public String name() {
        return name;
    }

    public AExpression value() {
        return value;
    }

    @Override
    public String toString() {
        return name + (value == null ? "" : " = " + value);
    }
}