package cpu.fpu;

import util.DataType;
import util.IEEE754Float;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        String result_Exp = "";
        String result_S = "";
        String s1 = "";
        String s2 = "";

        // 处理边界情况1：NaN
        if (number1.matches(IEEE754Float.NaN_Regular) || number2.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // 处理边界情况2
        if (cornerCheck(addCorner, number1, number2) != null){
            return new DataType(cornerCheck(addCorner, number1, number2));
        }

        // 提取符号，阶码，尾数
        String exp1 = number1.substring(1,9);
        String exp2 = number2.substring(1,9);

        String sign = "";
        if (compareExp(exp1, exp2) == 1) sign = number1.substring(0, 1);
        if (compareExp(exp1, exp2) == -1) {
            sign = number2.substring(0, 1);
        }
        else{
            if (Integer.parseInt(number1.substring(9), 2) > Integer.parseInt(number2.substring(9), 2)) sign = number1.substring(0, 1);
            else sign = number2.substring(0, 1);
        }


        // 提取尾数要增加保护位
        if (number1.charAt(0) == '1'){
            for (int i = 9; i < 32; i++){
                if (number1.charAt(i) == '1') s1 = s1 + "0";
                else s1 = s1 + "1";
            }
            //s1 = ALU_add(s1, "00000000000000000000001");
        }
        else s1 = number1.substring(9);
        if (number2.charAt(0) == '1'){
            for (int i = 9; i < 32; i++){
                if (number2.charAt(i) == '1') s2 = s2 + "0";
                else s2 = s2 + "1";
            }
            //s2 = ALU_add(s2, "00000000000000000000001");
        }
        else s2 = number2.substring(9);

        if (exp1.equals("00000000")) {
            s1 = "0" + s1 + "000";
        }
        else{
            s1 = "1" + s1 + "000";
        }
        if (exp2.equals("00000000")) {
            s2 = "0" + s2 + "000";
        }
        else{
            s2 = "1" + s2 + "000";
        }

        // 模拟运算得到中间结果
        // 1:对阶 小阶向大阶看齐，使x和y的阶码相等，小阶的尾数右移两个阶的差的绝对值
        if (compareExp(exp1, exp2) == 1){// exp1 > exp2
            int temp = Integer.parseInt(exp1,2) - Integer.parseInt(exp2,2);
            result_Exp = exp1;
            s2 = rightShift(s2, temp);
        }
        if (compareExp(exp1, exp2) == -1){
            int temp = Integer.parseInt(exp2,2) - Integer.parseInt(exp1,2);
            result_Exp = exp2;
            s1 = rightShift(s1, temp);
        }
        if (compareExp(exp1, exp2) == 0) result_Exp = exp1;


        // 2:尾数相加
        int carry = 0;
        for (int i = 26; i > -1; i--){
            switch (Integer.parseInt(String.valueOf(s1.charAt(i))) + Integer.parseInt(String.valueOf(s2.charAt(i)))){
                case 0:
                    result_S = carry + result_S;
                    carry = 0;
                    break;
                case 1:
                    if (carry == 0){
                        result_S = 1 + result_S;
                    }

                    else{
                        result_S = 0 + result_S;
                    }
                    break;
                case 2:
                    if (carry == 0){
                        result_S = 0 + result_S;
                    }
                    else{
                        result_S = 1 + result_S;
                    }
                    carry = 1;
            }
        }
        if (carry == 1) result_S = "1" + result_S;
        if (result_S.length() > 27){
            result_S = rightShift(result_S, 1).substring(1);
            result_Exp = ALU_add(result_Exp, "00000001");
            if (result_Exp.length() > 8 && sign.equals("0")) return new DataType(IEEE754Float.P_ZERO);
            if (result_Exp.length() > 8 && sign.equals("1")) return new DataType(IEEE754Float.N_ZERO);
        }

        // 规格化并判断阶码溢出情况
        if (result_Exp.equals("00000000")){
            String result = round(sign.charAt(0), "00000000", result_S);
            result = sign + "00000000" + result.substring(9);
            return new DataType(result);
        }

        int cnt = 0;
        while(result_S.charAt(cnt) != '1'){
            cnt++;
        }

        for (int i = 0; i < cnt; i++){
            if (result_Exp.equals("00000000")){
                return new DataType(round(sign.charAt(0), "00000000" , result_S));
            }
            result_Exp = ALU_add(result_Exp, "11111111");
            result_S = result_S.substring(1) + "0";
        }

        if (result_Exp.equals("11111111")){ // 阶码溢出
            String result = round(sign.charAt(0), result_Exp, result_S);
            if (result.substring(9).equals("10000000000000000000000")) return new DataType(IEEE754Float.NaN);
            if (result.substring(9).equals("00000000000000000000000") && sign.equals("0")) return  new DataType(IEEE754Float.P_INF);
            if (result.substring(9).equals("00000000000000000000000") && sign.equals("1")) return  new DataType(IEEE754Float.N_INF);
        }

        // 舍入并返回
        String x = round(sign.charAt(0), result_Exp, result_S);
        return new DataType(x);
    }

    public int compareExp(String exp1, String exp2){
        int n1 = Integer.parseInt(exp1,2);
        int n2 = Integer.parseInt(exp2,2);
        if (n1 > n2) return 1;
        if (n1 < n2) return -1;
        return 0;
    }

    public String ALU_add (String x, String y){
         int carry = 0;
         String result = "";
         for (int i = x.length() - 1; i > -1; i--){
             switch (Integer.parseInt(String.valueOf(x.charAt(i))) + Integer.parseInt((String.valueOf(y.charAt(i))))){
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
         if (carry == 1) result = "1" + result;
         return result;
     }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String s1 = "";
        for (int i = 9; i < 32; i++){
            if (number1.charAt(i) == '1') s1 = s1 + "0";
            else s1 = s1 + "1";
        }
        s1 = ALU_add(s1, "00000000000000000000001");
        if (number1.charAt(0) == '1'){
            number1 = "0" + number1.substring(1, 9) + s1;
        }
        else number1 = "1" + number1.substring(1, 9) + s1;
        return add(new DataType(number1), dest);
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24), 2);
        String sig = sig_grs.substring(0, 24);
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
