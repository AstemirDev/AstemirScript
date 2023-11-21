package org.astemir.ascript.parser;

import org.astemir.ascript.core.*;
import org.astemir.ascript.core.values.Bool;

import java.util.ArrayList;
import java.util.List;

public class AScriptParser {
    private Statements statements = new Statements();
    private final List<AToken> tokens;
    private final int size;
    private int pos;
    public AScriptParser(List<AToken> tokens) {
        this.tokens = tokens;
        this.size = tokens.size();
    }

    public AStatement.Block parse(AContext root){
        return statements.root(root);
    }

    private AToken consume(ATokenType tokenType){
        final AToken current = get(0);
        if (tokenType != current.type()){
            throw new RuntimeException("Token "+current+" doesn't match required "+tokenType+".");
        }else{
            pos++;
            return current;
        }
    }
    private boolean match(ATokenType tokenType){
        final AToken current = get(0);
        if (tokenType != current.type()){
            return false;
        }else{
            pos++;
            return true;
        }
    }

    private boolean lookMatch(int relativePos,ATokenType tokenType){
        final AToken current = get(relativePos);
        if (tokenType != current.type()){
            return false;
        }else{
            return true;
        }
    }

    private AToken get(int relativePosition){
        final int position = pos+relativePosition;
        if (position >= size) return AToken.EOF;
        return tokens.get(position);
    }


    private class Statements{
        private final Expressions expressions = new Expressions();

        public AStatement.Block root(AContext context){
            final AStatement.Block result = new AStatement.Block(new ArrayList<>(),0);
            while(!match(ATokenType.EOF)){
                if (importStatements(context,result)) break;
            }
            return result;
        }

        private boolean importStatements(AContext context,AStatement.Block result) {
            if (get(0).tabs() == result.identLevel()) {
                if (match(ATokenType.IMPORT)){
                    AExpression path = expressions.expression(context);
                    AScriptFile file = new AScriptFile(path.evaluate(context).asString()+".ascript");
                    for (AStatement statement : file.getRoot().statements()) {
                        result.add(statement);
                    }
                }else {
                    AStatement statement = statement(context);
                    result.add(statement);
                }
            }else{
                return true;
            }
            return false;
        }


        private AStatement statement(AContext context){
            for (ATokenType type : AScriptLexer.STATEMENT_TOKENS) {
                if (match(type)){
                    switch (type){
                        case PRINT ->{
                            return new AStatement.Print(expressions.expression(context));
                        }
                        case PRINTLN -> {
                            return new AStatement.Println(expressions.expression(context));
                        }
                        case PASS -> {
                            return new AStatement.ExecutionPass();
                        }
                        case BREAK -> {
                            return new AStatement.ExecutionBreak();
                        }
                        case RETURN -> {
                            return new AStatement.ExecutionReturn(expressions.expression(context));
                        }
                        case CONTINUE -> {
                            return new AStatement.ExecutionContinue();
                        }
                        case IF -> {
                            return conditionIfElse(context);
                        }
                        case WHILE -> {
                            return loopWhile(context);
                        }
                        case FOR -> {
                            return loopFor(context);
                        }
                        case FUNCTION -> {
                            return newFunction(context);
                        }
                        case TYPE -> {
                            return newType(context);
                        }
                        case LOCKED -> {
                            return new AStatement.Locked(statementOrBlock(context));
                        }
                    }
                }
            }
            if (get(0).type() == ATokenType.WORD && get(1).type() == ATokenType.LEFT_PAR){
                return new AStatement.CallMethod(expressions.callFunction(context));
            }
            return assignmentStatement(context);
        }

        private AStatement statementOrBlock(AContext context){
            if (get(0).type() == ATokenType.COLON) return block(context);
            return statement(new AContext("blocklessContext",context));
        }


        private AStatement.Block block(AContext context){
            consume(ATokenType.COLON);
            final AToken current = get(0);
            final AStatement.Block result = new AStatement.Block(new ArrayList<>(),current.tabs());
            while(true){
                if (importStatements(new AContext("blockContext",context),result)) break;
            }
            return result;
        }

