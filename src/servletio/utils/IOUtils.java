package servletio.utils;

import java.io.*;

public class IOUtils {

  public static String toString(final InputStream in) throws IOException {
    final StringBuilder sb = new StringBuilder();
    try {
      final InputStreamReader isr = new InputStreamReader(in);
      final BufferedReader br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    } finally {
      in.close();
    }
    return sb.toString();
  }

  public static byte[] toByteArray(final InputStream input) throws IOException {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for (int n = input.read(buf); n != -1; n = input.read(buf)) {
      os.write(buf, 0, n);
    }
    return os.toByteArray();
  }
}
