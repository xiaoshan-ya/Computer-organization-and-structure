package cpu.instr;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.mmu.MMU;
import util.DataType;
import util.Transformer;

public class Jmp implements Instruction{
    private final MMU mmu = MMU.getMMU();
    private int len = 0;
    private String instr;
    ALU alu = new ALU();

    @Override
    public int exec(int opcode) {
        if (opcode == 0xE9) {
            len = 1 + 4;
            instr = String.valueOf(mmu.read(CPU_State.cs.read() + CPU_State.eip.read(), len));
            String rel = MMU.ToBitStream(instr.substring(1,5));
            String eip = CPU_State.eip.read();
            String result = alu.add(new DataType(rel), new DataType(eip)).toString();
            CPU_State.eip.write(result);
        }
        return len;
    }
}
