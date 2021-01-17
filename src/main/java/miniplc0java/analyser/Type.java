package miniplc0java.analyser;

import miniplc0java.tokenizer.Token;

public enum Type {
    Int,Void,Double;

    public static Type check(Token t){
        String str = t.getValue().toString();
        switch (str){
            case "int":
                return Type.Int;
            case "void":
                return Type.Void;
            default:
                return Type.Double;
        }
    }
}
