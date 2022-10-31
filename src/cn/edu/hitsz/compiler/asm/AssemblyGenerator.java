package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    private List<AsmInstruction> asmInstructions = new ArrayList<>();
    private List<Instruction> processedInstructions = new ArrayList<>();
    private List<Boolean> isRs1LastUse = new ArrayList<>();
    private List<Boolean> isRs2LastUse = new ArrayList<>();

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // pre process
        // TODO: add variable reference info
        int i = 0;
        Instruction inst, newInst;
        for(i=0; i<originInstructions.size(); i++) {
            inst = originInstructions.get(i);
            IRValue lhs,rhs;
            InstructionKind kind = inst.getKind();
            if(kind.isBinary()) {
                IRVariable res = inst.getResult();
                lhs = inst.getLHS();
                rhs = inst.getRHS();
                if(lhs.isImmediate() && rhs.isImmediate()) {
                    int value = ((IRImmediate)lhs).getValue() + ((IRImmediate)rhs).getValue();
                    newInst = Instruction.createMov(res, IRImmediate.of(value));
                    processedInstructions.add(newInst);
                } else if (lhs.isImmediate()) {
                    switch (kind) {
                        case ADD :{
                            newInst = Instruction.createAdd(res, rhs, lhs);
                            processedInstructions.add(newInst);
                            break;
                        }
                        case SUB :{
                            IRVariable temp = IRVariable.temp();
                            newInst = Instruction.createMov(temp, lhs);
                            processedInstructions.add(newInst);
                            newInst = Instruction.createSub(res, temp, rhs);
                            processedInstructions.add(newInst);
                            break;
                        }
                        case MUL :{
                            IRVariable temp = IRVariable.temp();
                            newInst = Instruction.createMov(temp, lhs);
                            processedInstructions.add(newInst);
                            newInst = Instruction.createMul(res, temp, rhs);
                            processedInstructions.add(newInst);
                            break;
                        }
                        default : {
                            processedInstructions.add(inst);
                            break;
                        }
                    }
                } else if (rhs.isImmediate()) {
                    if (kind == InstructionKind.MUL) {
                        IRVariable temp = IRVariable.temp();
                        newInst = Instruction.createMov(temp, rhs);
                        processedInstructions.add(newInst);
                        newInst = Instruction.createMul(res, lhs, temp);
                        processedInstructions.add(newInst);
                    } else {
                        processedInstructions.add(inst);
                    }
                } else {
                    processedInstructions.add(inst);
                }
            }else if (kind.isReturn()) {
                processedInstructions.add(inst);
                break;
            } else {
                processedInstructions.add(inst);
            }
        }
        assessLastUse();
    }

    private void assessLastUse() {
        List<IRVariable> appearedVariables = new ArrayList<>();
        Instruction inst;
        IRValue rs1, rs2;
        List<IRValue> operands;
        for(int i=processedInstructions.size()-1; i >= 0; i--) {
            inst = processedInstructions.get(i);
            operands = inst.getOperands();
            rs1 = operands.get(0);
            if (rs1.isIRVariable()) {
                if(appearedVariables.contains(rs1)) {
                    isRs1LastUse.add(0,false);
                } else {
                    isRs1LastUse.add(0, true);
                    appearedVariables.add((IRVariable) rs1);
                }
            } else {
                isRs1LastUse.add(0, false);
            }
            if (operands.size() == 2) {
                rs2 = operands.get(1);
                if (rs2.isIRVariable()) {
                    if(appearedVariables.contains(rs2)) {
                        isRs2LastUse.add(0,false);
                    } else {
                        isRs2LastUse.add(0, true);
                        appearedVariables.add((IRVariable) rs2);
                    }
                } else {
                    isRs2LastUse.add(0, false);
                }
            }
            else {
                isRs2LastUse.add(0, false);
            }

            if (inst.getKind() != InstructionKind.RET) {
                IRVariable res = inst.getResult();
                if (!operands.contains(res)) {
                    appearedVariables.remove(res);
                }
            }

        }
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        RegisterAssigner ass = new RegisterAssigner();
        AsmInstruction asmInst;
        for (int i=0; i < processedInstructions.size(); i++) {
            Instruction inst = processedInstructions.get(i);
            System.out.println(inst.toString());
            IRValue lhs,rhs;
            InstructionKind kind = inst.getKind();
            switch (kind) {
                case MUL: {
                    IRVariable res = inst.getResult();
                    lhs = inst.getLHS();
                    rhs = inst.getRHS();
                    String r_res = ass.assignReg(res);
                    String r_lhs = ass.assignReg((IRVariable) lhs);
                    String r_rhs = ass.assignReg((IRVariable) rhs);
                    asmInst = AsmInstruction.createMul(r_res, r_lhs, r_rhs);
                    asmInstructions.add(asmInst);
                    break;
                }
                case SUB :{
                    IRVariable res = inst.getResult();
                    lhs = inst.getLHS();
                    rhs = inst.getRHS();
                    String r_res = ass.assignReg(res);
                    String r_lhs = ass.assignReg((IRVariable) lhs);
                    if (rhs.isImmediate()) {
                        String r_rhs = rhs.toString();
                        asmInst = AsmInstruction.createSubi(r_res, r_lhs, r_rhs);
                        asmInstructions.add(asmInst);
                    } else {
                        String r_rhs = ass.assignReg((IRVariable) rhs);
                        asmInst = AsmInstruction.createSub(r_res, r_lhs, r_rhs);
                        asmInstructions.add(asmInst);
                    }
                    break;
                }
                case ADD :{
                    IRVariable res = inst.getResult();
                    lhs = inst.getLHS();
                    rhs = inst.getRHS();
                    String r_res = ass.assignReg(res);
                    String r_lhs = ass.assignReg((IRVariable) lhs);
                    if (rhs.isImmediate()) {
                        String r_rhs = rhs.toString();
                        asmInst = AsmInstruction.createAddi(r_res, r_lhs, r_rhs);
                        asmInstructions.add(asmInst);
                    } else {
                        String r_rhs = ass.assignReg((IRVariable) rhs);
                        asmInst = AsmInstruction.createAdd(r_res, r_lhs, r_rhs);
                        asmInstructions.add(asmInst);
                    }
                    break;
                }
                case MOV :{
                    IRVariable res = inst.getResult();
                    String r_res = ass.assignReg(res);
                    IRValue from = inst.getFrom();
                    if (from.isImmediate()) {
                        asmInst = AsmInstruction.createLI(r_res, from.toString());
                        asmInstructions.add(asmInst);
                    }
                    else {
                        String r_from = ass.assignReg((IRVariable) from);
                        asmInst = AsmInstruction.createMV(r_res, r_from);
                        asmInstructions.add(asmInst);
                    }
                    break;
                }
                case RET :{
                    String r_ret = ass.getReturnReg();
                    IRValue res = inst.getReturnValue();
                    if (res.isImmediate()) {
                        String r_res = res.toString();
                        asmInst = AsmInstruction.createLI(r_ret, r_res);
                        asmInstructions.add(asmInst);
                    } else {
                        String r_res = ass.assignReg((IRVariable) res);
                        asmInst = AsmInstruction.createMV(r_ret, r_res);
                        asmInstructions.add(asmInst);
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected value: " + kind);
                }
            }

            if(isRs1LastUse.get(i)) {
                ass.freeReg((IRVariable) inst.getAllOperands().get(0));
            }
            if (isRs2LastUse.get(i)) {
                ass.freeReg((IRVariable) inst.getAllOperands().get(1));
            }
        }
    }

    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines("data/out/intermediate_code_s2.txt",
                processedInstructions.stream().map(Instruction::toString).toList());
        asmInstructions.add(0, AsmInstruction.createStart());
        FileUtils.writeLines(path, asmInstructions.stream().map(AsmInstruction::toString).toList());
    }
}

