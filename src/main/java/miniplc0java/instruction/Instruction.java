package miniplc0java.instruction;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    Integer x;
    Long y;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    public Instruction(Operation opt, Long y) {
        this.opt = opt;
        this.y = y;
    }

    public Instruction() {
        this.opt = Operation.nop;
        this.x = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public String toOutString() {
        switch (this.opt) {
            case nop:
                return "00";
            case push:
                return ("01 "+ Instruction.toLexString(this.y));
            case pop:
                return "02";
            case popn:
                return ("03 " + Instruction.toLexString(this.x));
            case dup:
                return "04";
            case loca:
                return ("0a " + Instruction.toLexString(this.x));
            case arga:
                return ("0b " + Instruction.toLexString(this.x));
            case globa:
                return ("0c " + Instruction.toLexString(this.x));
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
                return "19";
            case stackalloc:
                return ("1a " + Instruction.toLexString(this.x));
            case addi:
                return "20";
            case subi:
                return "21";
            case muli:
                return "22";
            case divi:
                return "23";
            case addf:
                return "24";
            case subf:
                return "25";
            case mulf:
                return "26";
            case divf:
                return "27";
            case divu:
                return "28";
            case shl:
                return "29";
            case shr:
                return "2a";
            case and:
                return "2b";
            case or:
                return "2c";
            case xor:
                return "2d";
            case not:
                return "2e";
            case cmpi:
                return "30";
            case cmpu:
                return "31";
            case cmpf:
                return "32";
            case negi:
                return "34";
            case negf:
                return "35";
            case itof:
                return "36";
            case ftoi:
                return "37";
            case shrl:
                return "38";
            case setlt:
                return "39";
            case setgt:
                return "3a";
            case br:
                return ("41 %s" + Instruction.toLexString(this.x));
            case brfalse:
                return ("42 %s" + Instruction.toLexString(this.x));
            case brtrue:
                return ("43 %s" + Instruction.toLexString(this.x));
            case call:
                return ("48 %s" + Instruction.toLexString(this.x));
            case callname:
                return ("4a %s" + Instruction.toLexString(this.x));
            case ret:
                return "49";
            case scani:
                return "50";
            case scanc:
                return "51";
            case scanf:
                return "52";
            case printi:
                return "54";
            case printc:
                return "55";
            case printf:
                return "56";
            case prints:
                return "57";
            case println:
                return "58";
            case panic:
                return "59";
            default:
                return "ERROR";
        }
    }

    //32位操作数变y/2字节格式
    public static String toLexString(Integer x){
        String lex = String.format("%08x",x);
        StringBuffer StringBuilder=new StringBuffer(lex);
        for(int j=6;j > 0;j-=2)
            StringBuilder.insert(j," ");
        return StringBuilder.toString();
    }

    //同上，64位，用于push
    public static String toLexString(Long x){
        String lex = String.format("%016x",x);
        StringBuffer StringBuilder=new StringBuffer(lex);
        for(int j=14;j > 0;j-=2)
            StringBuilder.insert(j," ");
        return StringBuilder.toString();
    }

    //将字符串改为输出格式
    public static String toSingleString(String s){
        String ns = "";
        StringBuffer ss = new StringBuffer(ns);
        int bound = s.length()-1;
        for(int i = 0;i<=bound;i++){
            ss.append("\'"+ s.charAt(i)+"\'");
            ss.append(" ");
        }
        return ss.toString();
    }
}
