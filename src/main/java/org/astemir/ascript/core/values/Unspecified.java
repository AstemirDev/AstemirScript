package org.astemir.ascript.core.values;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.AMathOperator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class Unspecified extends Instance<Object> {

    private Object value;

    public Unspecified(AContext context,Object value) {
        super(context,Type.UNDEFINED);
        this.value = value;
        if (value != null) {
            if (value instanceof Instance instance) {
                for (Map.Entry<String, AFunction> entry : instance.context().getFunctions().entrySet()) {
                    context().setFunction(entry.getKey(), entry.getValue(), false);
                }
                for (Map.Entry<String, AValue> entry : instance.context().getVariables().entrySet()) {
                    context().setVariable(entry.getKey(), entry.getValue(), false);
                }
            } else {
                for (Method method : value.getClass().getMethods()) {
                    AFunction.addBuiltinFunction(context(), method.getName(), method.getParameterCount(), (ctx, args) -> {
                        try {
                            Object[] invokeArguments = new Object[args.length];
                            for (int i = 0; i < args.length; i++) {
                                invokeArguments[i] = args[i].value();
                            }
                            return new Unspecified(ctx, method.invoke(value, invokeArguments));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public double asNumber() {
        if (value instanceof Instance instance) {
            return instance.asNumber();
        }
        return 0;
    }

    @Override
    public boolean asBoolean() {
        if (value instanceof Instance instance) {
            return instance.asBoolean();
        }
        return false;
    }

    @Override
    public String asString() {
        return "object("+value+")";
    }


    @Override
    public AValue calculate(AMathOperator operator, AValue operand) {
        if (value instanceof Numeric) {
            return new Numeric(context(),asNumber()).calculate(operator, operand);
        }
        if (value instanceof String) {
            return new Text(context(),value.toString()).calculate(operator, operand);
        }
        throw new RuntimeException("Invalid operation on %s".formatted(asString()));
    }
}
