package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AMathOperator;
import org.astemir.ascript.core.AStatement;
import org.astemir.ascript.core.GlobalVariables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class Type implements AValue<Type> {

    private static Map<Type,TypeFactory<?>> typesMap = new HashMap<>();
    public static final Type UNDEFINED = register("TypeUndefined","Undefined",TypeFactory.create((ctx,args)->new Unspecified(ctx,args[0])));
    public static final Type NUMERIC = register("TypeNumeric","Numeric",TypeFactory.create((ctx,args)->new Numeric(ctx,args[0].asNumber())));
    public static final Type BOOLEAN = register("TypeBoolean","Boolean",TypeFactory.create((ctx,args)-> new Bool(ctx,args[0].asBoolean())));
    public static final Type TEXT = register("TypeText","Text",TypeFactory.create((ctx,args)->new Text(ctx,args[0].asString())));
    public static final Type ARRAY = register("TypeArray","Array",TypeFactory.create((ctx,args)->new Array(ctx,args)));
    public static final Type DICTIONARY = register("TypeDict","Dictionary",TypeFactory.create((ctx,args)->new Dictionary(ctx,new ConcurrentHashMap<>())));
    public static final Type LIBRARY = register("TypeLibrary","Library",TypeFactory.NO_INSTANCE);
    public static <T extends AValue> Type register(String typeName, String translation, TypeFactory<T> factory){
        Type type = GlobalVariables.globalVar(typeName,new Type(translation,new ArrayList<>()));
        typesMap.put(type,factory);
        return type;
    }

    private String typeName;
    private List<AStatement> statements;

    public Type(String typeName, List<AStatement> statements) {
        this.typeName = typeName;
        this.statements = statements;
    }

    @Override
    public Type value() {
        return this;
    }

    @Override
    public double asNumber() {
        throw new RuntimeException("Illegal operation on type");
    }

    @Override
    public boolean asBoolean() {
        throw new RuntimeException("Illegal operation on type");
    }

    @Override
    public String asString() {
        return typeName+".type";
    }

    @Override
    public String toString() {
        return typeName+".type";
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        throw new RuntimeException("Illegal operation on type");
    }

    public String typeName() {
        return typeName;
    }

    public List<AStatement> statements() {
        return statements;
    }

    public static abstract class TypeFactory<T extends AValue>{

        private boolean instantiate = true;
        public TypeFactory(boolean instantiate) {
            this.instantiate = instantiate;
        }
        abstract T create(AContext context, AValue[] args);

        public boolean canBeInstantiated() {
            return instantiate;
        }

        public static final TypeFactory create(BiFunction<AContext,AValue[],AValue> function){
            return new TypeFactory(true) {
                @Override
                AValue create(AContext context, AValue[] args) {
                    return function.apply(context,args);
                }
            };
        }
        public static final TypeFactory NO_INSTANCE = new TypeFactory<>(false){
            @Override
            AValue create(AContext context, AValue[] args) {
                return null;
            }
        };
    }
    public static AValue newInstance(AContext context,Type type,AValue[] args){
        if (typesMap.containsKey(type)){
            TypeFactory factory = typesMap.get(type);
            if (factory.canBeInstantiated()){
                return factory.create(context,args);
            }else{
                throw new RuntimeException("You can't instantiate this type \""+type+"\".");
            }
        }else{
            return new Instance(context,type);
        }
    }
}

