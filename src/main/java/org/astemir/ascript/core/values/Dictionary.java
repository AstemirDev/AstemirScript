package org.astemir.ascript.core.values;


import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.AMathOperator;
import org.astemir.ascript.core.GlobalVariables;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dictionary extends Instance<Map<AValue,AValue>> {
    private final ConcurrentHashMap<AValue,AValue> elements;

    public Dictionary(AContext context,ConcurrentHashMap<AValue, AValue> elements) {
        super(context, Type.DICTIONARY);
        this.elements = elements;
        AFunction.addBuiltinFunction(context(),"put",2,(ctx,args)->{
            set(args[0],args[1]);
            return Numeric.ZERO;
        });
        AFunction.addBuiltinFunction(context(),"get",1,(ctx,args)-> get(args[0]));
        AFunction.addBuiltinFunction(context(),"remove",1,(ctx,args)-> remove(args[0]));

        AFunction.addBuiltinFunction(context(),"size",0,(ctx,args)-> AValue.numeric(ctx,size()));
    }

    public AValue get(AValue index){
        for (Map.Entry<AValue, AValue> entry : elements.entrySet()) {
            if (entry.getKey().value().equals(index.value())){
                return entry.getValue();
            }
        }
        return null;
    }

    public AValue remove(AValue index){
        for (Map.Entry<AValue, AValue> entry : elements.entrySet()) {
            if (entry.getKey().value().equals(index.value())){
                elements.remove(entry.getKey());
            }
        }
        return null;
    }

    public void set(AValue index,AValue value){
        this.elements.put(index,value);
    }

    public int size(){
        return elements.size();
    }

    @Override
    public Map<AValue, AValue> value() {
        return elements;
    }

    @Override
    public double asNumber() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return elements.toString();
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
        return asString();
    }
}