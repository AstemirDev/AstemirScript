package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AMathOperator;

public interface AValue<T> {

    T value();

    double asNumber();

    boolean asBoolean();

    String asString();

    AValue calculate(AMathOperator operator, AValue operand);


    static Numeric numeric(AContext context,double value){
        return new Numeric(context,value);
    }

    static Text text(AContext context,String value){
        return new Text(context,value);
    }
}
