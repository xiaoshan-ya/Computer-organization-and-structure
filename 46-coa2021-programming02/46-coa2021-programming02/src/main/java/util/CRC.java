package util;

import java.util.ArrayList;

public class CRC {

    /**
     * CRC计算器
     *
     * @param data       数据流
     * @param polynomial 多项式
     * @return CheckCode
     */
    public static char[] Calculate(char[] data, String polynomial) {
        //TODO
        int K = polynomial.length() - 1;
        ArrayList<Character> M = new ArrayList<>();
        for (int i = 0; i < K + data.length; i++){
            if (i < data.length) { M.add(data[i]); }
            else{
                M.add('0');
            }
        }

        String remainder = "";
        for (int cnt = 0; cnt < M.size();){
            if (remainder.length() != polynomial.length()){
                remainder =  remainder + M.get(cnt).toString();
                cnt++;
                continue;
            }
            remainder = remainderString(remainder, polynomial);
        }

        if (remainder.length() == polynomial.length()) { remainder = remainderString(remainder, polynomial); }
        remainder = addZero(remainder, polynomial.length() - 1);
        return remainder.toCharArray();
    }

    //获得两个二进制做模2运算后的余数,每一位做异或
    public static String remainderString (String remainder, String polynomial){
        String result = "";
        for (int i = 0; i < polynomial.length(); i++){
            String temp = Integer.toBinaryString(Integer.parseInt(remainder.substring(i, i + 1), 2) ^ Integer.parseInt(polynomial.substring(i, i + 1), 2));
            if (!temp.equals("0") || result.length() != 0){
                result = result + temp;
            }
        }
        return result;
    }

    public  static String addZero (String remainder, int length){
        while (remainder.length() != length){
            remainder = "0" + remainder;
        }
        return remainder;
    }

    /**
     * CRC校验器
     *
     * @param data       接收方接受的数据流
     * @param polynomial 多项式
     * @param CheckCode  CheckCode
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode) {
        //TODO
        String newData = "";
        String checkString = "";
        for (int i = 0; i < data.length; i++){
            newData = newData + data[i];
        }
        for (int i = 0; i < CheckCode.length; i++){
            newData = newData + CheckCode[i];
        }
        for (int i = 0; i < newData.length();){
            if (checkString.length() != polynomial.length()){
                checkString = checkString + newData.charAt(i);
                i++;
                continue;
            }
            checkString = remainderString(checkString, polynomial);
        }

        if (checkString.length() == polynomial.length()) { checkString = remainderString(checkString, polynomial); }
        checkString = addZero(checkString, polynomial.length() - 1);
        return checkString.toCharArray();
    }

}
