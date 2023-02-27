/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge.http;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER(S) OR AUTHOR(S) BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the pipe or socket channel.
 * @author jostb
 *
 */
public abstract class AbstractChannel {
    /**
     * Returns the channel's input stream.
     * @return The InputStream
     * @throws FileNotFoundException
     */
    public abstract InputStream getInputStream() throws IOException;
    /**
     * Returns the channel's output stream.
     * @return The OutputStream.
     * @throws FileNotFoundException
     */
    public abstract OutputStream getOuptutStream() throws IOException;
    /**
     * Shut down the channel, closes the in- and output stream and other resources.
     */
    public abstract void shutdown();
    
    /**
     * Returns the name of the channel, for example the socket # or the pipe name.
     * @see php.java.bridge.http.AbstractChannelName#getName()
     * @return the name of the channel.
     */
    public abstract String getName();
}
