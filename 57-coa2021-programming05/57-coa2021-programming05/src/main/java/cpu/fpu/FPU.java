package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {
    public static void main(String[] args) {
        final FPU fpu = new FPU();
        Transformer transformer = new Transformer();
        DataType src;
        DataType dest;
        DataType result;

        String deNorm1 = "00000000000000000000000000000001";
        String deNorm2 = "00000000000000000000000000000010";
        String deNorm3 = "10000000010000000000000000000000";
        String small1 = "00000000100000000000000000000000";
        String small2 = "00000000100000000000000000000001";
        String big1 = "01111111000000000000000000000001";
        String big2 = "11111111000000000000000000000001";
        String[] strings = {deNorm1, deNorm2, deNorm3, small1, small2, big1, big2};
        double[] doubles = {10000000, 1.2, 1.1, 1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, -0.1, -0.2, -0.3, -0.4, -0.5, -0.6, -0.7, -0.8, -0.9, -1, -10000000};

        float[] input = new float[strings.length + doubles.length];
        for (int i = 0; i < strings.length; i++) {
            input[i] = Float.parseFloat(transformer.binaryToFloat(strings[i]));
        }
        for (int i = 0; i < doubles.length; i++) {
            input[i + strings.length] = (float) doubles[i];
        }

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length; j++) {
                src = new DataType(transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[i]))));
                dest = new DataType(transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[j]))));
                result = fpu.mul(src, dest);
                String expect = transformer.intToBinary(Integer.toString(Float.floatToIntBits(input[i] + input[j])));
                if (!expect.equals(result.toString())) {
                    System.out.println("i = " + i + ", j = " + j);
                    System.out.println("src: " + src);
                    System.out.println("dest:" + dest);
                    System.out.println("Expect: " + expect);
                    System.out.println("Actual: " + result);
                    System.out.println();
                }
            }
        }
    }

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */

    ALU calculator = new ALU();
    public DataType mul(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        String exp1 = number1.substring(1,9);
        String exp2 = number2.substring(1,9);
        String s1 = number1.substring(9);
        String s2 = number2.substring(9);
        if (exp1.equals("00000000")) {
            exp1 = "00000001";
        }
        if (exp2.equals("00000000")) {
            exp2 = "00000001";
        }
        String result_S = "";
        String result_Exp = "";
        String result_Symbol = "";

        // 处理边界情况1：NaN
        if (number1.matches(IEEE754Float.NaN_Regular) || number2.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // 处理边界情况2
        if (cornerCheck(mulCorner, number1, number2) != null){
            return new DataType(cornerCheck(mulCorner, number1, number2));
        }

        // 提取尾数
        if (exp1.equals("00000001")) {
            s1 = "0" + s1 + "000";
        }
        else{
            s1 = "1" + s1 + "000";
        }
        if (exp2.equals("00000001")) {
            s2 = "0" + s2 + "000";
        }
        else{
            s2 = "1" + s2 + "000";
        }

        // 计算符号位
        char symbol1 = number1.charAt(0);
        char symbol2 = number2.charAt(0);
        if (symbol1 == symbol2) result_Symbol = "0";
        else result_Symbol = "1";

        // 尾数相乘

            String X = s1; // 27位
            String Y = "";
            for (int i = 26; i > -1; i--){
                if (s2.charAt(i) == '0'){
                    Y = X.charAt(26) + Y;
                    X = "0" + X.substring(0, 26);
                }
                else{
                    X = calculator.add(new DataType("00000" + X), new DataType("00000" + s1)).toString().substring(5);
                    Y = X.charAt(26) + Y;
                    X = "0" + X.substring(0, 26);
                }
            }
            result_S = X + Y;


        // 计算阶码
        while (exp1.length() < 32){
            exp1 = "0" + exp1;
            exp2 = "0" + exp2;
        }
        result_Exp = calculator.add(new DataType(exp1), new DataType(exp2)).toString();
        result_Exp = calculator.sub(new DataType("00000000000000000000000001111111"), new DataType(result_Exp)).toString();// 用32位，这样可以表示出正负
        result_Exp = calculator.add(new DataType("00000000000000000000000000000001"), new DataType(result_Exp)).toString();
        if (result_Exp.charAt(0) == '1'){
            if (result_Symbol.charAt(0) == '0') return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        }


        // 尾数的规格化
        while (result_S.charAt(0) == '0' && Integer.parseInt(result_Exp, 2) > 0){
            result_S = result_S.substring(1) + "0";
            result_Exp = calculator.sub(new DataType("00000000000000000000000000000001"), new DataType(result_Exp)).toString();
        }
        boolean judge = judgeAllZero(result_S);
        int cnt = 0;
        while (Integer.parseInt(result_Exp, 2) < 0 && judge){
            cnt++;
            result_Exp = calculator.add(new DataType(result_Exp), new DataType("00000000000000000000000000000001")).toString();
            result_S = rightShift(result_S, 1);
            if (judgeAllZero(result_S) || result_Exp.equals("00000000000000000000000000000000") || cnt == 54) break;
        }

        if (result_Exp.charAt(0) == 1){
            if (result_Symbol.equals("1")) return  new DataType(IEEE754Float.N_ZERO);
            else return new DataType(IEEE754Float.P_ZERO);
        }

        result_Exp = result_Exp.substring(24);

        if (result_Exp.equals("11111111") && result_Symbol.equals("1")) return new DataType(IEEE754Float.N_INF);
        if (result_Exp.equals("11111111") && result_Symbol.equals("0")) return new DataType(IEEE754Float.P_INF);
        if (result_Exp.equals("00000000")){
            result_S = rightShift(result_S, 1);
            return new DataType(round(result_Symbol.charAt(0), "00000000", result_S));
        }
            String x = round(result_Symbol.charAt(0), result_Exp, result_S);
            return new DataType(round(result_Symbol.charAt(0), result_Exp, result_S));
    }




    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
        String number1 = src.toString();
        String number2 = dest.toString();
        String exp1 = number1.substring(1,9);
        String exp2 = number2.substring(1,9);
        String s1 = number1.substring(9);
        String s2 = number2.substring(9);
        String result_S = "";
        String result_Exp = "";
        String result_Symbol = "";

        // 处理边界情况1：NaN
        if (number1.matches(IEEE754Float.NaN_Regular) || number2.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);
        }
        // 处理边界情况2
        if (cornerCheck(divCorner, number1, number2) != null){
            return new DataType(cornerCheck(divCorner, number1, number2));
        }
        if (number1.equals("00000000000000000000000000000000")) throw new ArithmeticException();

        // 提取尾数
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

        // 计算符号位
        char symbol1 = number1.charAt(0);
        char symbol2 = number2.charAt(0);
        if (symbol1 == symbol2) result_Symbol = "0";
        else result_Symbol = "1";

        // 尾数相除 s2 / s1,用余数减去除数  余数 商
        String divisor = s1;                                 // 除数
        String remainder = "0" + s2;                         // 余数
        String quotient = "000000000000000000000000000";     // 商 :27位
        for (int i = 0; i < 27; i++){
            if (calculator.sub(new DataType("00000" + divisor), new DataType("0000" + remainder)).toString().charAt(0) == '0'){
                remainder = calculator.sub(new DataType("00000" +divisor), new DataType("0000" +remainder)).toString().substring(4);
                remainder = remainder.substring(1) + quotient.charAt(0);
                quotient = quotient.substring(1) + "1";
            }
            else{
                remainder = remainder.substring(1) + quotient.charAt(0);
                quotient = quotient.substring(1) + "0";
            }
        }
        result_S = quotient;

        // 计算阶码
        while (exp1.length() < 32){
            exp1 = "0" + exp1;
            exp2 = "0" + exp2;
        }
        result_Exp = calculator.sub(new DataType(exp1), new DataType(exp2)).toString();
        result_Exp = calculator.add(new DataType("00000000000000000000000001111111"), new DataType(result_Exp)).toString();// 用32位，这样可以表示出正负
        //result_Exp = Integer.toBinaryString(Integer.parseInt(exp2, 2) - Integer.parseInt(exp1, 2) + 127);
        if (result_Exp.charAt(0) == '1'){
            if (result_Symbol.equals("0")) return new DataType(IEEE754Float.P_ZERO);
            return new DataType(IEEE754Float.N_ZERO);
        }
        // 尾数规格化
        int cnt = 0;
        while (result_S.charAt(0) != '1'){
            cnt++;
            result_S = result_S.substring(1) + "0";
            result_Exp = calculator.sub(new DataType("00000000000000000000000000000001"), new DataType(result_Exp)).toString();
            if (result_Exp.equals("00000000000000000000000000000000")){
                String x = round(result_Symbol.charAt(0), "00000000", result_S);
                return new DataType(round(result_Symbol.charAt(0), "00000000", result_S));
            }
            if (cnt > 27) break;
        }

        result_Exp = result_Exp.substring(24);
        if (result_Exp.equals("00000001")) result_Exp = "00000000";
        String x = round(result_Symbol.charAt(0), "00000000", result_S);
        return new DataType(round(result_Symbol.charAt(0), result_Exp, result_S));
    }

    public boolean judgeAllZero (String x){
        for (int i = 0; i < 27; i++){
            if (x.charAt(i) == '0') return false;
        }
        return true;
    }

    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
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
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return IEEE754Float.P_INF;
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
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
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
