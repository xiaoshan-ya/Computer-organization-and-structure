package cpu.instr.decode;

import cpu.CPU_State;
import cpu.MMU;
import cpu.registers.Register;
import memory.Memory;

public class Operand {
    private OperandType type;
    private String addr;
    private int sreg;
    private String val;
    private int data_size = 32;
    private MMU mmu = MMU.getMMU();

    public Operand() {
    }

    public OperandType getType() {
        return type;
    }

    public void setType(OperandType type) {
        this.type = type;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getSreg() {
        return sreg;
    }

    public void setSreg(int sreg) {
        this.sreg = sreg;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    // read the operand's value
    // put value back to its addr
    public void operandRead() {
        // TODO: this.addr 读取数据
        switch (this.type) {
            case OPR_MEM:
                //assert(opr->sreg == SREG_DS || opr->sreg == SREG_SS);
                // opr->val = vaddr_read(opr->addr, opr->sreg, 4);
                this.val = String.valueOf(mmu.read(this.addr, this.data_size));
                break;
            case OPR_IMM:
                this.val = String.valueOf(mmu.read(this.addr,4));
                break;
            case OPR_REG:
            case OPR_CREG:
            case OPR_SREG:
        }

        // deal with data size
        switch (this.data_size) {
            case 8:
            case 16:
            case 32:
                break;
            default:
                System.out.printf("Error: Operand data size = %u\n", this.data_size);
                break;
        }
    }

    // write the operand's value to the addr
    public void operandWrite() {
        switch(this.type) {
            case OPR_MEM:
                //TODO: this.value 写入DS寄存器
                CPU_State.ds.write(this.val);
                break;
            case OPR_REG:
            case OPR_IMM:
                System.out.println("Error: Cannot write to an immediate");
                break;
            case OPR_CREG:
        }
    }

    public class memAddr {
        int disp;  // hex
        int base;  // register
        int index; // register
        int scale; // 1, 2, 4, 8
    }

}
