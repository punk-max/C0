package miniplc0java.instruction;

public enum Operation {
    ILL, LIT, LOD, STO, ADD, SUB, MUL, DIV, WRT,
    nop, push, pop, popn, dup, loca, arga, globa, load8, load16, load32, load64, store8, store16, store32, store64,
    alloc, free, stackalloc, addi, subi, muli, divi, addf, subf, mulf, divf, divu, shl, shr, and, or, xor, not,
    cmpi, cmpu, cmpf, negi, negf, itof, ftoi, shrl, setlt, setgt, br, brfalse, brtrue, call, ret, callname,
    scani, scanc, scanf, printi, printc, printf, prints, println, panic;

    /*static public String toInstruction(Operation o){
        switch (o){
            case nop:
                return "00";
            case push:
                return "01";
            case pop:
                return "02";
            case popn:
                return "03";
            case dup:
                return "04";
            case loca:
                return "0a";
            case arga:
                return "0b";
            case globa:
                return "0c";
            case load8:
                return "10";
            case load16:
                return "11";
            case load32:
                return "12";
            case load64:
                return "13";
            case store8:
                return "14";
            case store16:
                return "15";
            case store32:
                return "16";
            case store64:
                return "17";
            case alloc:
                return "18";
            case free:
                return ""
        }
    }*/
}
