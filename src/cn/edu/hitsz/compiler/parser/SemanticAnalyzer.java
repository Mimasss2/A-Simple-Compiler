package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.NonTerminal;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    private SymbolTable symbolTable;
    private Stack<SourceCodeType> typeStack = new Stack<>();
    private Stack<Token> tokenStack = new Stack<>();

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        // do nothing
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        if (production.index() == 4) {
//            System.out.println(tokenStack);
//            System.out.println(typeStack);
            // S -> D id;
            Token id = tokenStack.pop();
            typeStack.pop();
            tokenStack.pop();
            SourceCodeType dtype = typeStack.pop();

            typeStack.push(null);
            tokenStack.push(null);

//            System.out.println(id);
//            System.out.println(dtype);

            SymbolTableEntry tableEntry = this.symbolTable.get(id.getText());
            tableEntry.setType(dtype);
        } else if (production.index() == 5) {
            // D -> int;
            SourceCodeType type = typeStack.pop();
//            System.out.println(type);
            tokenStack.pop();
            tokenStack.push(null);
            typeStack.push(type);
        } else {
            int n = production.body().size();
            while(n > 0) {
                typeStack.pop();
                tokenStack.pop();
                n--;
            }
            typeStack.push(null);
            tokenStack.push(null);
        }

    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        tokenStack.push(currentToken);
        if(currentToken.getKind().getIdentifier().equals("int")) {
//            System.out.println("scanning int");
            typeStack.push(SourceCodeType.Int);
        }
        else {
            typeStack.push(null);
        }
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        this.symbolTable = table;
    }
}

