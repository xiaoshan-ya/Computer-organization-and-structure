package cpu.nbcdu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class NBCDUSubTest {

    private final NBCDU nbcdu = new NBCDU();
    private final Transformer transformer = new Transformer();
    private DataType src;
    private DataType dest;
    private DataType result;

    @Test
    public void SubTest1() {
        src = new DataType("11001001011101110110011001000010");
        dest = new DataType("11000011100000100111010001100000");
        result = nbcdu.sub(src, dest);
        assertEquals("11010101100101001001000110000010", result.toString());
    }

    @Test
    public void SubRandomTest(){
        int a,b;
        Random r = new Random();
        for (int i = 0; i < 10000000; i++){
            a = r.nextInt(9999999);
            b = r.nextInt(9999999);
            src = new DataType(transformer.decimalToNBCD(String.valueOf(a)));
            dest = new DataType(transformer.decimalToNBCD(String.valueOf(b)));
            result = new DataType(transformer.decimalToNBCD(String.valueOf(b - a)));
            if (!result.toString().equals(nbcdu.sub(src,dest).toString())) {
                System.out.println("src: " + src + "  " + a);
                System.out.println("dest: " + dest + "  " + b);
                System.out.println("expect: " + result + "  " + transformer.NBCDToDecimal(result.toString()));
                System.out.println("actual: " + nbcdu.add(src, dest) + "  " + transformer.NBCDToDecimal(nbcdu.add(src, dest).toString()));
                break;
            }
        }
    }

}
