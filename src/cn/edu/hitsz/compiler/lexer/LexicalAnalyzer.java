package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private String code;
    private List<Token> tokens;

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        StringBuilder code = new StringBuilder();
        try (FileReader reader = new FileReader(path);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                code.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.code = code.toString();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        this.tokens = new ArrayList<>();
        int state = StateConst.START;
        int start = 0;
        int end = 0;
        int codeLen = this.code.length();
        char curChar;
        while (end < codeLen) {
            curChar = this.code.charAt(end);
            switch(state) {
                case(StateConst.START): {
                    if(curChar == '\r' | Character.isWhitespace(curChar)) {
                        break;
                    } else if (Character.isDigit(curChar)) {
                        state = StateConst.INT_CONST1;
                        start = end;
                    } else if (Character.isLetter(curChar)) {
                        state = StateConst.ID1;
                        start = end;
                    } else if (curChar == '=') {
                        this.tokens.add(Token.simple("="));
                    } else if (curChar == ',') {
                        this.tokens.add(Token.simple(","));
                    } else if (curChar == ';') {
                        this.tokens.add(Token.simple("Semicolon"));
                    } else if (curChar == '+') {
                        this.tokens.add(Token.simple("+"));
                    } else if (curChar == '-') {
                        this.tokens.add(Token.simple("-"));
                    } else if (curChar == '*') {
                        this.tokens.add(Token.simple("*"));
                    } else if (curChar == '/') {
                        this.tokens.add(Token.simple("/"));
                    } else if (curChar == '(') {
                        this.tokens.add(Token.simple("("));
                    } else if (curChar == ')') {
                        this.tokens.add(Token.simple(")"));
                    }
                    break;
                }
                case(StateConst.INT_CONST1): {
                    if(!Character.isDigit(curChar)) {
                        state = StateConst.START;
                        String number = this.code.substring(start, end);
                        this.tokens.add(Token.normal("IntConst", number));
                        end--;
                    }
                    break;
                }
                case(StateConst.ID1): {
                    if(!Character.isLetter(curChar)) {
                        state = StateConst.START;
                        String identifierText = this.code.substring(start, end);
                        end--;
                        if(TokenKind.isAllowed(identifierText)) {
                            this.tokens.add(Token.simple(identifierText));
                        }else {
                            this.tokens.add(Token.normal("id", identifierText));
                            if (!symbolTable.has(identifierText)) {
                                symbolTable.add(identifierText);
                            }
                        }
                    }
                    break;
                }
                default:{
                }
            }
            end++;
        }
        this.tokens.add(Token.eof());

    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return this.tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
