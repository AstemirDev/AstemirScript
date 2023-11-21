package org.astemir.ascript.core.libs;

import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AFunction;
import org.astemir.ascript.core.GlobalVariables;
import org.astemir.ascript.core.values.AValue;
import org.astemir.ascript.core.values.Numeric;

public class MathLib extends Library{
    public MathLib(AContext context) {
        super(context);
        AFunction.addBuiltinFunction(context(),"abs",1,(ctx,args)->AValue.numeric(context,Math.abs(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"ceil",1,(ctx,args)->AValue.numeric(context,Math.ceil(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"round",1,(ctx,args)->AValue.numeric(context,Math.round(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"sin",1,(ctx,args)->AValue.numeric(context,Math.sin(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"cos",1,(ctx,args)->AValue.numeric(context,Math.cos(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"tan",1,(ctx,args)->AValue.numeric(context,Math.tan(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"asin",1,(ctx,args)->AValue.numeric(context,Math.asin(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"acos",1,(ctx,args)->AValue.numeric(context,Math.acos(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"atan",1,(ctx,args)->AValue.numeric(context,Math.atan(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"floor",1,(ctx,args)->AValue.numeric(context,Math.floor(args[0].asNumber())));
        AFunction.addBuiltinFunction(context(),"atan2",2,(ctx,args)->AValue.numeric(context,Math.atan2(args[0].asNumber(),args[1].asNumber())));
        AFunction.addBuiltinFunction(context(),"random",0,(ctx,args)->AValue.numeric(context,Math.random()));
        AFunction.addBuiltinFunction(context(),"random_int",0,(ctx,args)->{
            double min = args[0].asNumber();
            double max = args[1].asNumber();
            return AValue.numeric(context,min + (int)(Math.random() * ((max - min) + 1)));
        });
        AFunction.addBuiltinFunction(context(),"random_double",0,(ctx,args)->{
            double min = args[0].asNumber();
            double max = args[1].asNumber();
            return AValue.numeric(context,Math.min(max, min + (float)(Math.random() * ((max - min) + 1.0f))));
        });
        AFunction.addBuiltinFunction(context(),"pow",2,(ctx,args)->AValue.numeric(context,Math.pow(args[0].asNumber(),args[1].asNumber())));
        AFunction.addBuiltinFunction(context(),"max",2,(ctx,args)->AValue.numeric(context,Math.max(args[0].asNumber(),args[1].asNumber())));
        AFunction.addBuiltinFunction(context(),"min",2,(ctx,args)->AValue.numeric(context,Math.min(args[0].asNumber(),args[1].asNumber())));
        context().setVariable("PI",new Numeric(AContext.GLOBAL_CONTEXT,Math.PI),false);
        context().setVariable("E",new Numeric(AContext.GLOBAL_CONTEXT,Math.E),false);
        context().setVariable("PI_2",new Numeric(AContext.GLOBAL_CONTEXT,Math.PI*2),false);
        context().setVariable("HALF_PI",new Numeric(AContext.GLOBAL_CONTEXT,Math.PI/2),false);
        setName("math");
        setVersion("0.1");
    }

}
