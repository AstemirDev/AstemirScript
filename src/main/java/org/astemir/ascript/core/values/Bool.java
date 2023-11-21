package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AMathOperator;
import org.astemir.ascript.core.GlobalVariables;

public class Bool extends Instance<Boolean> {
    private boolean value;

    public Bool(AContext context, boolean value) {
        super(context, Type.BOOLEAN);
        this.value = value;
    }

    public Bool(AContext context, double value) {
        super(context, Type.BOOLEAN);
        this.value = value == 1 ? true : false;
    }

    public Bool(AContext context, int value) {
        super(context, Type.BOOLEAN);
        this.value = value == 1 ? true : false;
    }

    @Override
    public Boolean value() {
        return value;
    }

    @Override
    public double asNumber() {
        return value ? 1 : 0;
    }
    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public String asString() {
        return value ? "true" : "false";
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        return AValue.numeric(context(),asNumber()).calculate(operator,operand);
    }

    @Override
    public String toString() {
        return asString();
    }
}