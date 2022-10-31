package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRVariable;

import java.util.ArrayList;
import java.util.List;

public class RegisterAssigner {
    private final String returnReg = "a0";
    private List<String> freeRegs = new ArrayList<>();
    private BMap<IRVariable, String> regMap = new BMap<>();
    public RegisterAssigner() {
        freeRegs.add("t0");
        freeRegs.add("t1");
        freeRegs.add("t2");
        freeRegs.add("t3");
        freeRegs.add("t4");
        freeRegs.add("t5");
        freeRegs.add("t6");
    }
    public String getReturnReg() {
        return returnReg;
    }

    public String assignReg(IRVariable var) {
        if (regMap.containsKey(var)) {
            System.out.println("get %s at reg %s".formatted(var.toString(), regMap.getByKey(var)));
            return regMap.getByKey(var);
        }
        if(freeRegs.size() < 1) {
            System.out.println("!! NOT ENOUGH FREE REGISTERS!!");
            throw new NotImplementedException();
        }
        String assReg = freeRegs.get(0);
        freeRegs.remove(0);
        regMap.replace(var, assReg);
        System.out.println("assign %s at reg %s".formatted(var.toString(), regMap.getByKey(var)));
        return assReg;
    }

    public void freeReg(IRVariable var) {
        System.out.println("free %s at reg %s".formatted(var.toString(), regMap.getByKey(var)));
        if(regMap.containsKey(var)) {
            freeRegs.add(regMap.getByKey(var));
            regMap.removeByKey(var);
        }
    }

}
