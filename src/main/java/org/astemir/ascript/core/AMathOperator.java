package org.astemir.ascript.core;

import org.astemir.ascript.parser.ATokenType;

public enum AMathOperator {

    ADD("+",ATokenType.PLUS),
    SUB("-",ATokenType.MINUS),
    MULT("*",ATokenType.ASTERISK),
    DIV("/",ATokenType.SLASH),
    EXP("^",ATokenType.CARET),
    EQUALS("==",ATokenType.EQ_EQ),
    NOT_EQUALS("!=",ATokenType.EXCL_EQ),
    NOT("!",ATokenType.EXCL),
    LESS("<",ATokenType.LT),
    GREATER(">",ATokenType.GT),
    LESS_OR_EQUALS("<=",ATokenType.LT_EQ),
    GREATER_OR_EQUALS(">=",ATokenType.GT_EQ),
    AND("&&",ATokenType.AMP_AMP),
    OR("||",ATokenType.BAR_BAR),
    INCREMENT("++",ATokenType.PLUSPLUS),
    DECREMENT("--",ATokenType.MINUSMINUS),

    ;


    private String text;

    private ATokenType token;

    AMathOperator(String text,ATokenType token) {
        this.text = text;
        this.token = token;
    }

    public static AMathOperator fromToken(ATokenType type){
        for (AMathOperator value : values()) {
            if (value.getToken() == type){
                return value;
            }
        }
        throw new RuntimeException("Invalid operator type.");
    }

    public ATokenType getToken() {
        return token;
    }

    @Override
    public String toString() {
        return text;
    }
}
