package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AMathOperator;

public class Numeric extends Instance<Number> {
    public static final Numeric ZERO = new Numeric(AContext.GLOBAL_CONTEXT,0);
    private Format format = Format.DOUBLE;
    private Number value;

    public Numeric(AContext context,boolean value) {
        super(context, Type.NUMERIC);
        this.value = value ? 1 : 0;
    }

    public Numeric(AContext context,double value) {
        super(context, Type.NUMERIC);
        this.value = value;
    }

    public Numeric(AContext context,int value) {
        super(context, Type.NUMERIC);
        this.value = value;
    }

    @Override
    public Number value() {
        switch (format){
            case INT: return value.intValue();
            case DOUBLE: return value.doubleValue();
            case FLOAT: return value.floatValue();
            case LONG: return value.longValue();
        }
        throw new RuntimeException("Invalid number format");
    }

    @Override
    public double asNumber() {
        return value.doubleValue();
    }

    @Override
    public String asString() {
        return value().toString();
    }

    @Override
    public boolean asBoolean() {
        return asNumber() == 1 ? true : false;
    }

    public Numeric format(Format format){
        this.format = format;
        return this;
    }

    public Format getFormat() {
        return format;
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        switch (operator){
            case ADD:{
                if (operand instanceof Numeric) {
                    return new Numeric(context(),asNumber()+operand.asNumber());
                }else
                if (operand instanceof Text){
                    return new Text(context(),asString()+operand.asString());
                }
            }
            case SUB:{
                return new Numeric(context(),asNumber()-operand.asNumber());
            }
            case MULT:{
                if (operand instanceof Numeric) {
                    return new Numeric(context(),asNumber()*operand.asNumber());
                }else
                if (operand instanceof Text){
                    return new Text(context(),operand.asString().repeat((int)value));
                }
            }
            case DIV:{
                return new Numeric(context(),asNumber()/operand.asNumber());
            }
            case EXP: return new Numeric(context(),Math.pow(asNumber(),operand.asNumber()));
            default: throw new RuntimeException("Illegal operator %s!".formatted(operator));
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    public enum Format{
        DOUBLE,INT,FLOAT,LONG
    }
}