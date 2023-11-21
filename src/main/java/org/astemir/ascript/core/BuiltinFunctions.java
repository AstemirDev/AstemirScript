package org.astemir.ascript.core;

import org.astemir.ascript.core.libs.JavaLib;
import org.astemir.ascript.core.libs.MathLib;
import org.astemir.ascript.core.values.*;
import org.astemir.ascript.utils.ReflectionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BuiltinFunctions {

    private static Map<String,AFunction> builtinFunctions = new HashMap<>();

    static {
        builtinFunc("int",1,(args,context)-> AValue.numeric(context,args[0].asNumber()).format(Numeric.Format.INT));
        builtinFunc("float",1,(args,context)-> AValue.numeric(context,args[0].asNumber()).format(Numeric.Format.FLOAT));
        builtinFunc("long",1,(args,context)-> AValue.numeric(context,args[0].asNumber()).format(Numeric.Format.LONG));
        builtinFunc("double",1,(args,context)-> AValue.numeric(context,args[0].asNumber()).format(Numeric.Format.DOUBLE));
        builtinFunc("str",1,(args,context)->AValue.text(context,args[0].value().toString()));
        builtinFunc("typeof",1,(args,context)->{
            AValue value = args[0];
            if (value instanceof Instance instance){
                return instance.type();
            }
            throw new RuntimeException("This value is too primitive.");
        });
        builtinFunc("array", 1, new BiFunction<>() {
            @Override
            public AValue apply(AValue[] args,AContext context) {
                return createArray(args, context,0);
            }
            private Array createArray(AValue[] args, AContext context, int index) {
                final int size = (int) args[index].asNumber();
                final int last = args.length - 1;
                Array array = new Array(context,size);
                if (index == last) {
                    for (int i = 0; i < size; i++) {
                        array.set(i, Numeric.ZERO);
                    }
                } else if (index < last) {
                    for (int i = 0; i < size; i++) {
                        array.set(i, createArray(args, context,index + 1));
                    }
                }
                return array;
            }
        });
        builtinFunc("lib",1,(args,context)->{
            String name = args[0].asString();
            switch (name){
                case "math": return new MathLib(context);
                case "java": return new JavaLib(context);
            }
            throw new RuntimeException("Illegal library name!");
        });
    }

    private static void builtinFunc(String name, int argsMinCount, BiFunction<AValue[],AContext,AValue> function){
        builtinFunctions.put(name, (context,args) -> {
            checkLength(args.length,argsMinCount);
            return function.apply(args,context);
        });
    }

    private static void builtinMethod(String name, int argsMinCount, BiConsumer<AValue[],AContext> consumer) {
        builtinFunctions.put(name, (context,args) -> {
            checkLength(args.length, argsMinCount);
            consumer.accept(args,context);
            return Numeric.ZERO;
        });
    }

    private static void checkLength(int length,int requiredLength){
        if (length < requiredLength){
            throw new RuntimeException("Atleast "+requiredLength+" arguments required");
        }else
        if (length > requiredLength){
            throw new RuntimeException("This function requires "+requiredLength+" arguments.");
        }
    }

    public static Map<String, AFunction> getBuiltinFunctions() {
        return builtinFunctions;
    }
}
