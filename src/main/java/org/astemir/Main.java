package org.astemir;

import org.astemir.ascript.parser.AScriptFile;


public class Main{

    public static void main(String[] args) {
        AScriptFile test = new AScriptFile("test.ascript");
        test.run();
    }
}
