package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;

public class NBCDU {

    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    ALU alu = new ALU();
    DataType add(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        char symbol1 = number1.charAt(3);
        char symbol2 = number2.charAt(3);
        String n1 = number1.substring(4);
        String n2 = number2.substring(4);
        String result_Symbol = "";
        String result = "";

        if (symbol1 != symbol2) {
            if (symbol1 == '1') {
                return sub(new DataType("1100" + n1),dest);
            }
            else {
                return sub(new DataType("1100" + n2),src);
            }
        }

        result_Symbol = "110" + symbol1;

        int carry = 0;
        for (int i = 27; i >= 3; i -= 4){
            int temp = Integer.parseInt(n1.substring(i - 3, i + 1), 2) + Integer.parseInt(n2.substring(i - 3, i + 1), 2);
            temp += carry;
            if (temp <= 9) {
                result = addZero(Integer.toBinaryString(temp), 4) + result;
                carry = 0;
            }
            else if (temp >= 10 && temp <= 15) {
                String tempString = addZero(Integer.toBinaryString(temp + 6), 5);
                result = tempString.substring(1) + result;
                carry = Integer.parseInt(tempString.charAt(0) + "");
            }
            else{
                String tempString = addZero(Integer.toBinaryString(temp + 6), 5);
                result = tempString.substring(1) + result;
                carry = 1;
            }
        }

        result = result_Symbol + result;
        return new DataType(result);
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        char symbol1 = number1.charAt(3);
        char symbol2 = number2.charAt(3);
        String n1 = number1.substring(4);
        String n2 = number2.substring(4);
        String result_Symbol = "1100";
        String result = "";

        if (symbol1 == '1' && symbol2 == '0') return add(new DataType("1100" + n1), new DataType(number2));
        if (symbol1 == '1' && symbol2 == '1') return sub(new DataType("1100" + n2), new DataType("1100" + n1));
        if (symbol1 == '0' && symbol2 == '1') return add(new DataType("1101" + n1), new DataType("1101" + n2));

        // 判断符号的正负
        for (int i = 0; i < 28; i++){
            if (n2.charAt(i) == '1' && n1.charAt(i) == '0') {
                result_Symbol = "1100";
                break;
            }
            if (n2.charAt(i) == '0' && n1.charAt(i) == '1') {
                result_Symbol = "1101";
                break;
            }
        }
        //相减为负数时
        if (result_Symbol.equals("1101")){
            String temp = n1;
            n1 = n2;
            n2 = temp;
        }

        int carry = 1;
        int cnt = 0;
        for (int i = 27; i >= 3; i -= 4){
            cnt++;
            int temp = Integer.parseInt(n2.substring(i - 3, i + 1), 2) + 9 - Integer.parseInt(n1.substring(i - 3, i + 1), 2);
            temp += carry;
            if (temp <= 9) {
                result = addZero(Integer.toBinaryString(temp), 4) + result;
                carry = 0;
            }
            else if (temp >= 10 && temp <= 15) {
                String tempString = addZero(Integer.toBinaryString(temp + 6), 5);
                result = tempString.substring(1) + result;
                carry = Integer.parseInt(tempString.charAt(0) + "");
            }
            else{
                String tempString = addZero(Integer.toBinaryString(temp + 6), 5);
                result = tempString.substring(1) + result;
                carry = 1;
            }
        }

        int x = result.length();
        result = result_Symbol + result;

        return new DataType(result);
    }

    public String addZero (String x, int length){
        for (int i = x.length(); i < length; i++) x = "0" + x;
        return x;
    }

}
