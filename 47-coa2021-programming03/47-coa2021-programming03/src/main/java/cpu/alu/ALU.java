package cpu.alu;

import util.DataType;

import java.util.ArrayList;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    DataType remainderReg;

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        int carry = 0;
        String result = "";
        String number1 = src.toString();
        String number2 = dest.toString();
        for (int i = 31; i > -1; i--){
            switch (Integer.parseInt(number1.substring(i, i + 1)) + Integer.parseInt(number2.substring(i, i + 1))){
                case 0:
                    result = carry + result;
                    carry = 0;
                    break;
                case 1:
                    if (carry == 0){
                        result = 1 + result;
                    }
                    else{
                        result = 0 + result;
                    }
                    break;
                case 2:
                    if (carry == 0){
                        result = 0 + result;
                    }
                    else{
                        result = 1 + result;
                    }
                    carry = 1;
            }
        }

        if (result.length() > 32){
            result = result.substring(1);
        }
        DataType resultList = new DataType(result);
        return resultList;
    }


    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        String reverseNumber1 = "";
        for (int i = 0; i < 32; i++){
            if (number1.charAt(i) == '1') reverseNumber1 = reverseNumber1 + "0";
            else reverseNumber1 = reverseNumber1 + "1";
        }
        reverseNumber1 = add(new DataType(reverseNumber1), new DataType("00000000000000000000000000000001")).toString();
        reverseNumber1 = addZero(reverseNumber1, 32);
        return add(new DataType(number2), new DataType(reverseNumber1));
    }


    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        //TODO
        String number1 = src.toString();
        String number2 = dest.toString() + "0";
        String reverseNumber1 = "";
        for (int i = 0; i < 32; i++){
            if (number1.charAt(i) == '1') reverseNumber1 = reverseNumber1 + "0";
            else reverseNumber1 = reverseNumber1 + "1";
        }
        reverseNumber1 = add(new DataType(reverseNumber1), new DataType("00000000000000000000000000000001")).toString();
        String X = "";
        for (int i = 0; i < reverseNumber1.length(); i++){
            X += "0";
        }

        // 开始逐步计算乘法：布斯算法:
        // 假设number2为Y，每次先得到Yi - Yi+1，进而得到X || -X || 0，加到X上
        // 后将X和Y都右移一位，X前面根据原来的0或1来补位，一共进行number1.length()次
        // 得到的结果为X + Y的除去最后的Y0位

        String finalString = "";
        for (int i = 0; i < number1.length(); i++){
            int symbol = Integer.parseInt(number2.substring(number2.length() - 1)) - Integer.parseInt(number2.substring(number2.length() - 2, number2.length() - 1));
            String addString_to_X = "";
            if (symbol == 1) { addString_to_X = number1; }
            else if (symbol == -1) { addString_to_X = reverseNumber1; }
            else {
                for (int j = 0; j < number1.length(); j++) { addString_to_X += "0"; }
            }

            X = add(new DataType(addString_to_X), new DataType(X)).toString();

            number2 = X.substring(X.length() - 1) + number2.substring(0, number1.length()); //Y的右移
            // X的右移
            if (X.charAt(0) == '0'){
                X = "0" + X.substring(0, X.length() - 1);
            }
            else{
                X = "1" + X.substring(0, X.length() - 1);
            }
        }

        finalString = number2.substring(0, number2.length() - 1);
        return new DataType(finalString);
    }

    public static String addZero (String x, int length) {
        if (x.charAt(0) == '1' && x.length() > 1) {
            for (int i = x.length(); i < length; i++) {
                x = "1" + x;
            }
        }
        else{
            for (int i = x.length(); i < length; i++) {
                x = "0" + x;
            }
        }
        return x;
    }

    /**
     * 返回两个二进制整数的除法结果
     * 请注意使用不恢复余数除法方式实现
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        //TODO
        String divisor = src.toString();                                 // 除数
        String dividend = dest.toString();                               // 被除数
        String remainder = "";                                           // 余数
        if (dividend.charAt(0) == '1'){
            while (remainder.length() < 32) remainder += "1";
        }
        else{
            while (remainder.length() < 32) remainder += "0";
        }
        String quotient = dividend;                                      // 商

        // 抛出除数为0的异常
        if (divisor.equals("00000000000000000000000000000000")) throw new ArithmeticException();

        // 余数    商    除数

        // 循环，判断余数和除数的符号，相同，则余数=余数-除数；后将余数和商同时左移; 最后左移后，还要差一步余数 = 余数 +/- 商；还差一步去掉商的最高位，如果被除数和除数符号不同，商末尾 + 1(解决办法：循环多做一步)
        if (dividend.charAt(0) == divisor.charAt(0)) {
            remainder = sub(new DataType(divisor), new DataType(remainder)).toString();
        }
        else {
            remainder = add(new DataType(remainder), new DataType(divisor)).toString();
        }

        for (int i = 0; i < 32; i++){
            if (remainder.charAt(0) == divisor.charAt(0)) {
                remainder = remainder.substring(1) + quotient.charAt(0);
                remainder = sub(new DataType(divisor), new DataType(remainder)).toString();
                quotient = quotient.substring(1) + "1";
            }
            else{
                remainder = remainder.substring(1) + quotient.charAt(0);
                remainder = add(new DataType(remainder), new DataType(divisor)).toString();
                quotient = quotient.substring(1) + "0";
            }
        }

        // 左移商，并判断商是否 + 1
        if (remainder.charAt(0) == divisor.charAt(0)) {
            quotient = quotient.substring(1) + "1";
        }
        else{
            quotient = quotient.substring(1) + "0";
        }
        if (dividend.charAt(0) != divisor.charAt(0)){
            quotient = add(new DataType(quotient), new DataType("00000000000000000000000000000001")).toString();
        }

        // 最后要调整余数
        if (remainder.charAt(0) != dividend.charAt(0)){
            if (dividend.charAt(0) == divisor.charAt(0)){
                remainder = add(new DataType(remainder), new DataType(divisor)).toString();
            }
            else{
                remainder = sub(new DataType(divisor), new DataType(remainder)).toString();
            }
        }

        // 处理特殊情况
        if (add(new DataType(remainder), new DataType(divisor)).toString().equals("00000000000000000000000000000000")){ // 8 / -4 或 -8 / 4, 商-1，余4
            remainder = "00000000000000000000000000000000";
            quotient = sub(new DataType("00000000000000000000000000000001"), new DataType(quotient)).toString();
        }
        if (remainder.equals(divisor)){                                                                                 // 8 / 4, 商1 余4
            remainder = "00000000000000000000000000000000";
            quotient = add(new DataType("00000000000000000000000000000001"), new DataType(quotient)).toString();
        }
        if (quotient.equals("00000000000000000000000000000000")){                                                       // 3 / -7，商是0，余数是被除数
            remainder = dividend;
        }
        if (dividend.equals("00000000000000000000000000000000")){                                                       // 被除数是0
            quotient = "00000000000000000000000000000000";
            remainder = "00000000000000000000000000000000";
        }

        remainderReg = new DataType(remainder);
        return new DataType(quotient);
    }

}
