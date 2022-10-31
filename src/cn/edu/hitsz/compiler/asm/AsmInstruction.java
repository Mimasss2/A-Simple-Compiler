package cn.edu.hitsz.compiler.asm;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * riscv汇编指令类
 */
public class AsmInstruction {
    public static AsmInstruction createAdd(String result, String lhs, String rhs) {
        return new AsmInstruction(AsmInstructionKind.ADD, result, List.of(lhs, rhs));
    }

    public static AsmInstruction createSub(String result, String lhs, String rhs) {
        return new AsmInstruction(AsmInstructionKind.SUB, result, List.of(lhs, rhs));
    }

    public static AsmInstruction createMul(String result, String lhs, String rhs) {
        return new AsmInstruction(AsmInstructionKind.MUL, result, List.of(lhs, rhs));
    }

    public static AsmInstruction createAddi(String result, String lhs, String rhs) {
        return new AsmInstruction(AsmInstructionKind.ADDI, result, List.of(lhs, rhs));
    }

    public static AsmInstruction createSubi(String result, String lhs, String rhs) {
        return new AsmInstruction(AsmInstructionKind.SUBI, result, List.of(lhs, rhs));
    }

    public static AsmInstruction createLW(String result, String base, String offset) {
        return new AsmInstruction(AsmInstructionKind.LW, result, List.of(base, offset));
    }

    public static AsmInstruction createSW(String result, String base, String offset) {
        return new AsmInstruction(AsmInstructionKind.SW, result, List.of(base, offset));
    }

    public static AsmInstruction createMV(String result, String from) {
        return new AsmInstruction(AsmInstructionKind.MV, result, List.of(from));
    }
    public static AsmInstruction createNEG(String result, String source) {
        return new AsmInstruction(AsmInstructionKind.NEG, result, List.of(source));
    }

    public static AsmInstruction createLI(String result, String imm) {
        return new AsmInstruction(AsmInstructionKind.LI, result, List.of(imm));
    }

    /**
     * @return ".text" 汇编代码段开始标识符
     */
    public static AsmInstruction createStart() {
        return new AsmInstruction(AsmInstructionKind.START, null, null);
    }


    //============================== 基础设施 ==============================
    @Override
    public String toString() {
        if(kind == AsmInstructionKind.START) {
            return ".text\n";
        }

        final var kindString = kind.toString().toLowerCase();
        final var resultString = result == null ? "" : result.toString();
        var operandsString = "";
        switch (kind) {
            case LW, SW -> {
                operandsString = "%s(%s)".formatted(operands.get(1), operands.get(0));
            }
            case ADD, SUB, MUL, ADDI, SUBI, MV, NEG, LI -> operandsString = operands.stream().map(Objects::toString).collect(Collectors.joining(", "));
        }
        return "\t%s %s, %s".formatted(kindString, resultString, operandsString);
    }


    private AsmInstruction(AsmInstructionKind kind, String result, List<String> operands) {
        this.kind = kind;
        this.result = result;
        this.operands = operands;
    }

    private final AsmInstructionKind kind;
    private final String result;
    private final List<String> operands;

}
