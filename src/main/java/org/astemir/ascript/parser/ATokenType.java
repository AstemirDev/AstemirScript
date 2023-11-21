package org.astemir.ascript.parser;

public enum ATokenType {


    NUMBER,
    HEX_NUMBER,
    WORD,
    TEXT,
    WHILE("while"),
    FOR("for"),
    BREAK("break"),
    CONTINUE("continue"),
    PRINT("print"),
    PRINTLN("println"),
    PASS("pass"),
    IF("if"),
    ELSE("else"),
    IMPORT("import"),
    FUNCTION("func"),
    RETURN("return"),
    TYPE("type"),
    THIS("this"),
    LOCKED("locked"),
    PLUS("+"),
    PLUSPLUS("++"),
    MINUSMINUS("--"),
    MINUS("-"),
    ASTERISK("*"),
    SLASH("/"),
    CARET("^"),
    EQUALS("="),
    LEFT_PAR("("),
    RIGHT_PAR(")"),
    LEFT_SQ_BRACKET("["),
    RIGHT_SQ_BRACKET("]"),
    LEFT_BRACKET("{"),
    RIGHT_BRACKET("}"),

    COLON(":"),
    DELIM(";"),
    COMMA(","),
    EQ_EQ("=="),
    EXCL("!"),
    EXCL_EQ("!="),
    LT("<"),
    LT_EQ("<="),
    GT(">"),
    DOT("."),
    GT_EQ(">="),
    AMP("&"),
    AMP_AMP("&&"),
    BAR("|"),
    BAR_BAR("||"),
    EOF;

    private String definition;

    ATokenType() {
        this("");
    }
    ATokenType(String definition) {
        this.definition = definition;
    }

    public static ATokenType getTokenByDefinition(ATokenType[] tokens,String definition){
        for (ATokenType value : tokens) {
            if (value.getDefinition().equals(definition)){
                return value;
            }
        }
        return null;
    }

    public static boolean hasTokenDefinition(ATokenType[] tokens,String definition){
        return getTokenByDefinition(tokens,definition) != null;
    }

    public String getDefinition() {
        return definition;
    }
}
