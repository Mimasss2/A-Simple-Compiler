package cn.edu.hitsz.compiler.asm;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static AsmInstruction createStart() {
        return new AsmInstruction(AsmInstructionKind.START, null, null);
    }

    //============================== 不同种类 IR 的参数 getter ==============================
    public AsmInstructionKind getKind() {
        return kind;
    }

    public String getResult() {
        return result;
    }

//    public String getLHS() {
//        ensureKindMatch(Set.of(InstructionKind.ADD, InstructionKind.SUB, InstructionKind.MUL));
//        return operands.get(0);
//    }
//
//    public String getRHS() {
//        ensureKindMatch(Set.of(InstructionKind.ADD, InstructionKind.SUB, InstructionKind.MUL));
//        return operands.get(1);
//    }
//
//    public String getFrom() {
//        ensureKindMatch(Set.of(InstructionKind.MOV));
//        return operands.get(0);
//    }
//
//    public String getReturnValue() {
//        ensureKindMatch(Set.of(InstructionKind.RET));
//        return operands.get(0);
//    }


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

    public List<String> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    private AsmInstruction(AsmInstructionKind kind, String result, List<String> operands) {
        this.kind = kind;
        this.result = result;
        this.operands = operands;
    }

    private final AsmInstructionKind kind;
    private final String result;
    private final List<String> operands;

    private void ensureKindMatch(Set<AsmInstructionKind> targetKinds) {
        final var kind = getKind();
        if (!targetKinds.contains(kind)) {
            final var acceptKindsString = targetKinds.stream()
                    .map(AsmInstructionKind::toString)
                    .collect(Collectors.joining(","));

            throw new RuntimeException(
                    "Illegal operand access, except %s, but given %s".formatted(acceptKindsString, kind));
        }
    }
}
