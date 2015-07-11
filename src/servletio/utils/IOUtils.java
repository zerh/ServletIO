package servletio.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
	
	public static String toString(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader inR = new InputStreamReader(in);
			BufferedReader buf = new BufferedReader(inR);
			String line;
			while ((line = buf.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			in.close();
		}
		return sb.toString();
	}
	
	public static byte[] toByteArray(InputStream input) throws IOException {
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
        	byte[] buf = new byte[1024];
        	for (int n = input.read(buf); n != -1; n = input.read(buf)) {
        	    os.write(buf, 0, n);
        	}
        	return os.toByteArray();
    	}
}
