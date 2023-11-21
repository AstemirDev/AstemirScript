package org.astemir.ascript.core;

import org.astemir.ascript.core.values.AValue;

import java.util.HashMap;
import java.util.Map;

public class AContext {

    public static final AContext GLOBAL_CONTEXT = new AContext("global",null);
    private Map<String, AValue> variables = new HashMap<>();
    private Map<String, AFunction> functions = new HashMap<>();
    private AContext previous;
    private String contextName;

    public AContext(String contextName,AContext previous) {
        this.contextName = contextName;
        this.previous = previous;
    }

    public void setVariable(String name, AValue value, boolean deep) {
        if (deep && previous != null && previous.isVariableExist(name)) {
            previous.setVariable(name, value, true);
        } else {
            if (isVariableExist(name)) {
                variables.replace(name, value);
            } else {
                variables.put(name, value);
            }
        }
    }


    public void setFunction(String name, AFunction function, boolean deep) {
        if (deep && previous != null && previous.isFunctionExist(name)) {
            previous.setFunction(name, function, true);
        } else {
            if (isVariableExist(name)) {
                functions.replace(name, function);
            } else {
                functions.put(name, function);
            }
        }
    }

    public AValue getVariable(String name) {
        AValue value = variables.get(name);
        if (value != null) {
            return value;
        } else if (previous != null) {
            return previous.getVariable(name);
        } else {
            if (GlobalVariables.getGlobalVariables().containsKey(name)) {
                return GlobalVariables.getGlobalVariables().get(name);
            }else{
                throw new RuntimeException("Variable named \""+name+"\" doesn't exist");
            }
        }
    }

    public AFunction getFunction(String name) {
        AFunction value = functions.get(name);
        if (value != null) {
            return value;
        } else if (previous != null) {
            return previous.getFunction(name);
        } else {
            if (BuiltinFunctions.getBuiltinFunctions().containsKey(name)) {
                return BuiltinFunctions.getBuiltinFunctions().get(name);
            }else{
                throw new RuntimeException("Function named \""+name+"\" doesn't exist");
            }
        }
    }

    public boolean isVariableExist(String name) {
        AValue value = variables.get(name);
        if (value != null) {
            return true;
        } else if (previous != null) {
            return previous.isVariableExist(name);
        } else {
            if (GlobalVariables.getGlobalVariables().containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFunctionExist(String name) {
        AFunction value = functions.get(name);
        if (value != null) {
            return true;
        } else if (previous != null) {
            return previous.isFunctionExist(name);
        } else {
            if (BuiltinFunctions.getBuiltinFunctions().containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return contextName;
    }

    public Map<String, AValue> getVariables() {
        return variables;
    }

    public AContext getPrevious() {
        return previous;
    }

    public Map<String, AFunction> getFunctions() {
        return functions;
    }

    public String getContextName() {
        return contextName;
    }
}