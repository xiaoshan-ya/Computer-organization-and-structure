package cpu.nbcdu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class NBCDUAddTest {

	private final NBCDU nbcdu = new NBCDU();
	private final Transformer transformer = new Transformer();
	private DataType src;
	private DataType dest;
	private DataType result;

	@Test
	public void AddTest1() {
		src = new DataType("11010001010100000001011000000011");
		dest = new DataType("11000001010100000001011000000001");
		result = nbcdu.add(src, dest);
		assertEquals("11010000000000000000000000000010", result.toString());
	}

	@Test
	public void AddRandomTest() {
		int a,b;
		Random r = new Random();
		for (int i = 0; i < 10000000; i++){
			a = r.nextInt(9999999 * 2) - 9999999;
			b = r.nextInt(9999999 * 2) - 9999999;
			src = new DataType(transformer.decimalToNBCD(String.valueOf(a)));
			dest = new DataType(transformer.decimalToNBCD(String.valueOf(b)));
			result = new DataType(transformer.decimalToNBCD(String.valueOf(a + b)));
			if (!result.toString().equals(nbcdu.add(src,dest).toString())) {
				System.out.println("src: " + src + "  " + a);
				System.out.println("dest: " + dest + "  " + b);
				System.out.println("expect: " + result + "  " + transformer.NBCDToDecimal(result.toString()));
				System.out.println("actual: " + nbcdu.add(src, dest) + "  " + transformer.NBCDToDecimal(nbcdu.add(src, dest).toString()));
				break;
			}
		}
	}

}
