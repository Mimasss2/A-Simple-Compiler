package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    private SymbolTable symbolTable;
    private List<Instruction> instructions = new ArrayList<>();
    private Stack<IRValue> irValueStack = new Stack<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        switch (currentToken.getKindId()) {
            case "int", "return", "=", ",", "Semicolon", "+", "-", "*", "/","(", ")" -> {
                irValueStack.push(null);
            }
            case "id" -> {
                irValueStack.push(IRVariable.named(currentToken.getText()));
            }
            case "IntConst" -> {
                irValueStack.push(IRImmediate.of(Integer.parseInt(currentToken.getText())));
            }
            default -> {
                throw new RuntimeException("Unknown production index");
            }
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Instruction instruction;
        switch (production.index()) {
            // 我们推荐在 case 后面使用注释标明产生式
            // 这样能比较清楚地看出产生式索引与产生式的对应关系
            case 1,2,3,4,5 -> {
                // P -> S_list;
                // S_list -> S Semicolon S_list;
                // S_list -> S Semicolon;
                // S -> D id;
                // D -> int;
                for (int i = 0; i < production.body().size(); i++) {
                    irValueStack.pop();
                }
                irValueStack.push(null);
            }
            case 6 -> { // S -> id = E;
                List<Term> body = production.body();
                IRValue e = irValueStack.pop();
                irValueStack.pop();
                IRValue id = irValueStack.pop();
                irValueStack.push(null);

                instruction = Instruction.createMov((IRVariable) id, e);
                instructions.add(instruction);
            }
            case 7 -> { // S -> return E;
                IRValue e = irValueStack.pop();
                irValueStack.pop();
                irValueStack.push(null);

                instruction = Instruction.createRet(e);
                instructions.add(instruction);

            }
            case 8 -> { // E -> E + A;
                IRValue a = irValueStack.pop();
                irValueStack.pop();
                IRValue e = irValueStack.pop();
                IRVariable temp = IRVariable.temp();
                irValueStack.push(temp);

                instruction = Instruction.createAdd(temp, e, a);
                instructions.add(instruction);
            }
            case 9 -> { // E -> E - A;
                IRValue a = irValueStack.pop();
                irValueStack.pop();
                IRValue e = irValueStack.pop();
                IRVariable temp = IRVariable.temp();
                irValueStack.push(temp);

                instruction = Instruction.createSub(temp, e, a);
                instructions.add(instruction);
            }
            case 10 -> { // E -> A;
                // do nothing
            }
            case 11 -> { // A -> A * B;
                IRValue b = irValueStack.pop();
                irValueStack.pop();
                IRValue a = irValueStack.pop();
                IRVariable temp = IRVariable.temp();
                irValueStack.push(temp);

                instruction = Instruction.createMul(temp, a, b);
                instructions.add(instruction);
            }
            case 12-> { // A -> B;
                // do nothing
            }
            case 13 -> { // B -> ( E );
                irValueStack.pop();
                IRValue e = irValueStack.pop();
                irValueStack.pop();
                irValueStack.push(e);
            }
            case 14,15 -> { // B -> id;   B -> IntConst;
                IRValue id = irValueStack.pop();
                irValueStack.push(id);
            }
            default -> { //
                 throw new RuntimeException("Unknown production index");
                // 或者任何默认行为
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        // do nothing
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.symbolTable = table;
    }

    public List<Instruction> getIR() {
        // TODO
        return this.instructions;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

