package transformer;

public class Transformer {
    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        //TODO:
        int number = Integer.parseInt(numStr);
        StringBuilder result = new StringBuilder(Integer.toBinaryString(number));
        for (; result.length() < 32;){
            result.insert(0, "0");
        }
        return result.toString();
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
        return Integer.parseInt(binStr, 2) + "";
    }
    /**
     * The decimal number to its NBCD code
     * */
    public String decimalToNBCD(String decimalStr) {
        //TODO:
        String result = "";
        int number = Integer.parseInt(decimalStr);
        if (decimalStr.charAt(0) == '-') { result += "1101"; }
        else{
            result += "1100";
        }
        number = Math.abs(number);
        String tempString = "";
        int i;
        for (; number > 0; number /= 10){
            i = number % 10;
            String temp = Integer.toBinaryString(i);
            switch(temp.length()) {
                case 1:
                    temp = "000" + temp;
                    break;
                case 2:
                    temp = "00" + temp;
                    break;
                case 3:
                    temp = "0" + temp;
                    break;
            }
            tempString = temp + tempString;

        }
        for (;tempString.length() < 28;){
            tempString = "0" + tempString;
        }
        result = result + tempString;
        return result;
    }

    /**
     * NBCD code to its decimal number
     * */
    public String NBCDToDecimal(String NBCDStr) {
        //TODO:
        String number = "";
        String symbol = "";
        if (NBCDStr.charAt(3) == '1') { symbol = "-"; }
        NBCDStr = NBCDStr.substring(4);

        if (NBCDStr.length() == 0){ return "0"; }

        for (int i = 0; i < NBCDStr.length(); i += 4){
            if (!judgeAllZero(NBCDStr.substring(i, i + 4)) || number.length() != 0){
                number = number + Integer.parseInt(NBCDStr.substring(i, i + 4), 2);
            }
        }
        number = symbol + number;
        return number;
    }
    public static boolean judgeAllZero(String string){
        boolean judge = true;
        for (int i = 0; i < string.length(); i++){
            if (string.charAt(i) != '0'){
                judge = false;
                break;
            }
        }
        return judge;
    }

    /**
     * Float true value to binaryString
     * @param floatStr : The string of the float true value
     * */
    public String floatToBinary(String floatStr) {
        //TODO:
        if (floatStr.equals(Double.MAX_VALUE + "")) { return "+Inf"; }
        if (floatStr.equals(Double.MIN_VALUE + "")) { return "-Inf"; }
        String result = "";
        if (floatStr.charAt(0) == '-'){ result = "1"; }
        //String[] list = floatStr.split("\\.");
        //String integer = Integer.toBinaryString(Integer.parseInt(list[0]));
        String decimals = "";
        float number = Float.parseFloat(floatStr);
        String integer = (int) number + "";
        float weishu = number - Integer.parseInt(integer);
        while(decimals.length() < 22){
            weishu *= 2;
            if (weishu >= 1){
                decimals = "1" + decimals;
                weishu -= 1;
            }
            else{
                decimals= "0" + decimals;
            }
        }
        decimals = "1" + decimals;

        String S = "";
        String E = "";
        if (Integer.parseInt(integer) == 0){
            S = decimals;

            E = Integer.toBinaryString(Integer.parseInt(integer) - 127);
            while (E.length() < 8){
                E = "0" + E;
            }
        }
        else{
            S = decimals;
            E = Integer.toBinaryString(Integer.parseInt(integer) - 127);
            while (E.length() < 8){
                E = "0" + E;
            }
        }

        result = result + S + E;
        return result;
    }

    /**
     * Binary code to its float true value
     * */
    public String binaryToFloat(String binStr) {
        //TODO:
        float result;
        if (binStr.charAt(0) == 1){ result = -1; }
        else{ result = 1; }
        int cnt = Integer.parseInt(binStr.substring(1,9)) - 127;
        String integer = "1" + binStr.substring(9, cnt + 10);
        binStr = binStr.substring(cnt + 10);
        float number = Integer.parseInt(integer);
        for (int i = 0; i < binStr.length(); i++){
            number += Integer.parseInt(binStr.substring(i, i + 1)) * Math.pow(2, -i - 1);
        }
        result *= number;

        return result + "";
    }


}
