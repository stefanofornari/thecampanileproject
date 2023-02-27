/*-*- mode: Java; tab-width:8 -*-*/

package php.java.fastcgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import php.java.bridge.Util;

/*
 * Copyright (C) 2017 Jost BÃ¶kemeier
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


public abstract class FCGIHeaderParser {
    /**
     * The default CGI header parser. The default implementation discards everything.
     */
    public static final FCGIHeaderParser DEFAULT_HEADER_PARSER = new SimpleHeaderParser();
    /**
     * Parse a header
     * @param header The header string to parse
     */
    public abstract void parseHeader(String header);
    /**
     * Add a header
     * @param key the key
     * @param val the value
     */
    public abstract void addHeader (String key, String val);
    /**
     * Discards all header fields from a HTTP connection and write the body to the OutputStream
     * @param buf A buffer, for example new byte[BUF_SIZE]
     * @param natIn The InputStream
     * @param out The OutputStream
     * @param parser The header parser
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void parseBody(byte[] buf, InputStream natIn, OutputStream out, OutputStream err) throws UnsupportedEncodingException, IOException {
	int i = 0, n, s = 0;
	boolean eoh = false;
	boolean rn = false;
	String remain = null;
	String line;
	
	// the header and content
	while((n = natIn.read(buf)) !=-1 ) {
	    if (n<-1) {
		n = n*-1-1;
		err.write(buf, 0, n);
		continue;
	    }
	    int N = i + n;
	    // header
	    while(!eoh && i<N) {
		switch(buf[i++]) {

		case '\n':
		    if(rn) {
			eoh=true;
		    } else {
			if (remain != null) {
			    line = remain + new String(buf, s, i-s, Util.ASCII);
			    line = line.substring(0, line.length()-2);
			    remain = null;
			} else {
			    line = new String(buf, s, i-s-2, Util.ASCII);
			}
			this.parseHeader(line); //addHeader
			s=i;
		    }
		    rn=true;
		    break;

		case '\r': break;

		default: rn=false;	

		}
	    }
	    // body
	    if(eoh) {
		if(i<N) {
		    out.write(buf, i, N-i);
		}
	    }  else { 
		if (remain != null) {
		    remain += new String(buf, s, i-s, Util.ASCII);
		} else {
		    remain = new String(buf, s, i-s, Util.ASCII);
		}
	    }
	    s = i = 0;
	}
    }
}