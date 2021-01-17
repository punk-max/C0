package miniplc0java.tokenizer;

import miniplc0java.error.ErrorCode;
import miniplc0java.error.TokenizeError;
import miniplc0java.util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();
        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if ((peek<='z'&&peek>='a')||(peek<='Z'&&peek>='A')||peek=='_') {
            return lexIdentOrKeyword();
        }else if(peek=='\"')
            return lexString();
        else if(peek=='\'')
            return lexChar();
        else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        StringBuffer s = new StringBuffer();
        Pos spos=it.currentPos();
        Pos epos=it.nextPos();
        while(it.peekChar()<='9'&&it.peekChar()>='0'){
            s.append(it.nextChar());
            //如果有小数点，判断是否为double
            if(it.peekChar()=='.'){
                s.append(it.nextChar());
                //小数点后无数字，报错
                if(it.peekChar()>='9'||it.peekChar()<='0')
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                while(it.peekChar()<='9'&&it.peekChar()>='0'){
                    s.append(it.nextChar());
                }
                //如果后面有Ee，判断是否为科学计数
                if(it.peekChar()=='e'||it.peekChar()=='E')
                {
                    s.append(it.nextChar());
                    //如果有正负号，加入字符串判定
                    if(it.peekChar()=='+'||it.peekChar()=='-')
                        s.append(it.nextChar());
                    //后面必须有数字，否则报错
                    if(it.peekChar()<='9'||it.peekChar()>='0') {
                        while (it.peekChar() <= '9' && it.peekChar() >= '0')
                            s.append(it.nextChar());
                    }
                    else
                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
                //返回double型
                epos=it.currentPos();
                return new Token(TokenType.DOUBLE_LITERAL,s.toString(),spos,epos);
            }
        }
        //返回无符号型
        epos=it.currentPos();
        return new Token(TokenType.UINT_LITERAL,Integer.parseInt(s.toString()),spos,epos);
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        //throw new Error("Not implemented");
    }

    //匹配转义字符，返回该字符
    private Boolean isEscapeSequence(StringBuffer s){
        s.append(it.nextChar());
        switch (it.nextChar()){
            case '\\':
                s.append('\\');
                return true;
            case '\"':
                s.append('\"');
                return true;
            case '\'':
                s.append('\'');
                return true;
            case '\n':
                s.append('\n');
                return true;
            case '\r':
                s.append('\r');
                return true;
            case '\t':
                s.append('\t');
                return true;
            default:
                return false;
        }
    }

    private Token lexString() throws TokenizeError{
        StringBuffer s = new StringBuffer();
        Pos spos=it.currentPos();
        Pos epos=it.nextPos();
        it.nextChar();
        while(StringIter.isRegularString(it.peekChar())){
            if(it.peekChar()=='\\'){
                if(!isEscapeSequence(s))
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            else
                s.append(it.nextChar());
        }
        if(it.peekChar()=='\"')
        {
            it.nextChar();
            epos = it.currentPos();
            return new Token(TokenType.STRING_LITERAL,s.toString(),spos,epos);
        }
        else
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
    }

    private Token lexChar() throws TokenizeError{
        StringBuffer s = new StringBuffer();
        Pos spos=it.currentPos();
        Pos epos=it.nextPos();
        it.nextChar();
        if(StringIter.isRegularChar(it.peekChar())){
            if(it.peekChar()=='\\'){
                if(!isEscapeSequence(s))
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            else
                s.append(it.nextChar());
            if(it.peekChar()!='\'')
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            else {
                it.nextChar();
                epos = it.currentPos();
                return new Token(TokenType.CHAR_LITERAL, s.toString(),spos,epos );
            }
        }
        else
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        StringBuffer s = new StringBuffer();
        Pos spos=it.currentPos();
        Pos epos=it.nextPos();
        while((it.peekChar()<='9'&&it.peekChar()>='0')||(it.peekChar()<='Z'&&it.peekChar()>='A')||(it.peekChar()<='z'&&it.peekChar()>='a')||it.peekChar()=='_'){
            s.append(it.nextChar());
        }
        epos=it.currentPos();
        String str=s.toString();
        if(str.equals("fn")){
            return new Token(TokenType.FN_KW,null,spos,epos);
        }
        else if(str.equals("let")){
            return new Token(TokenType.LET_KW,null,spos,epos);
        }
        else if(str.equals("const")){
            return new Token(TokenType.CONST_KW,null,spos,epos);
        }
        else if(str.equals("as")){
            return new Token(TokenType.AS_KW,null,spos,epos);
        }
        else if(str.equals("while")){
            return new Token(TokenType.WHILE_KW,null,spos,epos);
        }
        else if(str.equals("if")){
            return new Token(TokenType.IF_KW,null,spos,epos);
        }
        else if(str.equals("else")){
            return new Token(TokenType.ELSE_KW,null,spos,epos);
        }
        else if(str.equals("return")){
            return new Token(TokenType.RETURN_KW,null,spos,epos);
        }
        else if(str.equals("break")){
            return new Token(TokenType.BREAK_KW,null,spos,epos);
        }
        else if(str.equals("continue")){
            return new Token(TokenType.CONTINUE_KW,null,spos,epos);
        }
        else if(str.equals("int")||str.equals("void")||str.equals("double"))
            return new Token(TokenType.ty,str,spos,epos);
        else{
            return new Token(TokenType.IDENT,str,spos,epos);
        }
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
    }

    //识别运算符的类型，返回对应的token，识别失败则抛出异常
    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
            case '-':
                // 填入返回语句
                if(it.peekChar()=='>')
                {
                    Pos p = it.previousPos();
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", p, it.currentPos());
                }
                else
                    return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            // 填入更多状态和返回语句
            case '=':
                if(it.peekChar()=='=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                else
                    return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case '!':
                if(it.peekChar()=='=') {
                    Pos p = it.previousPos();
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", p, it.currentPos());
                }
                else
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            case '<':
                if(it.peekChar()=='=') {
                    Pos p = it.previousPos();
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", p, it.currentPos());
                }
                else
                    return new Token(TokenType.LT,'<',it.previousPos(),it.currentPos());
            case '>':
                if(it.peekChar()=='=') {
                    Pos p = it.previousPos();
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", p, it.currentPos());
                }
                else
                    return new Token(TokenType.GT,'>',it.previousPos(),it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    //跳过所有空白字符，将指针移到第一个非空白字符上
    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
