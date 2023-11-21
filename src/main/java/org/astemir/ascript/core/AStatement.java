package org.astemir.ascript.core;

import org.astemir.ascript.core.values.AValue;
import org.astemir.ascript.core.values.Type;
import org.astemir.ascript.core.values.Instance;

import java.util.List;

public interface AStatement {

    AExecuteResult execute(AContext context);


    record AssignMember(AExpression.AccessMember member, AExpression expression) implements AStatement {
        @Override
        public AExecuteResult execute(AContext context) {
            AValue value = context.getVariable(member.varName());
            if (value instanceof Instance instance){
                AExecuteResult result = AExecuteResult.VOID;
                for (int i = 0;i<member().members().size();i++) {
                    AExpression member = member().members().get(i);
                    AStatement st = null;
                    if (member instanceof AExpression.AccessMember subMember){
                        st = new AssignMember(subMember,expression);
                    }
                    else
                    if (member instanceof AExpression.GetVar getVar){
                        st = new AssignVar(getVar.name(), expression);
                    }
                    else
                    if (member instanceof AExpression.CallFunction callFunction){
                        st = new CallMethod(callFunction);
                    }
                    result = st.execute(instance.context());
                }
                return result;
            }else{
                throw new RuntimeException(value+" can't contain members");
            }
        }

        @Override
        public String toString() {
            return member + "=" + expression;
        }
    }
    record AssignArray(AExpression.AccessArray array, AExpression expression) implements AStatement {
        @Override
        public AExecuteResult execute(AContext context) {
            array.getArray(context).set(array.lastIndex(context), expression.evaluate(context));
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return array + "=" + expression;
        }
    }


    record IncrementVar(String varName,AMathOperator operator) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            AValue value = context.getVariable(varName);
            switch (operator) {
                case INCREMENT: {
                    context.setVariable(varName, AValue.numeric(context,context.getVariable(varName).asNumber() + 1), true);
                    break;
                }
                case DECREMENT: {
                    context.setVariable(varName, AValue.numeric(context,context.getVariable(varName).asNumber() - 1), true);
                    break;
                }
            }
            return AExecuteResult.VOID;
        }
    }

    record AssignVar(String varName, AExpression expression) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            final AValue result = expression.evaluate(context);
            context.setVariable(varName,result,true);
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return varName+"="+expression;
        }
    }

    record Block(List<AStatement> statements, int identLevel) implements AStatement {
        public void add(AStatement statement){
            statements.add(statement);
        }

        public AStatement get(int index){
            return statements.get(index);
        }

        public boolean has(String name){
            return get(name) != null;
        }

        public AStatement get(String name){
            for (AStatement statement : statements) {
                if (statement instanceof AStatement.AssignVar var){
                    if (var.varName().equals(name)){
                        return var;
                    }
                }
                if (statement instanceof AStatement.NewFunction func){
                    if (func.name().equals(name)){
                        return func;
                    }
                }
            }
            return null;
        }

        @Override
        public AExecuteResult execute(AContext context) {
            for (AStatement statement : statements) {
                AExecuteResult stResult = statement.execute(context);
                if (stResult != AExecuteResult.VOID){
                    return stResult;
                }
            }
            return AExecuteResult.VOID;
        }

        public int identLevel() {
            return identLevel;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{\n");
            for (int i = 0;i<statements.size();i++){
                builder.append(statements.get(i));
                builder.append(";");
                if (i < statements.size()-1){
                    builder.append("\n");
                }
            }
            builder.append("\n}");
            return builder.toString();
        }
    }

    record ExecutionBreak() implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            return AExecuteResult.BREAK;
        }

        @Override
        public String toString() {
            return "break";
        }
    }

    record ExecutionContinue() implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            return AExecuteResult.CONTINUE;
        }

        @Override
        public String toString() {
            return "continue";
        }
    }


    record ExecutionPass() implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return "pass";
        }
    }


    record ExecutionReturn(AExpression expression) implements AStatement {
        @Override
        public AExecuteResult execute(AContext context) {
            return AExecuteResult.returnResult(expression.evaluate(context));
        }

        @Override
        public String toString() {
            return "return "+expression;
        }
    }

    record AssignType(String name, AStatement.Block content) implements AStatement{

        @Override
        public AExecuteResult execute(AContext context) {
            context.setVariable(name,new Type(name,content.statements()),true);
            return AExecuteResult.VOID;
        }
    }

    record LoopWhile(AExpression condition, AStatement statement) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            while (condition.evaluate(context).asNumber() != 0){
                AExecuteResult res = statement.execute(context);
                if (res == AExecuteResult.BREAK){
                    break;
                }else
                if (res == AExecuteResult.CONTINUE){
                    //continue
                }else
                if (res != AExecuteResult.VOID){
                    return res;
                }
            }
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return "while "+condition+" "+statement;
        }
    }

    record LoopFor(AStatement initialization, AExpression termination, AStatement increment, AStatement statement) implements AStatement {
        @Override
        public AExecuteResult execute(AContext context) {
            for (initialization.execute(context); termination.evaluate(context).asNumber() != 0; increment.execute(context)){
                AExecuteResult res = statement.execute(context);
                if (res == AExecuteResult.BREAK){
                    break;
                }else
                if (res == AExecuteResult.CONTINUE){
                    //continue;
                }else
                if (res != AExecuteResult.VOID){
                    return res;
                }
            }
            return AExecuteResult.VOID;
        }


        @Override
        public String toString() {
            return "for ("+initialization + ";" + termination + ";" + increment + ") " + statement;
        }
    }


    record CallMethod(AExpression.CallFunction expression) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            expression.evaluate(context);
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return expression.toString();
        }
    }


    record ConditionIf(AExpression expression, AStatement ifStatement, AStatement elseStatement) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            final double result = expression.evaluate(context).asNumber();
            if (result != 0){
                AExecuteResult res = ifStatement.execute(context);
                if (res != AExecuteResult.VOID){
                    return res;
                }
            }else{
                if (elseStatement != null){
                    AExecuteResult res = elseStatement.execute(context);
                    if (res != AExecuteResult.VOID){
                        return res;
                    }
                }
            }
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            if (elseStatement != null) {
                return "if " + expression + " " + ifStatement + " else " + elseStatement;
            }else{
                return "if " + expression + " " + ifStatement;
            }
        }
    }

    record Locked(AStatement statement) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            AExecuteResult res = statement.execute(context);
            return res;
        }


        @Override
        public String toString() {
            return "locked {"+statement+"}";
        }
    }


    record NewFunction(String name, AFuncArguments args, AStatement body) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            context.setFunction(name,new AFunction.New(args,body),true);
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return name + "(" + args + ")" + body;
        }
    }

    record Print(AExpression expression) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            System.out.print(expression.evaluate(context));
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return "print "+expression;
        }
    }

    record Println(AExpression expression) implements AStatement {

        @Override
        public AExecuteResult execute(AContext context) {
            if (expression != null) {
                System.out.println(expression.evaluate(context));
            }else{
                System.out.println();
            }
            return AExecuteResult.VOID;
        }

        @Override
        public String toString() {
            return "print "+expression;
        }
    }
}
