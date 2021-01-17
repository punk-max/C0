package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,
    /** 标识符 */
    IDENT,
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串字面量 */
    STRING_LITERAL,
    /** double型浮点数 */
    DOUBLE_LITERAL,
    /** 单字符字面量 */
    CHAR_LITERAL,
    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 赋值符号 */
    ASSIGN,
    /** 相等符号 */
    EQ,
    /** 不等号 */
    NEQ,
    /** 小于号 */
    LT,
    /** 大于号 */
    GT,
    /** 小于等于号 */
    LE,
    /** 大于等于号 */
    GE,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /** 分号 */
    SEMICOLON,
    /** 注释 */
    COMMENT,
    /**类型*/
    ty,
    /** 文件结尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "Null";
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case EOF:
                return "EOF";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case UINT_LITERAL:
                return "digit+";
            case STRING_LITERAL:
                return "string";
            case DOUBLE_LITERAL:
                return "double";
            case CHAR_LITERAL:
                return "char";
            case PLUS:
                return "PLUS";
            case MINUS:
                return "MINUS";
            case MUL:
                return "MUL";
            case DIV:
                return "DIV";
            case ASSIGN:
                return "ASSIGN";
            case EQ:
                return "EQUAL";
            case NEQ:
                return "NOT EQUAL";
            case LT:
                return "LESS THAN";
            case GT:
                return "GREATER THAN";
            case LE:
                return "LESS EQUAL";
            case GE:
                return "GREATER EQUAL";
            case L_PAREN:
                return "LEFT PAREN";
            case R_PAREN:
                return "RIGHT PAREN";
            case L_BRACE:
                return "LEFT BRACE";
            case R_BRACE:
                return "RIGHT BRACE";
            case ARROW:
                return "ARROW";
            case COMMA:
                return "COMMA";
            case COLON:
                return "COLON";
            case SEMICOLON:
                return "SEMICOLON";
            case COMMENT:
                return "COMMENT";
            case IDENT:
                return "IDENT";
            case ty:
                return "ty";
            default:
                return "InvalidToken";
        }
    }
}
