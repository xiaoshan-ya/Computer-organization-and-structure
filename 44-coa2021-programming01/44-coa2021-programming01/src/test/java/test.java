public class test {
    public static void main(String args[]){
        //浮点数转为二进制
        float f = 20.3f;
        int i = Float.floatToIntBits(f);
        System.out.println(Integer.toBinaryString(i));
    }
}
