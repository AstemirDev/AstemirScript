package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AMathOperator;
import org.astemir.ascript.core.AStatement;



public class Instance<T> implements AValue<T> {

    private AContext context;
    private Type type;

    public Instance(AContext context, Type type) {
        this.type = type;
        this.context = new AContext("Instance("+type+")",context);
    }

    public Instance load(){
        for (AStatement statement : this.type.statements()) {
            statement.execute(context());
        }
        return this;
    }

    @Override
    public T value() {
        return (T) this;
    }

    @Override
    public double asNumber() {
        if (context().isFunctionExist("to_num")){
            return context().getFunction("to_num").execute(context()).asNumber();
        }
        return 0;
    }

    @Override
    public boolean asBoolean() {
        if (context().isFunctionExist("to_bool")){
            return context().getFunction("to_bool").execute(context()).asBoolean();
        }
        return false;
    }

    @Override
    public String asString() {
        if (context().isFunctionExist("to_str")){
            return context().getFunction("to_str").execute(context()).asString();
        }
        return type().typeName();
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        if (context().isFunctionExist("operator_"+operator)){
            return context().getFunction("operator_"+ operator).execute(context(),operand);
        }
        throw new RuntimeException("Math operators not implemented in this type.");
    }

    public AContext context() {
        return context;
    }

    public Type type() {
        return type;
    }
}