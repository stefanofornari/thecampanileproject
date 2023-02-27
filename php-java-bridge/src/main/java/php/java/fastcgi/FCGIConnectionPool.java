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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FCGIConnectionPool implements CloseableConnection {

    private int connections = 0;
    private List freeList = new ArrayList();
    private List connectionList = new ArrayList();
    private FCGIFactory factory;
    private FCGIHelper helper;

    public FCGIConnectionPool(FCGIHelper helper) {
	this.helper = helper;
    }

    /* helper for openConnection() */
    private Connection createNewConnection() throws FCGIProcessException {
	Connection connection = factory.connect();
	connectionList.add(connection);
	connection.setId(connections++);
	return connection;
    }

    /**
     * Opens a connection to the back end.
     * 
     * @return The connection
     * @throws InterruptedException
     * @throws FCGIProcessException
     */
    public synchronized Connection openConnection()
            throws InterruptedException, FCGIProcessException {
	Connection connection;
	if (freeList.isEmpty()
	        && connections < helper.getPhpFcgiConnectionPoolSize()) {
	    connection = createNewConnection();
	} else {
	    while (freeList.isEmpty()) {
		if (helper.getPhpFcgiConnectionPoolTimeout() > 0) {
		    long t1 = System.currentTimeMillis();
		    wait(helper.getPhpFcgiConnectionPoolTimeout());
		    long t2 = System.currentTimeMillis();
		    long t = t2 - t1;
		    if (t >= helper.getPhpFcgiConnectionPoolTimeout())
			throw new FCGIProcessException(
			        new IOException("pool timeout "
			                + helper.getPhpFcgiConnectionPoolTimeout()
			                + " exceeded: " + t));
		} else {
		    wait();
		}
	    }
	    connection = (Connection) freeList.remove(0);
	}
	return connection;
    }

    private synchronized Connection reopen(Connection connection) throws FCGIProcessException {
	if (connection.decrementCounter()) {
	    connection.setIsClosed();
	}

	if (connection.isClosed()) {
	    int id = connection.getId();
	    connectionList.remove(connection);
	    connection.closeConnection();
	    
	    connection = factory.connect();
	    connectionList.add(connection);
	    connection.setId(id);
	}

	return connection;
    }

    public synchronized void closeConnection(Connection connection)
            throws FCGIProcessException {
	freeList.add(reopen(connection));
	notify();
    }

    /**
     * Destroy the connection pool.
     * 
     * It releases all physical connections.
     *
     */
    public synchronized void destroy() {
	for (Iterator ii = connectionList.iterator(); ii.hasNext();) {
	    Connection connection = (Connection) ii.next();
	    connection.closeConnection();
	    connection.setId(-1);
	}

	if (factory != null)
	    factory.destroy();
    }

    public static FCGIConnectionPool createConnectionPool(String[] args,
            Map env, FCGIHelper helper) throws FCGIProcessException, ConnectionException {
	FCGIConnectionPool pool = new FCGIConnectionPool(helper);
	pool.factory = FCGIFactory.createConnectionFactory(args, env, pool,
	        helper);
	pool.factory.startFCGIServer();
	return pool;
    }

}
