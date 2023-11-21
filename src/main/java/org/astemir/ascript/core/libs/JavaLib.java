package org.astemir.ascript.core.libs;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.values.AValue;
import org.astemir.ascript.core.values.Numeric;
import org.astemir.ascript.core.values.Unspecified;
import org.astemir.ascript.utils.ReflectionHelper;

public class JavaLib extends Library{
    public JavaLib(AContext context) {
        super(context);
        AFunction.addBuiltinFunction(context(),"get_class",1,(ctx,args)-> new Unspecified(context, ReflectionHelper.findClass(args[0].asString())));
        AFunction.addBuiltinFunction(context(),"new_instance",1,(ctx,args)->{
            AValue className = args[0];
            Object[] otherArgs = new Object[args.length-1];
            if (otherArgs.length > 0){
                for (int i = 0;i<otherArgs.length;i++){
                    otherArgs[i] = args[i+1].value();
                }
            }
            return new Unspecified(context,ReflectionHelper.newInstance(className.asString(),otherArgs));
        });
        AFunction.addBuiltinFunction(context(),"call",1,(ctx,args)->{
            Unspecified object = (Unspecified) args[0];
            AValue methodName = args[1];
            Object[] otherArgs = new Object[args.length-2];
            if (otherArgs.length > 0){
                for (int i = 0;i<otherArgs.length;i++){
                    AValue argValue = args[i+2];
                    Object value = argValue.value();
                    otherArgs[i] = value;
                }
            }
            return new Unspecified(context,ReflectionHelper.invokeMethod(object.value(),methodName.asString(),otherArgs));
        });
        AFunction.addBuiltinFunction(context(),"call_static",1,(ctx,args)->{
            AValue className = args[0];
            AValue methodName = args[1];
            Object[] otherArgs = new Object[args.length-2];
            if (otherArgs.length > 0){
                for (int i = 0;i<otherArgs.length;i++){
                    otherArgs[i] = args[i+2].value();
                }
            }
            return new Unspecified(context,ReflectionHelper.invokeStaticMethod(className.asString(),methodName.asString(),otherArgs));
        });
        setName("java");
        setVersion("0.1");
    }
}
