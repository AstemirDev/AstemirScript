package org.astemir.ascript.core;

import org.astemir.ascript.core.libs.Library;
import org.astemir.ascript.core.values.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface AExpression{

    AValue evaluate(AContext context);

    class ParseValue implements AExpression {

        public final AValue value;

        public ParseValue(AContext context,double value) {
            this.value = new Numeric(context,value);
        }
        public ParseValue(AContext context,boolean value) {
            this.value = new Bool(context,value);
        }
        public ParseValue(AContext context, String value) {
            this.value = new Text(context,value);
        }

        @Override
        public AValue evaluate(AContext context) {
            return value;
        }
        @Override
        public String toString() {
            return value.asString();
        }
    }

     record GetVar(String name) implements AExpression {

        @Override
        public AValue evaluate(AContext context) {
            return context.getVariable(name);
        }

        @Override
        public String toString() {
            return name;
        }
     }


    record CreateArray(List<AExpression> elements) implements AExpression {

        @Override
        public AValue evaluate(AContext context) {
            final int size = elements.size();
            final Array arrayValue = new Array(context,size);
            for (int i = 0; i < size; i++) {
                arrayValue.set(i, elements.get(i).evaluate(context));
            }
            return arrayValue;
        }

        @Override
        public String toString() {
            return "[" + elements + "]";
        }
    }

    record AccessMember(String varName, List<AExpression> members) implements AExpression {

        @Override
        public AValue evaluate(AContext context) {
            AValue value = context.getVariable(varName);
            if (value instanceof Instance instance){
                for (int i = 0; i < members.size(); i++) {
                    AExpression member = members.get(i);
                    value = member.evaluate(instance.context());
                    if (value instanceof Instance){
                        instance = (Instance) value;
                    }
                }
                return value;
            }
            throw new RuntimeException(value+" can't contain members");
        }

        @Override
        public String toString() {
            return varName + "." + members + "";
        }
    }

    record AccessArray(String varName, List<AExpression> indices) implements AExpression {

        @Override
        public AValue evaluate(AContext context) {
            return getArray(context).get(lastIndex(context));
        }

        public Array getArray(AContext context) {
            Array array = consumeArray(context.getVariable(varName));
            final int last = indices.size() - 1;
            for (int i = 0; i < last; i++) {
                array = consumeArray(array.get(index(context,i)));
            }
            return array;
        }

        public int lastIndex(AContext context) {
            return index(context,indices.size() - 1);
        }

        public int index(AContext context,int index) {
            return (int) indices.get(index).evaluate(context).asNumber();
        }

        private Array consumeArray(AValue value) {
            if (value instanceof Array arrayValue) {
                return arrayValue;
            } else {
                throw new RuntimeException("Expected array but found " + value);
            }
        }

        @Override
        public String toString() {
            return varName + "[" + indices + "]";
        }
    }


    class OperationUnary implements AExpression {

        public AExpression operand;
        private AMathOperator operator;

        public OperationUnary(AMathOperator operator, AExpression operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public AValue evaluate(AContext context) {
            double value = operand.evaluate(context).asNumber();
            switch (operator){
                case ADD: return new Numeric(context,value);
                case SUB: return new Numeric(context,-value);
                case NOT: return new Numeric(context,value == 1 ? false : true);
                case INCREMENT: return new Numeric(context,value + 1);
                case DECREMENT: return new Numeric(context,value -1);
                default: throw new RuntimeException("Illegal operator %s!".formatted(operator));
            }
        }

        @Override
        public String toString() {
            return operator+""+operand;
        }
    }

    class OperationBinary implements AExpression {

        public AExpression leftOperand;
        public AExpression rightOperand;
        private AMathOperator operator;

        public OperationBinary(AMathOperator operator, AExpression leftOperand, AExpression rightOperand) {
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        @Override
        public AValue evaluate(AContext context) {
            return leftOperand.evaluate(context).calculate(operator,rightOperand.evaluate(context));
        }

        @Override
        public String toString() {
            return "["+leftOperand+""+operator+""+rightOperand+"]";
        }
    }


    class OperationConditional implements AExpression {

        public AExpression leftOperand;
        public AExpression rightOperand;
        private AMathOperator operator;

        public OperationConditional(AMathOperator operator, AExpression leftOperand, AExpression rightOperand) {
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        @Override
        public AValue evaluate(AContext context) {
            AValue value1 = leftOperand.evaluate(context);
            AValue value2 = rightOperand.evaluate(context);
            double double1, double2;
            if (value1 instanceof Text){
                double1 = value1.asString().compareTo(value1.asString());
                double2 = 0;
            }else{
                double1 = value1.asNumber();
                double2 = value2.asNumber();
            }
            boolean result;
            switch (operator){
                case EQUALS: result = double1 == double2;break;
                case NOT_EQUALS: result = double1 != double2;break;
                case LESS: result = double1 < double2;break;
                case LESS_OR_EQUALS: result = double1 <= double2;break;
                case GREATER: result = double1 > double2;break;
                case GREATER_OR_EQUALS: result = double1 >= double2;break;
                case AND: result = (double1 != 0) && (double2 != 0);break;
                case OR: result = (double1 != 0) || (double2 != 0);break;
                default: throw new RuntimeException("Illegal operator %s!".formatted(operator));
            }
            return new Numeric(context,result);
        }

        @Override
        public String toString() {
            return "["+leftOperand+""+operator+""+rightOperand+"]";
        }
    }

    class CallFunction implements AExpression{

        private final String name;
        public final List<AExpression> arguments;

        public CallFunction(String name) {
            this.name = name;
            this.arguments = new ArrayList<>();
        }

        public void addArgument(AExpression arg){
            this.arguments.add(arg);
        }

        @Override
        public AValue evaluate(AContext context) {
            AValue[] values = new AValue[arguments.size()];
            if (context.isVariableExist(name)){
                return createObject(context,values);
            }
            return callFunction(context,values);
        }

        private AValue callFunction(AContext context,AValue[] values){
            int size = arguments.size();
            for (int i = 0; i < size; i++) {
                values[i] = arguments.get(i).evaluate(context);
            }
            AFunction function = context.getFunction(name);
            return tryExecuteFunc(context,values,size,function);
        }

        private AValue tryExecuteFunc(AContext context,AValue[] values,int size,AFunction function){
            if (function instanceof AFunction.New functionDefinition){
                AContext functionContext = new AContext(name,context);
                if (size != functionDefinition.argsCount()) throw new RuntimeException("Mismatched arguments count.");
                for (int i = 0; i < size; i++) {
                    functionContext.setVariable(functionDefinition.argName(i),values[i],true);
                }
                return functionDefinition.execute(functionContext,values);
            }
            return function.execute(context,values);
        }

        private AValue createObject(AContext context,AValue[] values){
            int size = arguments.size();
            for (int i = 0; i < size; i++) {
                values[i] = arguments.get(i).evaluate(context);
            }
            AValue var = context.getVariable(name);
            if (var instanceof Type typeVar){
                Instance instance = (Instance) Type.newInstance(context,typeVar,values);
                instance.load();
                AContext instanceContext = instance.context();
                if (instanceContext.isFunctionExist("init")){
                    tryExecuteFunc(instanceContext,values,size,instanceContext.getFunction("init"));
                }
                return instance;
            }

            throw new RuntimeException("\"name\" is not callable type.");
        }

        @Override
        public String toString() {
            return name+"("+arguments+")";
        }
    }
}
