package convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectByteConverter {
	
	public static byte[] serObject(Object o){
		ByteArrayOutputStream arrout = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		try {
			ObjectOutputStream objout = new ObjectOutputStream(arrout);
			objout.writeObject(o);
			
			b = arrout.toByteArray();
			arrout.close();
			objout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	public static Object deserObject(byte[] b){
		ByteArrayInputStream arr = new ByteArrayInputStream(b);
		ObjectInputStream ser;
		Object o = null;
		try {
			ser = new ObjectInputStream(arr);
			o = ser.readObject();
			arr.close();
			ser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

}
