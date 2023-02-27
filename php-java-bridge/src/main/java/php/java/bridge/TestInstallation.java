package php.java.bridge;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Used only for release tests
 */
public class TestInstallation {
    public static void main(String[] args) throws ScriptException, IOException {
	ScriptEngine e = new ScriptEngineManager()
	        .getEngineByExtension("phtml");
	OutputStream out = new ByteArrayOutputStream();
	OutputStream err = new ByteArrayOutputStream();

	e.getContext().setWriter(new OutputStreamWriter(out));
	e.getContext().setErrorWriter(new OutputStreamWriter(err));

	e.eval("<?php echo new java('java.lang.String', 'hello php from java');");

	((Closeable) e).close();

	if ("hello php from java".equals(out.toString())) {
	    System.out.println("installation okay");
	} else {
	    System.err.println("err: " + err.toString());
	    System.out.println("out: " + out.toString());
	}

	ClassLoader loader = TestInstallation.class.getClassLoader();
	InputStream in = loader.getResourceAsStream("WEB-INF/lib/JavaBridge.jar");
	extractFile(in, new File("JavaBridge.jar").getAbsoluteFile());
	in.close();

	System.exit(0);
    }

    private static void extractFile(InputStream in, File target)
            throws IOException {
		byte[] buf = new byte[8192];
		FileOutputStream out = new FileOutputStream(target);
		int c;
		while ((c = in.read(buf)) != -1) {
		    out.write(buf, 0, c);
		}
		out.close();
    }

}
