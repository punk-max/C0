package miniplc0java.instruction;

import java.util.List;

public class Function {
    int name;
    int ret;
    int param;
    int loc;
    List<Instruction> body;

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public List<Instruction> getBody() {
        return body;
    }

    public void setBody(List<Instruction> body) {
        this.body = body;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public Function(int name, int ret, int loc) {
        this.name = name;
        this.ret = ret;
        this.loc = loc;
    }

    public Function(int name, int ret, int loc, List<Instruction> body) {
        this.name = name;
        this.ret = ret;
        this.loc = loc;
        this.body = body;
    }

    public Function(int name, int ret, int param, int loc, List<Instruction> body) {
        this.name = name;
        this.ret = ret;
        this.param = param;
        this.loc = loc;
        this.body = body;
    }

    public void addInstruction(Instruction ins){
        this.body.add(ins);
    }

    public String toOutString(){
        String s = "";
        StringBuffer ss = new StringBuffer(s);
        ss.append(Instruction.toLexString(this.name));
        ss.append("\n");
        ss.append(Instruction.toLexString(this.ret));
        ss.append("\n");
        ss.append(Instruction.toLexString(this.param));
        ss.append("\n");
        ss.append(Instruction.toLexString(this.loc));
        ss.append("\n");
        ss.append(Instruction.toLexString(this.body.size()));
        ss.append("\n");
        for(Instruction item : this.body)
        {
            ss.append("\t");
            ss.append(item.toOutString());
            ss.append("\n");
        }
        return ss.toString();
    }
}
