/*-*- mode: Java; tab-width:8 -*-*/

package php.java.fastcgi;

import java.io.IOException;
import java.io.InputStream;

import php.java.bridge.util.NotImplementedException;

public class FCGIInputStream extends InputStream {
    private InputStream in;

    public FCGIInputStream(InputStream in) {
	super();
	this.in = in;
    }

    /** {@inheritDoc} */
    public int read() throws ConnectionException {
	throw new NotImplementedException();
    }

    public int available() throws IOException {
	return in.available();
    }
    
    public int read(byte buf[]) throws ConnectionException {
	try {
	    return doRead(buf);
	} catch (IOException ex) {
	    throw new ConnectionException(ex);
	}
    }

    /** {@inheritDoc} */
    public int read(byte buf[], int off, int buflength)
            throws ConnectionException {
	try {
	    return in.read(buf, off, buflength);
	} catch (IOException ex) {
	    throw new ConnectionException(ex);
	}
    }

    public void close() throws ConnectionException, FCGIProcessException {
	// ignore
    }

    private byte header[] = new byte[FCGIUtil.FCGI_HEADER_LEN];

    private int doRead(byte buf[]) throws IOException {
    	int n, i;
    	for (n = 0; (i = read(header, n, FCGIUtil.FCGI_HEADER_LEN - n)) > 0;)
    	    n += i;
    	if (FCGIUtil.FCGI_HEADER_LEN != n)
    	    throw new IOException("Protocol error");

	
	int type = header[1] & 0xFF;
	int contentLength = ((header[4] & 0xFF) << 8) | (header[5] & 0xFF);
	int paddingLength = header[6] & 0xFF;
	switch (type) {
	case FCGIUtil.FCGI_STDERR:
	case FCGIUtil.FCGI_STDOUT: {
	    for (n = 0; (i = read(buf, n, contentLength - n)) > 0;)
		n += i;
	    if (n != contentLength)
		throw new IOException("Protocol error while reading FCGI data");
	    if (paddingLength > 0) {
		byte b[] = new byte[paddingLength];
		for (n = 0; (i = read(b, n, b.length - n)) > 0;)
		    n += i;
		if (n != paddingLength)
		    throw new IOException(
		            "Protocol error while reading FCGI padding");
	    }
	    if (type == FCGIUtil.FCGI_STDERR) {
		return contentLength * -1 - 1;
	    }
	    return contentLength;
	}
	case FCGIUtil.FCGI_END_REQUEST: {
	    for (n = 0; (i = read(buf, n, contentLength - n)) > 0;)
		n += i;
	    if (n != contentLength)
		throw new IOException("Protocol error while reading EOF data");
	    if (paddingLength > 0) {
			for (n = 0; (i = read(buf, n, buf.length - n)) > 0;)
			    n += i;
			if (n != paddingLength)
			    throw new IOException(
			            "Protocol error while reading EOF padding");
	    }
	    return -1;
	}
	}
	throw new IOException("Received unknown type");
    }
}