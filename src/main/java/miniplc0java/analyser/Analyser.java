package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.Function;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;
    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 全局变量表*/
    List<GlobalEntry> globalTable = new ArrayList<>();

    /** 函数及局域变量表*/
    List<SymbolEntry> symbolTable = new ArrayList<>();

    //总类型表
    Map<Integer,Map<Integer,Type>> typeTable = new HashMap<>();

    //局域类型表
    Map<Integer,Type> locaTypeTable = new HashMap<>();

    //返回值slot记录
    Map<Integer,Integer> slotTable = new HashMap<>();

    int paramCount = 0;
    int locaCount = 0;

    Object literal;

    //字符串入栈长度
    int length = 1;

    //是否进入函数
    boolean inFunc = false;

    //作用域等级
    int level = 0;

    //函数域等级
    int funcLevel = 0;

    //全局变量个数
    int globalCount = 0;

    //栈偏移位置
    int stackSetoff1;
    int stackSetoff2;

    //函数id
    int funcId;

    //表达式是否有值
    boolean isTrue;

    //比较符号
    boolean anti;

    //最近的操作数的类型
    Type recentType = Type.Void;

    //最近的两个变量类型是否相同
    boolean isSameType = false;

    //函数列表
    List<Function> funcs = new ArrayList<>();

    //重置属性
    public void reset(){
        locaCount = 0;
        paramCount = 0;
        instructions.clear();
    }

    //查找全局变量是否重复
    public GlobalEntry findGlobal(String name){
        for(GlobalEntry item:globalTable)
        {
            if(item.getName().equals(name))
                return item;
        }
        return null;
    }

    //查找某个函数的某一级及其上级的局域变量
    public SymbolEntry findLocal(String name,int belong,int level){
        for(int i = symbolTable.size()-1;i>=0;i--)
        {
            SymbolEntry item = symbolTable.get(i);
            if(item.getName().equals(name) && item.getBelong() == belong && item.getLevel()<=level)
                return item;
        }
        return null;
    }

    //释放某一级的局域变量
    public void releaseBlock(int level){
        for(int i = symbolTable.size()-1; i>=0;i--)
        {
            if(symbolTable.get(i).getLevel() == level)
                symbolTable.remove(i);
        }
    }

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public void analyse() throws CompileError {
        analyseProgram();
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 recentType，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 recentType，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 recentType，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * 
     * @return
     */


    /**
     * 添加一个符号
     * 
     * @param name          名字
     * @param isInit 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addLocal(String name, Type type, int belong, int level, boolean isInit, boolean isConstant,int stackSetoff,Pos curPos) throws AnalyzeError {
        SymbolEntry item = findLocal(name,belong,level);
        if(item == null){
            symbolTable.add(new SymbolEntry(name,isConstant,isInit,type,level,belong,stackSetoff));
        }
        else
        {
            if(item.getBelong()==belong && item.getLevel() == level)
            {
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
            }
        }
    }

    private void addLocal(String name, Type type, int belong, int level, boolean isInit, boolean isConstant,int stackSetoff1,int stackSetoff2,Pos curPos) throws AnalyzeError {
        SymbolEntry item = findLocal(name,belong,level);
        if(item == null){
            symbolTable.add(new SymbolEntry(name,isConstant,isInit,type,level,belong,stackSetoff1,stackSetoff2));
        }
        else
        {
            if(item.getBelong()==belong && item.getLevel() == level)
            {
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
            }
        }
    }

    //添加全局变量
    private void addGlobal(int id,String name, Type type, boolean isInit, boolean isConstant,Pos curPos) throws AnalyzeError {
        GlobalEntry item = findGlobal(name);
        if(item == null){
            globalTable.add(new GlobalEntry(id,name,isConstant,isInit,type));
        }
        else
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
    }

    //添加全局变量
    private void addGlobalValue(int id,String name,Object value, Type type, boolean isInit, boolean isConstant,Pos curPos) throws AnalyzeError {
        GlobalEntry item = findGlobal(name);
        if(item == null){
            globalTable.add(new GlobalEntry(id,name,value,isConstant,isInit,type));
        }
        else
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
    }

    //添加函数
    private void addFunc(int id,String name, Type type, boolean isInit, boolean isConstant,Pos curPos,boolean isFunc) throws AnalyzeError {
        GlobalEntry item = findGlobal(name);
        if(item == null){
            globalTable.add(new GlobalEntry(id,name,isConstant,isInit,isFunc,type));
        }
        else
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,curPos);
    }

    /**
     * 设置符号为已赋值
     * 
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name,int belong, int level, Pos curPos) throws AnalyzeError {
        SymbolEntry item = findLocal(name,belong,level);
        if (item == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            item.setInitialized(true);
        }
    }

    //设置全局变量为已初始化
    private void initializeGlobal(String name, Pos curPos) throws AnalyzeError {
        GlobalEntry item = findGlobal(name);
        if (item == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            item.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name,int belong, int level, Pos curPos) throws AnalyzeError {
        SymbolEntry entry = findLocal(name,belong,level);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getLoca();
        }
    }


    /**
     * 获取变量是否是常量
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name,int belong, int level, Pos curPos) throws AnalyzeError {
        SymbolEntry entry = findLocal(name,belong,level);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    //标准库函数调用
    private void useLib(String name,Pos curPos) throws AnalyzeError {
        if(name.equals("getint")){
            instructions.add(new Instruction(Operation.scani));
            stackSetoff1++;
            isTrue = false;
        }
        else if(name.equals("getdouble")){
            instructions.add(new Instruction(Operation.scanf));
            stackSetoff1++;
            isTrue = false;
        }
        else if(name.equals("getchar")){
            instructions.add(new Instruction(Operation.scanc));
            stackSetoff1++;
            isTrue = false;
        }
        else if(name.equals("putint")){
            if(recentType!=Type.Int)
                throw new AnalyzeError(ErrorCode.WrongType,curPos);
            instructions.add(new Instruction(Operation.printi));
            stackSetoff1--;
            isTrue = true;
        }
        else if(name.equals("putdouble")){
            if(recentType!=Type.Double)
                throw new AnalyzeError(ErrorCode.WrongType,curPos);
            instructions.add(new Instruction(Operation.printf));
            stackSetoff1--;
            isTrue = true;
        }
        else if(name.equals("putchar")){
            instructions.add(new Instruction(Operation.printc));
            stackSetoff1--;
            isTrue = true;
        }
        else if(name.equals("putstr")){
            for(int i = 0;i<length;i++)
            {
                instructions.add(new Instruction(Operation.printc));
                stackSetoff1--;
            }
            length = 1;
            isTrue = true;
        }
        else if(name.equals("putln")){
            instructions.add(new Instruction(Operation.push,(long)'\n'));
            instructions.add(new Instruction(Operation.printc));
        }
        else
            throw new AnalyzeError(ErrorCode.NotDeclared,curPos);
    }

    //获取全局变量是否为常量
    private boolean isGlobalConstant(String name, Pos curPos) throws AnalyzeError {
        GlobalEntry entry = findGlobal(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    //查询符号表是否冲突
    private boolean isDup(String name){
        if(level == 0)
        {
            GlobalEntry entry = findGlobal(name);
            return (entry == null);
        }
        else{
            SymbolEntry entry = findLocal(name,funcId,level);
            if(entry == null)
                return true;
            else
            {
                return (entry.getLevel() != level);
            }
        }
    }

    //二元操作
    public void putBinOpration(String s, Pos curPos) throws AnalyzeError{
        if(recentType ==Type.Int)
        {
            if(s.equals("+"))
            {
                instructions.add(new Instruction(Operation.addi));
                locaTypeTable.remove(stackSetoff1);
                stackSetoff1--;
            }
            else if(s.equals("-"))
            {
                instructions.add(new Instruction(Operation.subi));
                locaTypeTable.remove(stackSetoff1);
                stackSetoff1--;
            }
            else if(s.equals("/"))
            {
                instructions.add(new Instruction(Operation.divi));
                stackSetoff1--;
            }
            else if(s.equals("*"))
            {
                instructions.add(new Instruction(Operation.muli));
                stackSetoff1--;
            }
            else if(s.equals("=="))
            {
                instructions.add(new Instruction(Operation.cmpi));
                stackSetoff1--;
                anti = false;
            }
            else if (s.equals(">"))
            {
                instructions.add(new Instruction(Operation.cmpi));
                instructions.add(new Instruction(Operation.setgt));
                stackSetoff1--;
                anti = true;
            }
            else if (s.equals("<"))
            {
                instructions.add(new Instruction(Operation.cmpi));
                instructions.add(new Instruction(Operation.setlt));
                stackSetoff1--;
                anti = true;
            }
            else if (s.equals(">="))
            {
                instructions.add(new Instruction(Operation.cmpi));
                instructions.add(new Instruction(Operation.setlt));
                stackSetoff1--;
                anti = false;
            }
            else if (s.equals("<="))
            {
                instructions.add(new Instruction(Operation.cmpi));
                instructions.add(new Instruction(Operation.setgt));
                stackSetoff1--;
                anti = false;
            }
            else if (s.equals("!="))
            {
                instructions.add(new Instruction(Operation.cmpi));
                stackSetoff1--;
                anti = true;
            }
            else
                throw new AnalyzeError(ErrorCode.InvalidInput,curPos);
        }
        else if(recentType == Type.Double)
        {
            if(s.equals("+"))
                {
                    instructions.add(new Instruction(Operation.addf));
                    stackSetoff1--;
                }
            else if(s.equals("-"))
                {
                    instructions.add(new Instruction(Operation.subf));
                    stackSetoff1--;
                }
            else if(s.equals("/"))
                {
                    instructions.add(new Instruction(Operation.divf));
                    stackSetoff1--;
                }
            else if(s.equals("*"))
                {
                    instructions.add(new Instruction(Operation.mulf));
                    stackSetoff1--;
                }
                //不清楚
            else if(s.equals("=="))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    stackSetoff1--;
                    anti = false;
                }
            else if(s.equals(">"))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    instructions.add(new Instruction(Operation.setgt));
                    stackSetoff1--;
                    anti = true;
                }
            else if(s.equals("<"))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    instructions.add(new Instruction(Operation.setlt));
                    stackSetoff1--;
                    anti = true;
                }
            else if(s.equals(">="))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    instructions.add(new Instruction(Operation.setlt));
                    stackSetoff1--;
                    anti = false;
                }
            else if(s.equals("<="))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    instructions.add(new Instruction(Operation.setgt));
                    stackSetoff1--;
                    anti = false;
                }
            else if(s.equals("!="))
                {
                    instructions.add(new Instruction(Operation.cmpf));
                    stackSetoff1--;
                    anti = true;
                }
            else
                throw new AnalyzeError(ErrorCode.InvalidInput,curPos);
            }
        else
            throw new AnalyzeError(ErrorCode.WrongType,curPos);
    }

    //将ident中的value字符串转为其对应type

    private void analyseProgram() throws CompileError {
        // 程序 -> 'begin' 主过程 'end'
        // 示例函数，示例如何调用子程序
        // 'begin'
        //expect(TokenType.Begin);

        analyseMain();

        // 'end'
        //expect(TokenType.End);
        expect(TokenType.EOF);
    }

    private void analyseMain() throws CompileError {
        // 主过程 -> 常量声明 变量声明 语句序列
        while (check(TokenType.CONST_KW) || check(TokenType.LET_KW) || check(TokenType.FN_KW))
        {
            if(check(TokenType.CONST_KW) || check(TokenType.LET_KW))
                analyseDeclStml();
            else if(check(TokenType.FN_KW))
                analyseFunction();
        }
        //throw new Error("Not implemented");
    }

    //判断是否为type
    private void annlyseType(Pos curPos) throws CompileError{
        if(expect(TokenType.IDENT).getValue().equals("int")||expect(TokenType.IDENT).getValue().equals("void"))
            return ;
        else
            throw new AnalyzeError(ErrorCode.InvalidInput,curPos);

    }

    //分析表达式
    private void analyseExpr() throws CompileError{
        var token = peek();
        Pos curPos = token.getStartPos();
        //first集为{'-','IDENT','UINT','DOUBLE','STRING','('}
            //如果是减号规约为取反表达式
            if(check(TokenType.MINUS))
            {
                analyseNegateExpr();
                isTrue = false;
                instructions.add(new Instruction(Operation.push,0L));
                //需要考虑取反数值的类型
                if(locaTypeTable.get(stackSetoff1)==Type.Double)
                    instructions.add(new Instruction(Operation.subf));
                else if(locaTypeTable.get(stackSetoff1)==Type.Int)
                    instructions.add(new Instruction(Operation.subi));
                else
                    throw new AnalyzeError(ErrorCode.WrongType,curPos);
            }
            //如果是标识符规约为标识符或赋值表达式或函数调用
            else if(check(TokenType.IDENT)){
                Token token1 = next();
                String name = token1.getValueString();
                if(check(TokenType.ASSIGN))
                {
                    SymbolEntry sy = findLocal(name,funcId,level);
                    if(sy == null)
                    {
                        GlobalEntry gy = findGlobal(name);
                        if(gy == null)
                            throw new AnalyzeError(ErrorCode.NotDeclared,curPos);
                        Type type = gy.getType();
                        recentType = type;
                        int id = gy.getId();
                        instructions.add(new Instruction(Operation.globa,id));
                        stackSetoff1++;
                        locaTypeTable.put(stackSetoff1,type);
                        if(stackSetoff2>=0)
                            stackSetoff2++;
                    }
                    //如果是局域变量
                    else
                    {
                        Type type = sy.getType();
                        int stack1 = sy.getLoca();
                        int stack2 = sy.getArga();
                        recentType = type;
                        if(stack2>0)
                        {
                            instructions.add(new Instruction(Operation.arga,stack2));
                            stackSetoff1++;
                        }
                        else
                        {
                            instructions.add(new Instruction(Operation.loca,stack1));
                            stackSetoff1++;
                        }
                        if(stackSetoff2>=0)
                            stackSetoff2++;
                        locaTypeTable.put(stackSetoff1,type);
                    }
                    analyseAssginExpr();
                    instructions.add(new Instruction(Operation.store64));
                    isTrue = true;
                }
                //函数调用
                else if(check(TokenType.L_PAREN))
                {
                    GlobalEntry g = findGlobal(name);
                    //可能是库函数
                    if(g == null)
                    {
                        expect(TokenType.L_PAREN);
                        if(!check(TokenType.R_PAREN))
                            analyseCallParamList();
                        expect(TokenType.R_PAREN);
                        useLib(name,curPos);
                    }
                    else{
                        Type type = g.getType();
                        if(!g.isFunc)
                            throw new AnalyzeError(ErrorCode.InvalidIdentifier,curPos);
                        int id  = g.getId();
                        expect(TokenType.L_PAREN);

                        //压入返回值
                        instructions.add(new Instruction(Operation.push,0L));
                        stackSetoff2=0;
                        stackSetoff1++;
                        locaTypeTable.put(stackSetoff1,type);

                        //存入函数返回值slot数
                        slotTable.put(funcLevel,stackSetoff1);

                        //保存原有类型表
                        typeTable.put(funcLevel,locaTypeTable);

                        //函数调用层次加一
                        funcLevel++;

                        //进入函数空间,清空原有的局域类型表
                        locaTypeTable.clear();

                        if(!check(TokenType.R_PAREN))
                            analyseCallParamList();
                        expect(TokenType.R_PAREN);
                        funcLevel--;

                        //重新取出原有的类型表
                        locaTypeTable = typeTable.get(funcLevel);

                        instructions.add(new Instruction(Operation.call,id));

                        //获取原有的slot偏移
                        stackSetoff1 = slotTable.get(funcLevel);
                        //判断返回值类型
                        if(type == Type.Void)
                        {
                            isSameType =  (recentType == Type.Void);
                            recentType = Type.Void;
                        }
                        else if(type == Type.Int)
                        {
                            isSameType =  (recentType == Type.Int);
                            recentType = Type.Int;
                        }
                        else
                        {
                            isSameType =  (recentType == Type.Double);
                            recentType = Type.Double;
                        }
                        isTrue = false;
                    }
                }
                //单独标识符入栈
                else{
                    SymbolEntry sy = findLocal(name,funcId,level);
                    //如果是全局变量
                    if(sy == null)
                    {
                        GlobalEntry gy = findGlobal(name);
                        if(gy == null)
                            throw new AnalyzeError(ErrorCode.NotDeclared,curPos);
                        Type type = gy.getType();
                        isSameType = (type == recentType);
                        recentType = type;
                        int id = gy.getId();
                        instructions.add(new Instruction(Operation.globa,id));
                        instructions.add(new Instruction(Operation.load64));
                        stackSetoff1++;
                        locaTypeTable.put(stackSetoff1,type);
                        if(stackSetoff2>=0)
                            stackSetoff2++;
                    }
                    //如果是局域变量
                    else
                    {
                        Type type = sy.getType();
                        int stack1 = sy.getLoca();
                        int stack2 = sy.getArga();
                        isSameType = (type == recentType);
                        recentType = type;
                        if(stack2>0)
                        {
                            instructions.add(new Instruction(Operation.arga,stack2));
                            stackSetoff1++;
                        }
                        else
                        {
                            instructions.add(new Instruction(Operation.loca,stack1));
                            stackSetoff1++;
                        }
                        if(stackSetoff2>=0)
                            stackSetoff2++;
                        instructions.add(new Instruction(Operation.load64));
                        locaTypeTable.put(stackSetoff1,type);
                    }
                    isTrue = false;
                }
            }
            //如果是字面量，规约为字面量表达式
            else if(check(TokenType.UINT_LITERAL) || check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.CHAR_LITERAL))
            {
                analyseLiteralExpr();
                isTrue = false;
            }
            //如果是左括号规约为括号表达式
            else if(check(TokenType.L_PAREN))
            {
                analyseParenExpr();
                isTrue = false;
            }
            else
                throw new TokenizeError(ErrorCode.InvalidInput,curPos);
            //如果后面跟着AS为类型转换表达式
            while(check(TokenType.AS_KW))
            {
                expect(TokenType.AS_KW);
                Token t = expect(TokenType.ty);
                Type tt = Type.check(t);
                //如果转为Int型
                if(tt == Type.Int && tt != recentType)
                {
                    instructions.add(new Instruction(Operation.ftoi));
                    locaTypeTable.put(stackSetoff1,Type.Int);
                    recentType = Type.Int;
                }
                //如果转为Double型
                else if(tt == Type.Double && tt != recentType)
                {
                    instructions.add(new Instruction(Operation.itof));
                    locaTypeTable.put(stackSetoff1,Type.Double);
                    recentType = Type.Double;
                }
                isTrue = false;
                //类型没有发生转变
            }
            //如果后面跟操作符
        while(analyseBinOperator()) {
            Token t = next();
            String v = t.getValue().toString();
            analyseExpr();
            isTrue = false;
            if(locaTypeTable.get(stackSetoff1) == locaTypeTable.get(stackSetoff1-1))
                putBinOpration(v, curPos);
            else
                throw new AnalyzeError(ErrorCode.WrongType,curPos);
        }
    }

    //运算符表达式
    private void analyseOperatorExpr() throws CompileError{
        analyseExpr();
        var token = peek();
        Pos curPos = token.getStartPos();
        if(analyseBinOperator())
            next();
        else
            throw new TokenizeError(ErrorCode.InvalidInput, curPos);
        analyseExpr();
    }

    //取反表达式
    private void analyseNegateExpr() throws CompileError{
        expect(TokenType.MINUS);
        analyseExpr();
    }

    //赋值表达式
    private void analyseAssginExpr() throws CompileError{
        expect(TokenType.ASSIGN);
        analyseExpr();
    }

    //类型转换表达式
    private void analyseAsExpr() throws CompileError{
        analyseExpr();
        expect(TokenType.AS_KW);
        expect(TokenType.ty);
    }

    //函数调用表达式
    private void analyseCallExpr() throws CompileError{
        expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);
        if(!check(TokenType.R_PAREN))
            analyseCallParamList();
        expect(TokenType.R_PAREN);
    }

    //函数参数列表
    private void analyseCallParamList() throws CompileError{
        //int count = 1;
        analyseExpr();
        //instructions.add(new Instruction(Operation.arga,count++));
        //instructions.add(new Instruction(Operation.store64));
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            analyseExpr();
        }
    }

    //字面量表达式
    private void analyseLiteralExpr() throws CompileError{
        var token = peek();
        Pos curPos = token.getStartPos();
        if(check(TokenType.UINT_LITERAL))
        {
            Token t = expect(TokenType.UINT_LITERAL);
            literal = Integer.parseInt(t.getValue().toString());
            long l = (long)Integer.parseInt(t.getValue().toString());
            instructions.add(new Instruction(Operation.push,l));
            stackSetoff1++;
            locaTypeTable.put(stackSetoff1,Type.Int);
            recentType = Type.Int;
            isTrue = false;
        }
        else if(check(TokenType.DOUBLE_LITERAL))
        {
            Token t = expect(TokenType.DOUBLE_LITERAL);
            long l = (long)Double.parseDouble(t.getValue().toString());
            literal = l;
            instructions.add(new Instruction(Operation.push,l));
            stackSetoff1++;
            locaTypeTable.put(stackSetoff1,Type.Double);
            recentType = Type.Double;
            isTrue = false;
        }
        else if(check(TokenType.STRING_LITERAL))
        {
            Token t = expect(TokenType.STRING_LITERAL);
            String str = t.getValue().toString();
            length = str.length();
            for(int i = length-1;i>=0;i--)
                instructions.add(new Instruction(Operation.push,(long)str.charAt(i)));
            isTrue = false;
        }
        else if(check(TokenType.CHAR_LITERAL))
        {
            Token t = expect(TokenType.CHAR_LITERAL);
            instructions.add(new Instruction(Operation.push,(long)t.getValue()));
            isTrue = false;
        }
        else
            throw new TokenizeError(ErrorCode.InvalidInput,curPos);
    }

    //标识符表达式
    private void analyseIdentExpr() throws CompileError{
        expect(TokenType.IDENT);
    }

    //括号表达式
    private void analyseParenExpr() throws CompileError{
        expect(TokenType.L_PAREN);
        analyseExpr();
        expect(TokenType.R_PAREN);
    }

    //两位运算符,
    private Boolean analyseBinOperator() throws CompileError{
        if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.DIV) || check(TokenType.MUL) ||
                check(TokenType.EQ) || check(TokenType.NEQ) || check(TokenType.LT) || check(TokenType.GT) ||
                check(TokenType.LE) || check(TokenType.GE))
            return true;
        else
            return false;
    }

    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 常量声明 -> 常量声明语句*

        // 如果下一个 token 是 const 就继续
        if (nextIf(TokenType.CONST_KW) != null) {
            // 常量声明语句 -> 'const' 变量名 '=' 常表达式 ';'

            // 变量名
            var token = expect(TokenType.IDENT);

            // 加入符号表
            String name = token.getValue().toString();

            //冒号
            expect(TokenType.COLON);

            // 类型
            Token tt=expect(TokenType.ty);
            Type t = Type.check(tt);

            //赋值号
            expect(TokenType.ASSIGN);

            analyseExpr();

            // 分号
            expect(TokenType.SEMICOLON);
            //符号表是否冲突
            if(!isDup(name))
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,token.getStartPos());

            //添加符号表
            if(level>0)
            {
                addLocal(name, t, funcId,level,true,true, stackSetoff1, token.getStartPos());
                stackSetoff1++;
            }
            else
            {
                addGlobalValue(globalCount, name,literal,t,true,true, token.getStartPos());
                globalCount++;
            }

            // 这里把常量值直接放进栈里，位置和符号表记录的一样。
            // 更高级的程序还可以把常量的值记录下来，遇到相应的变量直接替换成这个常数值，
            // 我们这里就先不这么干了。
        }
    }

    private void analyseLetDeclaration() throws CompileError {//Mark
        // 变量声明 -> 变量声明语句*

        // 如果下一个 token 是 let 就继续
        if (nextIf(TokenType.LET_KW) != null) {
            // 变量声明语句 -> 'let' IDENT ':' ty ('=' expr)? ';'

            // 变量名
            var token = expect(TokenType.IDENT);
            // 变量初始化了吗
            boolean initialized = false;

            // 下个 token 是冒号吗
            expect(TokenType.COLON);
            Token tt=expect(TokenType.ty);
            Type t = Type.check(tt);
            //如果是赋值号说明已经初始化
            if(check(TokenType.ASSIGN))
            {
                expect(TokenType.ASSIGN);
                analyseExpr();
                initialized = true;
            }
            //分号
            expect(TokenType.SEMICOLON);
            // 分析初始化的表达式

            // 加入符号表，请填写名字和当前位置（报错用）
            String name = /* 名字 */ token.getValueString();

            //符号表是否冲突
            if(!isDup(name))
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration,token.getStartPos());
            if(level>0)
            {
                addLocal(name, t, funcId , level,initialized, false, stackSetoff1,/* 当前位置 */ token.getStartPos());
                if (!initialized) {
                    instructions.add(new Instruction(Operation.push, 0L));
                    stackSetoff1++;
                    locaTypeTable.put(stackSetoff1,t);
                }
            }
            else
            {
                if (!initialized)
                    addGlobalValue(globalCount, name,0,t,false,false, token.getStartPos());
                else
                    addGlobalValue(globalCount, name,literal,t,false,false, token.getStartPos());
                globalCount++;
            }
            // 如果没有初始化的话在栈里推入一个初始值
        }
    }

    //声明变量语句
    private void analyseDeclStml() throws CompileError{
        var token = peek();
        Pos curPos = token.getStartPos();
        if(check(TokenType.CONST_KW))
            analyseConstantDeclaration();
        else if(check(TokenType.LET_KW))
            analyseLetDeclaration();
        else
            throw new TokenizeError(ErrorCode.InvalidInput,curPos);
    }

    //if语句
    private void analyseIfStml() throws CompileError{
        expect(TokenType.IF_KW);
        analyseExpr();
        int mark1 = instructions.size();
        analyseBlockStml();
        int mark2 = instructions.size();
        if(anti)
        {

            instructions.add(mark1,new Instruction(Operation.brfalse,mark2-mark1));
        }
        else
            instructions.add(mark1,new Instruction(Operation.brtrue,mark2-mark1));
        stackSetoff1--;
        //如果后面有else语句
        if(check(TokenType.ELSE_KW))
        {
            var token = expect(TokenType.ELSE_KW);
            Pos curPos = token.getStartPos();
            //如果下一个为if
            if(check(TokenType.IF_KW)){
                analyseIfStml();
            }
            //或者为代码块
            else if(check(TokenType.L_BRACE))
            {
                analyseBlockStml();
                int mark3 = instructions.size();
                instructions.add(mark2+1,new Instruction(Operation.br,mark3-mark2-1));
            }
            //其它报错
            else
                throw new TokenizeError(ErrorCode.InvalidInput,curPos);
        }
    }

    //while语句
    private void analyseWhileStml() throws CompileError{
        expect(TokenType.WHILE_KW);
        int mark1 = instructions.size();
        analyseExpr();
        int mark2 = instructions.size();
        analyseBlockStml();
        int mark3 = instructions.size();
        instructions.add(new Instruction(Operation.br,mark1-mark3-2));
        int mark4 = instructions.size() + 1;
        if(anti)
            instructions.add(mark2,new Instruction(Operation.brfalse,mark4-mark2));
        else
            instructions.add(mark2,new Instruction(Operation.brtrue,mark4-mark2));
        stackSetoff1--;
    }

    //return语句
    private void analyseReturnStml() throws CompileError{
        var token = expect(TokenType.RETURN_KW);
        Pos curPos = token.getStartPos();
        if(check(TokenType.MINUS) || check(TokenType.IDENT) || check(TokenType.UINT_LITERAL) ||
                check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.L_PAREN) || check(TokenType.CHAR_LITERAL))
        {
            analyseExpr();
            instructions.add(new Instruction(Operation.arga,0));
            instructions.add(new Instruction(Operation.store64));
        }
        expect(TokenType.SEMICOLON);
        //返回前记录局域变量个数
        locaCount = stackSetoff1+1;

        //加入返回指令
        instructions.add(new Instruction(Operation.ret));

        //清空当前函数的局域类型表
        locaTypeTable.clear();
    }

    //代码块
    private void analyseBlockStml() throws CompileError{
        expect(TokenType.L_BRACE);
        //作用域等级上升
        level++;
        if(check(TokenType.MINUS) || check(TokenType.IDENT) || check(TokenType.UINT_LITERAL) ||
                check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.L_PAREN) ||
                check(TokenType.LET_KW) || check(TokenType.CONST_KW) || check(TokenType.IF_KW) || check(TokenType.WHILE_KW) ||
                check(TokenType.RETURN_KW) || check(TokenType.L_BRACE) || check(TokenType.SEMICOLON))
            analyseStatementSequence();
        expect(TokenType.R_BRACE);
        //删除符号表中该作用域等级的符号
        releaseBlock(level);
        //作用域等级下降
        level--;
    }

    //语句序列
    private void analyseStatementSequence() throws CompileError {
        // 语句序列 -> 语句*
        // 语句 -> 赋值语句 | 输出语句 | 空语句

        while (true) {
            // 如果下一个 token 是……
            if (check(TokenType.MINUS) || check(TokenType.IDENT) || check(TokenType.UINT_LITERAL) ||
                    check(TokenType.DOUBLE_LITERAL) || check(TokenType.STRING_LITERAL) || check(TokenType.L_PAREN)) {

                // 调用相应的分析函数
                // 如果遇到其他非终结符的 FIRST 集呢？
                analyseExpr();
                expect(TokenType.SEMICOLON);

                //如果表达式没意义
                if(!isTrue)
                {
                    for(int i = 0;i<length;i++)
                        instructions.add(new Instruction(Operation.pop));
                    stackSetoff1-=length;
                    length = 1;
                }
            } 
            else if(check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
                analyseDeclStml();
            }
            else if(check(TokenType.IF_KW)){
                analyseIfStml();
            }
            else if(check(TokenType.WHILE_KW)){
                analyseWhileStml();
            }
            else if(check(TokenType.RETURN_KW)){
                analyseReturnStml();
            }
            else if(check(TokenType.L_BRACE)){
                analyseBlockStml();
            }
            else if(check(TokenType.SEMICOLON)){
                expect(TokenType.SEMICOLON);
            }
            else
                break;
        }
        //throw new Error("Not implemented");
    }

    //分析函数变量
    private void analyseFunctionParam() throws CompileError {
        boolean is = false;
        if(check(TokenType.CONST_KW))
        {
            expect(TokenType.CONST_KW);
            is = true;
        }
        Token t = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        Token t2 = expect(TokenType.ty);
        String name = t.getValue().toString();
        if(!isDup(name))
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration,t.getStartPos());
        stackSetoff2++;
        addLocal(name, Type.check(t2), funcId , level,true, is, stackSetoff1,stackSetoff2,t.getStartPos());
        paramCount++;
    }

    //分析函数变量列表
    private void analyseFunctionParamList() throws CompileError {
        analyseFunctionParam();
        while(check(TokenType.COMMA))
        {
            expect(TokenType.COMMA);
            analyseFunctionParam();
        }
    }

    //分析函数
    private void analyseFunction() throws CompileError {
        expect(TokenType.FN_KW);
        Token token1 = expect(TokenType.IDENT);
        expect(TokenType.L_PAREN);
        stackSetoff2=0;
        level++;
        if(check(TokenType.CONST_KW) || check(TokenType.IDENT))
            analyseFunctionParamList();
        expect(TokenType.R_PAREN);
        stackSetoff2=-1;
        expect(TokenType.ARROW);
        Token token2 = expect(TokenType.ty);
        Type tp = Type.check(token2);
        globalTable.add(new GlobalEntry(globalCount,token1.getValue().toString(),token1.getValue(),false,false,true,tp));
        globalCount++;
        stackSetoff1=-1;
        reset();
        analyseBlockStml();
        releaseBlock(level);
        level--;
        funcs.add(new Function(globalCount-1,1,paramCount,locaCount,instructions));
    }

    public List<GlobalEntry> getGlobalTable() {
        return globalTable;
    }

    public List<Function> getFuncs() {
        return funcs;
    }
}
