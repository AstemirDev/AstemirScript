package org.astemir.ascript.parser;

import java.util.ArrayList;
import java.util.List;

public class AScriptLexer {

    public static final ATokenType[] OPERATOR_TOKENS = new ATokenType[]{
            ATokenType.PLUS,
            ATokenType.MINUS,
            ATokenType.ASTERISK,
            ATokenType.SLASH,
            ATokenType.CARET,
            ATokenType.LEFT_PAR,
            ATokenType.RIGHT_PAR,
            ATokenType.EQUALS,
            ATokenType.EQ_EQ,
            ATokenType.EXCL,
            ATokenType.EXCL_EQ,
            ATokenType.LT,
            ATokenType.LT_EQ,
            ATokenType.GT,
            ATokenType.GT_EQ,
            ATokenType.AMP,
            ATokenType.AMP_AMP,
            ATokenType.BAR,
            ATokenType.BAR_BAR,
            ATokenType.COLON,
            ATokenType.DELIM,
            ATokenType.COMMA,
            ATokenType.LEFT_SQ_BRACKET,
            ATokenType.RIGHT_SQ_BRACKET,
            ATokenType.DOT,
            ATokenType.PLUSPLUS,
            ATokenType.MINUSMINUS
    };

    public static final ATokenType[] STATEMENT_TOKENS = new ATokenType[]{
            ATokenType.PRINT,
            ATokenType.PRINTLN,
            ATokenType.IF,
            ATokenType.ELSE,
            ATokenType.PASS,
            ATokenType.WHILE,
            ATokenType.FOR,
            ATokenType.BREAK,
            ATokenType.CONTINUE,
            ATokenType.FUNCTION,
            ATokenType.RETURN,
            ATokenType.TYPE,
            ATokenType.THIS,
            ATokenType.IMPORT,
            ATokenType.LOCKED
    };

    private final String input;
    private List<AToken> tokens;
    private final int length;
    private int pos;
    private int tabs;

    public AScriptLexer(String input) {
        this.input = input;
        this.length = input.length();
        this.tokens = new ArrayList<>();
    }

    public List<AToken> tokenize(){
        while(pos < length){
            ident();
            final char current = peek(0);
            if (Character.isDigit(current)){
                tokenizeNumber();
            }else
            if (Character.isLetter(current) || current == '@'){
                tokenizeWord();
            }else
            if (current == '#'){
                next();
                tokenizeHexNumber();
            }else
            if (current == '"'){
                tokenizeText();
            }else
            if (ATokenType.hasTokenDefinition(OPERATOR_TOKENS,String.valueOf(current))){
                tokenizeOperator();
            }else{
                next();
            }
        }
        return tokens;
    }

    private void ident() {
        if (tabs == 0) {
            int spaces = 0;
            char current = peek(0);
            while (true) {
                if (Character.isWhitespace(current)) {
                    if (current == ' ') {
                        spaces++;
                    } else if (current == '\t') {
                        spaces += 4;
                    }
                } else {
                    break;
                }
                current = next();
            }
            tabs = spaces / 4;
        }
        if (peek(0) == '\n') {
            tabs = 0;
        }
    }

    private void tokenizeOperator(){
        char current = peek(0);
        if (current == '/'){
            if (peek(1) == '/'){
                next();
                next();
                tokenizeComment(false);
                return;
            }else
            if (peek(1) == '*'){
                next();
                next();
                tokenizeComment(true);
                return;
            }
        }
        StringBuilder buffer = new StringBuilder();
        while (true){
            final String text = buffer.toString();
            while (!ATokenType.hasTokenDefinition(OPERATOR_TOKENS,text+current) && !text.isEmpty()){
                addToken(ATokenType.getTokenByDefinition(OPERATOR_TOKENS,text));
                return;
            }
            buffer.append(current);
            current = next();
        }
    }

    private void tokenizeComment(boolean multiline){
        char current = peek(0);
        if (multiline){
            while (true){
                if (current == '\0') throw new RuntimeException("Expected end of comment.");
                if (current == '*' && peek(1) == '/') break;
                current = next();
            }
            next();
            next();
        }else{
            while("\r\n\0".indexOf(current) == -1){
                current = next();
            }
        }
    }

    private void tokenizeText(){
        next();
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while(true){
            if (current == '\\'){
                current = next();
                switch (current){
                    case '"': current = next();buffer.append('"'); continue;
                    case 'n': current = next();buffer.append('\n'); continue;
                    case 't': current = next();buffer.append('\t'); continue;
                }
                buffer.append('\\');
                continue;
            }
            if (current == '"') break;
            buffer.append(current);
            current = next();
        }
        next();
        addToken(ATokenType.TEXT,buffer.toString());
    }
    private void tokenizeWord(){
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while(true){
            if (!Character.isLetterOrDigit(current) && current != '_' && current != '$' && current != '@'){
                break;
            }
            buffer.append(current);
            current = next();
        }
        String text = buffer.toString();
        ATokenType statementToken = ATokenType.getTokenByDefinition(STATEMENT_TOKENS,text);
        if (statementToken != null){
            addToken(statementToken);
        }else{
            addToken(ATokenType.WORD,text);
        }
    }

    private void tokenizeNumber(){
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while(true){
            if (current == '.'){
                if (buffer.indexOf(".") != -1) throw new RuntimeException("Exception too many dots in number.");
            }else
            if (!Character.isDigit(current)){
                break;
            }
            buffer.append(current);
            current = next();
        }
        addToken(ATokenType.NUMBER,buffer.toString());
    }

    private void tokenizeHexNumber(){
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while(Character.isDigit(current) || isHexNumber(current)){
            buffer.append(current);
            current = next();
        }
        addToken(ATokenType.HEX_NUMBER,buffer.toString());
    }

    private char next(){
        pos++;
        return peek(0);
    }

    private char peek(int relativePosition){
        final int position = pos+relativePosition;
        if (position >= length){
            return '\0';
        }
        return input.charAt(position);
    }

    private void addToken(ATokenType type){
        addToken(type,"");
    }

    private void addToken(ATokenType type, String text){
        tokens.add(new AToken(type, text,tabs));
    }

    private static boolean isHexNumber(char c){
        return "abcdef".indexOf(Character.toLowerCase(c)) != -1;
    }
}
