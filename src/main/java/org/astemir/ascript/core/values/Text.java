package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.AMathOperator;

public class Text extends Instance<String> {

    private String value;

    public Text(AContext context,String value) {
        super(context, Type.TEXT);
        this.value = value;
        AFunction.addBuiltinFunction(context(),"length",0,(ctx, args)-> AValue.numeric(ctx,value.length()));
        AFunction.addBuiltinFunction(context(),"split",1,(ctx,args)->{
            String[] split = asString().split(args[0].asString());
            AValue[] splitValues = new AValue[split.length];
            for (int i = 0; i < split.length; i++) {
                splitValues[i] = new Text(ctx,split[i]);
            }
            return new Array(ctx,splitValues);
        });
        AFunction.addBuiltinFunction(context(),"char_at",1,(ctx,args)->{
            AValue indexValue = args[0];
            return new Text(ctx,String.valueOf(asString().charAt((int) indexValue.asNumber())));
        });
        AFunction.addBuiltinFunction(context(),"set_char",2,(ctx,args)->{
            AValue indexValue = args[0];
            return setChar((int) indexValue.asNumber(),args[1].asString());
        });

        AFunction.addBuiltinFunction(context(),"sub",1, AFunction.CheckArgsMode.MORE_THAN,(ctx,args)->{
            if (args.length == 1) {
                AValue indexValue = args[0];
                return new Text(ctx, asString().substring((int) indexValue.asNumber()));
            }else
            if (args.length == 2){
                AValue a = args[0];
                AValue b = args[1];
                return new Text(ctx, asString().substring((int) a.asNumber(), (int) b.asNumber()));
            }
            return new Text(ctx,asString());
        });
        AFunction.addBuiltinFunction(context(),"format",1, AFunction.CheckArgsMode.MORE_THAN,(ctx,args)-> new Text(ctx,asString().formatted(args)));
    }

    @Override
    public String value() {
        return asString();
    }

    @Override
    public double asNumber() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean asBoolean() {
        return value.equals("true") ? true : false;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        switch (operator) {
            case ADD: {
                return new Text(context(),value + operand.asString());
            }
            case SUB: {
                return new Text(context(),subString(value, operand.asString()));
            }
            case MULT: {
                if (operand instanceof Numeric) {
                    return new Text(context(),value.repeat((int) operand.asNumber()));
                }
            }
            case DIV: {
                if (operand instanceof Numeric) {
                    return new Text(context(),divString(value, (int) operand.asNumber()));
                }
            }
            default:
                throw new RuntimeException("Illegal operator %s!".formatted(operator));
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    public Text setChar(int index, String character) {
        if (index < 0 || index >= value.length()) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        String newString = value.substring(0, index) + character + value.substring(index + 1);
        return new Text(context(),newString);
    }

    public static String divString(String str, int division) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (i % division == 0) {
                builder.append(str.charAt(i));
            }
        }
        return builder.toString();
    }

    public static String subString(String str, String subStr) {
        int index = str.indexOf(subStr);
        if (index == -1) {
            return str;
        }
        String prefix = str.substring(0, index);
        String suffix = str.substring(index + subStr.length());
        return prefix + suffix;
    }
}
