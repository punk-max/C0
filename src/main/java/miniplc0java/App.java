package miniplc0java;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.GlobalEntry;
import miniplc0java.analyser.Type;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Function;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Tokenizer;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws CompileError {
        var inputFileName = args[0];
        var outputFileName = args[1];

        File in = new File(inputFileName);
        File out = new File(outputFileName);
        InputStream input;
        OutputStream output;
            try {
                input = new FileInputStream(in);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }

            try {
                output = new FileOutputStream(out);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);
        var analyser = new Analyser(tokenizer);

        analyser.analyse();

        String str = "";
        String magic  = "72 30 3b 3e\n";
        String version = "00 00 00 01\n";
        str = magic + version;
        List<Function> functions = analyser.getFuncs();
        int funCount = functions.size();
        List<GlobalEntry> globalEntries = analyser.getGlobalTable();
        int globalcount = globalEntries.size();
        String globalCount = Instruction.toLexString(globalcount) + "\n";
        str+=globalCount;
        for(GlobalEntry item : globalEntries)
        {
            if(item.isConstant())
                str+="01\n";
            else
                str+="00\n";
            if(item.isFunc())
            {
                String s = item.getValue().toString();
                int a = s.length();
                str+=Instruction.toLexString(a)+"\n";
                str+=Instruction.toSingleString(s)+"\n";
            }
            else
            {
                if(item.getType()== Type.Double)
                {
                    str+=Instruction.toLexString(8)+"\n";
                    str += Instruction.toLexString((long)item.getValue())+"\n";
                }
                else if(item.getType() == Type.Int)
                {
                    str+=Instruction.toLexString(4)+"\n";
                    str += Instruction.toLexString((int)item.getValue())+"\n";
                }
            }
        }

        for(Function item:functions){
            str+=item.toOutString();
        }

        try {
            output.write(str.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
