package org.astemir.ascript.core;

import org.astemir.ascript.core.values.AValue;
import org.astemir.ascript.core.values.Numeric;

public interface AFunction {

    AValue execute(AContext context, AValue... args);

    static void addBuiltinFunction(AContext context,String name,int argsCount,CheckArgsMode mode,AFunction function) {
        context.setFunction(name, (ctx, args) -> {
            mode.check(name, args.length, argsCount);
            return function.execute(context, args);
        }, false);
    }

    static void addBuiltinFunction(AContext context,String name,int argsCount,AFunction function) {
        addBuiltinFunction(context,name,argsCount,CheckArgsMode.STRICT,function);
    }


    abstract class CheckArgsMode{

        public static final CheckArgsMode STRICT = new CheckArgsMode() {
            @Override
            void check(String name, int length, int requiredLength) {
                if (length < requiredLength){
                    throw new RuntimeException("Function \""+name+"\" requires "+requiredLength+" arguments.");
                }else
                if (length > requiredLength){
                    throw new RuntimeException("Function \""+name+"\" has too many arguments, required "+requiredLength+".");
                }
            }
        };

        public static final CheckArgsMode MORE_THAN = new CheckArgsMode() {
            @Override
            void check(String name, int length, int requiredLength) {
                if (length < requiredLength){
                    throw new RuntimeException("Function \""+name+"\" requires "+requiredLength+" or more arguments.");
                }
            }
        };

        public static final CheckArgsMode ANY = new CheckArgsMode() {
            @Override
            void check(String name, int length, int requiredLength) {}
        };

        abstract void check(String name,int length,int requiredLength);
    }

    record New(AFuncArguments args, AStatement body) implements AFunction{

        public int argsCount(){
            return args.size();
        }

        public String argName(int index){
            if (index < 0 || index >= argsCount()) return "";
            return args.get(index).name();
        }

        @Override
        public AValue execute(AContext context,AValue... args) {
            AExecuteResult result = body.execute(context);
            if (result.getValue() != null) {
                return result.getValue();
            }
            return Numeric.ZERO;
        }
    }
}
