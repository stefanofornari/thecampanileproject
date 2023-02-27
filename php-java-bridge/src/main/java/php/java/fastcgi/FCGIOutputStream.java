/*-*- mode: Java; tab-width:8 -*-*/

package php.java.fastcgi;

/*
 * Copyright (C) 2017 Jost Bökemeier
 *
 * The PHP/Java Bridge ("the library") is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either
 * version 2, or (at your option) any later version.
 *
 * The library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the PHP/Java Bridge; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA.
 *
 * Linking this file statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import php.java.bridge.Util;
import php.java.bridge.util.NotImplementedException;

public class FCGIOutputStream extends OutputStream {
    private OutputStream out;
    private int id;

    public FCGIOutputStream(OutputStream out) {
	super();
	this.out = out;
    }

    public void write(int type, byte buf[]) throws ConnectionException {
	write(type, buf, buf.length);
    }

    /**
     * Write a FCGI packet
     * 
     * @param type
     *            the packet type
     * @param buf
     *            the output buffer
     * @param length
     *            the packet length
     * @throws ConnectionException
     */
    public void write(int type, byte buf[], int length)
            throws ConnectionException {
	int requestId = id;
	byte[] header = new byte[] { 1, (byte) type,
	        (byte) ((requestId >> 8) & 0xff), (byte) ((requestId) & 0xff),
	        (byte) ((FCGIUtil.FCGI_BUF_SIZE >> 8) & 0xff),
	        (byte) ((FCGIUtil.FCGI_BUF_SIZE) & 0xff), 0, // padding
	        0 };
	int contentLength = length;
	int pos = 0;
	while (pos + FCGIUtil.FCGI_BUF_SIZE <= contentLength) {
	    write(header);
	    write(buf, pos, FCGIUtil.FCGI_BUF_SIZE);
	    pos += FCGIUtil.FCGI_BUF_SIZE;
	}
	contentLength = length % FCGIUtil.FCGI_BUF_SIZE;
	header[4] = (byte) ((contentLength >> 8) & 0xff);
	header[5] = (byte) ((contentLength) & 0xff);
	write(header);
	write(buf, pos, contentLength);
    }

    /**
     * Start the FCGI_RESPONDER conversation
     * 
     * @throws ConnectionException
     */
    public void writeBegin(boolean isLast) throws ConnectionException {
	int role = FCGIUtil.FCGI_RESPONDER;
	byte lastChild = (byte) (isLast? FCGIUtil.FCGI_END_CONN : FCGIUtil.FCGI_KEEP_CONN);
	byte[] body = new byte[] { (byte) ((role >> 8) & 0xff),
	        (byte) ((role) & 0xff), lastChild, 0, 0, 0, 0,
	        0 };

	write(FCGIUtil.FCGI_BEGIN_REQUEST, body);
    }

    private void writeLength(ByteArrayOutputStream out, int keyLen)
            throws IOException {
	if (keyLen < 0x80) {
	    out.write((byte) keyLen);
	} else {
	    byte[] b = new byte[] { (byte) (((keyLen >> 24) | 0x80) & 0xff),
	            (byte) ((keyLen >> 16) & 0xff),
	            (byte) ((keyLen >> 8) & 0xff), (byte) keyLen };
	    out.write(b);
	}
    }

    /**
     * Write FCGI Params according to FCGI spec
     * 
     * @param props
     * @throws ConnectionException
     */
    public void writeParams(Map props) throws ConnectionException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	for (Iterator ii = props.keySet().iterator(); ii.hasNext();) {
	    Object k = ii.next();
	    Object v = props.get(k);
	    String key = String.valueOf(k);
	    String val = String.valueOf(v);
	    int keyLen = key.length();
	    int valLen = val.length();
	    if (keyLen == 0 || valLen == 0)
		continue;
	    try {
		writeLength(out, keyLen);
		writeLength(out, valLen);
		out.write(key.getBytes(Util.ASCII));
		out.write(val.getBytes(Util.ASCII));
	    } catch (IOException e) {
		throw new ConnectionException(e);
	    }
	}
	write(FCGIUtil.FCGI_PARAMS, out.toByteArray());
    }

    /** {@inheritDoc} */
    public void write(byte buf[]) throws ConnectionException {
	write(buf, 0, buf.length);
    }

    /** {@inheritDoc} */
    public void write(byte buf[], int off, int buflength)
            throws ConnectionException {
	try {
	    out.write(buf, off, buflength);
	} catch (IOException ex) {
	    throw new ConnectionException(ex);
	}
    }

    /** {@inheritDoc} */
    public void write(int b) throws ConnectionException {
	throw new NotImplementedException();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws FCGIProcessException
     */
    public void close() throws ConnectionException {
	flush();
    }

    /** {@inheritDoc} */
    public void flush() throws ConnectionException {
	try {
	    out.flush();
	} catch (IOException ex) {
	    throw new ConnectionException(ex);
	}
    }

    public void setId(int id) {
	this.id = id;
   }

}