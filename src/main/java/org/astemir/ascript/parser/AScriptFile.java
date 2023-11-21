package org.astemir.ascript.parser;

import org.astemir.Main;
import org.astemir.ascript.core.AContext;
import org.astemir.ascript.core.AStatement;
import org.astemir.ascript.core.GlobalVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AScriptFile {

    private AStatement.Block root;
    private AContext context;
    public AScriptFile(String path) {
        AScriptLexer lexer = new AScriptLexer(readFile(getResource(path)));
        AScriptParser parser = new AScriptParser(lexer.tokenize());
        this.context = new AContext(path,AContext.GLOBAL_CONTEXT);
        this.root = parser.parse(context);
    }

    public void run(){
        root.execute(context);
    }

    public AStatement.Block getRoot() {
        return root;
    }

    private static InputStream getResource(String path){
        return Main.class.getClassLoader().getResourceAsStream(path);
    }

    private static String readFile(InputStream inputStream){
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
