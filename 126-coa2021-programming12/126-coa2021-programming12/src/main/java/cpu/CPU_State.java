package cpu;

import cpu.registers.*;
import cpu.registers.Register;

public class CPU_State {

    // 标志寄存器
    public static Register eflags = new EFLAGS();

    // 指令指针寄存器
    public static Register eip = new EIP(); // 32位

    // 通用寄存器
    public static Register eax = new Register32(); // 通常用来执行加法，函数调用的返回值一般也放在这里面
    public static Register ecx = new Register32(); // 通常用作for循环的计数器
    public static Register edx = new Register32(); // 读取I/O端口时，存放端口号
    public static Register ebx = new Register32(); // 通常用来数据存取
    public static Register esp = new Register32(); // 栈顶指针，指向栈的顶部
    public static Register ebp = new Register32(); // 栈底指针，指向栈的底部，用ebp+偏移量的形式来定位函数存放在栈中的局部变量

    // 段寄存器
    public static Register cs = new Register16(); // 代码段寄存器,存放当前正在运行的程序代码所在段的段基值
    public static Register ds = new Register16(); // 数据段寄存器,指出当前程序使用的数据所存放段的最低地址，即存放数据段的段基值
    public static Register ss = new Register16(); // 堆栈段寄存器,指出当前堆栈的底部地址，即存放堆栈段的段基值

}