        private AStatement assignmentStatement(AContext context){
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1, ATokenType.EQUALS)){
                final String varName = consume(ATokenType.WORD).value();
                consume(ATokenType.EQUALS);
                return new AStatement.AssignVar(varName, expressions.expression(context));
            }
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1, ATokenType.LEFT_SQ_BRACKET)){
                AExpression.AccessArray array = expressions.accessArrayElement(context);
                consume(ATokenType.EQUALS);
                return new AStatement.AssignArray(array, expressions.expression(context));
            }
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1, ATokenType.DOT)){
                AExpression.AccessMember member = expressions.accessMember(context);
                AExpression expression = null;
                if (match(ATokenType.EQUALS)){
                    expression = expressions.expression(context);
                }
                return new AStatement.AssignMember(member,expression);
            }
            if (lookMatch(0,ATokenType.WORD) && (lookMatch(1,ATokenType.PLUSPLUS) || lookMatch(1,ATokenType.MINUSMINUS))){
                String varName = consume(ATokenType.WORD).value();
                ATokenType operator = consume(get(0).type()).type();
                return new AStatement.IncrementVar(varName, AMathOperator.fromToken(operator));
            }
            throw new RuntimeException("Unknown statement \"%s\"".formatted(get(0).type().getDefinition()));
        }

        private AStatement conditionIfElse(AContext context){
            AExpression condition = expressions.expression(context);
            AStatement ifStatement = statementOrBlock(context);
            AStatement elseStatement = null;
            if (match(ATokenType.ELSE)){
                elseStatement = statementOrBlock(context);
            }
            return new AStatement.ConditionIf(condition,ifStatement,elseStatement);
        }

        private AStatement newType(AContext context){
            String typeName = consume(ATokenType.WORD).value();
            return new AStatement.AssignType(typeName,block(context));
        }

        private AStatement loopWhile(AContext context){
            AExpression condition = expressions.expression(context);
            AStatement statement = statementOrBlock(context);
            return new AStatement.LoopWhile(condition,statement);
        }

        private AStatement loopFor(AContext context){
            AContext forContext = new AContext("for",context);
            AStatement initialization = assignmentStatement(forContext);
            consume(ATokenType.DELIM);
            AExpression termination = expressions.expression(forContext);
            consume(ATokenType.DELIM);
            AStatement increment = assignmentStatement(forContext);
            AStatement statement = statementOrBlock(forContext);
            return new AStatement.LoopFor(initialization,termination,increment,statement);
        }

        private AStatement newFunction(AContext context){
            final String name = consume(ATokenType.WORD).value();
            final AFuncArguments arguments = arguments(context);
            final AStatement statement = statementOrBlock(context);
            return new AStatement.NewFunction(name,arguments,statement);
        }

        private AFuncArguments arguments(AContext context) {
            final AFuncArguments arguments = new AFuncArguments();
            boolean startsOptionalArgs = false;
            consume(ATokenType.LEFT_PAR);
            while (!match(ATokenType.RIGHT_PAR)) {
                final String name = consume(ATokenType.WORD).value();
                if (match(ATokenType.EQUALS)) {
                    startsOptionalArgs = true;
                    arguments.addOptional(name, expressions.parseValue(context));
                } else if (!startsOptionalArgs) {
                    arguments.addRequired(name);
                } else {
                    throw new RuntimeException("Required argument cannot be after optional");
                }
                match(ATokenType.COMMA);
            }
            return arguments;
        }
    }

    private class Expressions{

        private AExpression.CallFunction callFunction(AContext context){
            final String functionName = consume(ATokenType.WORD).value();
            consume(ATokenType.LEFT_PAR);
            AExpression.CallFunction function = new AExpression.CallFunction(functionName);
            while(!match(ATokenType.RIGHT_PAR)){
                function.addArgument(expression(context));
                match(ATokenType.COMMA);
            }
            return function;
        }

        private AExpression.CreateArray createArray(AContext context){
            consume(ATokenType.LEFT_SQ_BRACKET);
            List<AExpression> expressions = new ArrayList<>();
            while (!match(ATokenType.RIGHT_SQ_BRACKET)){
                expressions.add(expression(context));
                match(ATokenType.COMMA);
            }
            return new AExpression.CreateArray(expressions);
        }

        private AExpression.AccessMember accessMember(AContext context){
            final String varName = consume(ATokenType.WORD).value();
            List<AExpression> members = new ArrayList<>();
            while(match(ATokenType.DOT)){
                AExpression member = expression(context);
                members.add(member);
            }
            return new AExpression.AccessMember(varName,members);
        }

        private AExpression.AccessArray accessArrayElement(AContext context){
            final String varName = consume(ATokenType.WORD).value();
            List<AExpression> indices = new ArrayList<>();
            do {
                consume(ATokenType.LEFT_SQ_BRACKET);
                final AExpression index = expression(context);
                consume(ATokenType.RIGHT_SQ_BRACKET);
                indices.add(index);
            }while (lookMatch(0,ATokenType.LEFT_SQ_BRACKET));
            return new AExpression.AccessArray(varName,indices);
        }

        private AExpression expression(AContext context){
            return conditionalOr(context);
        }

        private AExpression conditionalOr(AContext context){
            AExpression result = conditionalAnd(context);
            while(true) {
                if (match(ATokenType.BAR_BAR)) {
                    result = new AExpression.OperationConditional(AMathOperator.OR, result, conditionalAnd(context));
                    continue;
                }
                break;
            }
            return result;
        }

        private AExpression conditionalAnd(AContext context){
            AExpression result = conditionalEquals(context);
            while(true) {
                if (match(ATokenType.AMP_AMP)) {
                    result = new AExpression.OperationConditional(AMathOperator.AND, result, conditionalEquals(context));
                    continue;
                }
                break;
            }
            return result;
        }

        private AExpression conditionalEquals(AContext context){
            AExpression result = operationConditional(context);
            if (match(ATokenType.EQ_EQ)){
                return new AExpression.OperationConditional(AMathOperator.EQUALS,result, operationConditional(context));
            }
            if (match(ATokenType.EXCL_EQ)){
                return new AExpression.OperationConditional(AMathOperator.NOT_EQUALS,result, operationConditional(context));
            }
            return result;
        }

        private AExpression operationConditional(AContext context){
            AExpression result = operationAdditive(context);
            while(true){
                if (match(ATokenType.LT)){
                    result = new AExpression.OperationConditional(AMathOperator.LESS,result, operationAdditive(context));
                    continue;
                }
                if (match(ATokenType.LT_EQ)){
                    result = new AExpression.OperationConditional(AMathOperator.LESS_OR_EQUALS,result, operationAdditive(context));
                    continue;
                }
                if (match(ATokenType.GT)){
                    result = new AExpression.OperationConditional(AMathOperator.GREATER,result, operationAdditive(context));
                    continue;
                }
                if (match(ATokenType.GT_EQ)){
                    result = new AExpression.OperationConditional(AMathOperator.GREATER_OR_EQUALS,result, operationAdditive(context));
                    continue;
                }
                break;
            }
            return result;
        }

        private AExpression operationAdditive(AContext context){
            AExpression result = operationMultiplicative(context);
            while(true){
                if (match(ATokenType.PLUS)){
                    result = new AExpression.OperationBinary(AMathOperator.ADD,result, operationMultiplicative(context));
                    continue;
                }
                if (match(ATokenType.MINUS)){
                    result = new AExpression.OperationBinary(AMathOperator.SUB,result, operationMultiplicative(context));
                    continue;
                }
                break;
            }
            return result;
        }

        private AExpression operationMultiplicative(AContext context){
            AExpression result = operationUnary(context);
            while(true){
                if (match(ATokenType.ASTERISK)){
                    result = new AExpression.OperationBinary(AMathOperator.MULT,result, operationUnary(context));
                    continue;
                }
                if (match(ATokenType.SLASH)){
                    result = new AExpression.OperationBinary(AMathOperator.DIV,result, operationUnary(context));
                    continue;
                }
                if (match(ATokenType.CARET)){
                    result = new AExpression.OperationBinary(AMathOperator.EXP,result, operationUnary(context));
                    continue;
                }
                break;
            }
            return result;
        }

        private AExpression operationUnary(AContext context){
            if (match(ATokenType.MINUS)){
                return new AExpression.OperationUnary(AMathOperator.SUB, parseValue(context));
            }
            if (match(ATokenType.PLUS)){
                return new AExpression.OperationUnary(AMathOperator.ADD, parseValue(context));
            }
            if (match(ATokenType.PLUSPLUS)){
                return new AExpression.OperationUnary(AMathOperator.INCREMENT, parseValue(context));
            }
            if (match(ATokenType.MINUSMINUS)){
                return new AExpression.OperationUnary(AMathOperator.DECREMENT, parseValue(context));
            }
            if (match(ATokenType.EXCL)){
                return new AExpression.OperationUnary(AMathOperator.NOT, parseValue(context));
            }
            return parseValue(context);
        }

        private AExpression incrementDecrement(AExpression expression){
            if (match(ATokenType.PLUSPLUS)){
                return new AExpression.OperationUnary(AMathOperator.INCREMENT, expression);
            }else
            if (match(ATokenType.MINUSMINUS)){
                return new AExpression.OperationUnary(AMathOperator.DECREMENT, expression);
            }else{
                return expression;
            }
        }

        private AExpression parseValue(AContext context){
            final AToken current = get(0);
            if (match(ATokenType.NUMBER)){
                return incrementDecrement(new AExpression.ParseValue(context,Double.parseDouble(current.value())));
            }
            if (match(ATokenType.HEX_NUMBER)){
                return incrementDecrement(new AExpression.ParseValue(context,Long.parseLong(current.value(),16)));
            }
            if (match(ATokenType.TEXT)){
                return incrementDecrement(new AExpression.ParseValue(context,current.value()));
            }
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1,ATokenType.LEFT_PAR)){
                return incrementDecrement(callFunction(context));
            }
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1,ATokenType.LEFT_SQ_BRACKET)){
                return incrementDecrement(accessArrayElement(context));
            }
            if (lookMatch(0,ATokenType.WORD) && lookMatch(1,ATokenType.DOT)){
                return incrementDecrement(accessMember(context));
            }
            if (lookMatch(0,ATokenType.LEFT_SQ_BRACKET)){
                return incrementDecrement(createArray(context));
            }else
            if (match(ATokenType.WORD)){
                String varName = current.value();
                if (varName.equals("true") || varName.equals("false")){
                    return new AExpression.ParseValue(context, Boolean.parseBoolean(varName));
                }
                return incrementDecrement(new AExpression.GetVar(varName));
            }
            if (match(ATokenType.LEFT_PAR)){
                AExpression result = expression(context);
                match(ATokenType.RIGHT_PAR);
                return incrementDecrement(result);
            }
            throw new RuntimeException("Illegal expression "+current);
        }

    }
}
