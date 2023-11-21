package org.astemir.ascript.parser;

public record AToken(ATokenType type, String value,int tabs) {

    public static final AToken EOF = new AToken(ATokenType.EOF,"",0);


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("type=");
        sb.append(type);
        if (value != ""){
            sb.append(", ");
            sb.append("value=");
            sb.append(value);
        }
        sb.append(")");
        return sb.toString();
    }
}
