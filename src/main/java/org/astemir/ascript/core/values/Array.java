package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.AMathOperator;
import org.astemir.ascript.core.GlobalVariables;

import java.util.ArrayList;


public class Array extends Instance<AValue[]> {
    private final AValue[] elements;

    public Array(AContext context,int size) {
        super(context, Type.ARRAY);
        this.elements = new AValue[size];
        addAll();
    }

    public Array(AContext context,AValue... values) {
        super(context, Type.ARRAY);
        this.elements = new AValue[values.length];
        System.arraycopy(values,0,elements,0,values.length);
        addAll();
    }

    private void addAll(){
        AFunction.addBuiltinFunction(context(),"size",0,(ctx,args)-> AValue.numeric(ctx,elements.length));
    }

    public AValue get(int index){
        return elements[index];
    }

    public void set(int index,AValue value){
        this.elements[index] = value;
    }

    public int size(){
        return elements.length;
    }

    @Override
    public AValue[] value() {
        return elements;
    }

    @Override
    public double asNumber() {
        AValue result = new Numeric(context(),0);
        for (AValue element : elements) {
            result = result.calculate(AMathOperator.ADD, element);
        }
        return result.asNumber();
    }

    @Override
    public String asString() {
        AValue result = new Text(context(),"");
        for (int i = 0; i < elements.length; i++) {
            if (i != elements.length -1){
                result = result.calculate(AMathOperator.ADD, new Text(context(),elements[i].asString()+","));
            }else{
                result = result.calculate(AMathOperator.ADD, new Text(context(),elements[i].asString()));
            }
        }
        return "["+result.asString()+"]";
    }

    @Override
    public boolean asBoolean() {
        boolean result = false;
        for (AValue element : elements) {
            result = element.asBoolean();
        }
        return result;
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        switch (operator){
            case ADD,SUB,MULT,DIV -> {
                if (operand instanceof Array arrayValue){
                    if (size() != arrayValue.size()) throw new RuntimeException("Array size mismatch.");
                    for (int i = 0;i<size();i++){
                        set(i,get(i).calculate(operator,arrayValue.get(i)));
                    }
                }else{
                    for (int i = 0;i<size();i++){
                        set(i,get(i).calculate(operator,operand));
                    }
                }
            }
            default -> throw new RuntimeException("Illegal operation on array.");
        }
        return this;
    }

    @Override
    public String toString() {
        return asString();
    }
}