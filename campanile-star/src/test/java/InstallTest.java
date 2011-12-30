/*
 * The Campanile Project
 * Copyright (C) 2012 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class InstallTest {

    private Connection con;

    public InstallTest() {
        con = null;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        con = DriverManager.getConnection("jdbc:hsqldb:testdb", "SA", "");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void install() throws Exception {
        Install.installDB(con);

        for (int i = 0; i < 5;) {
            con.createStatement().executeUpdate("insert into stars (star) values(" + (++i) + ")");
        }

        Statement s = con.createStatement();
        ResultSet r = s.executeQuery("select star from stars order by star");

        int i = 0;
        while (r.next()) {
            Assert.assertEquals(++i, r.getInt(1));
        }

    }
}
