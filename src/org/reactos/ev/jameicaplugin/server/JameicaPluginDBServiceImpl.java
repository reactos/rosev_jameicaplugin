/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.server;

import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.reactos.ev.jameicaplugin.rmi.JameicaPluginDBService;

public class JameicaPluginDBServiceImpl extends DBServiceImpl implements JameicaPluginDBService
{
    private static final long serialVersionUID = -3136184720997872605L;

    public JameicaPluginDBServiceImpl() throws RemoteException
    {
        super();

        // For finding the MySQL JDBC driver
        this.setClassloader(Application.getClassLoader());
        this.setClassFinder(Application.getClassLoader().getClassFinder());
    }

    protected String getJdbcDriver() throws RemoteException
    {
        return "com.mysql.jdbc.Driver";
    }

    protected String getJdbcPassword() throws RemoteException
    {
        return jvereinMySqlService.getJdbcPassword();
    }

    protected String getJdbcUrl() throws RemoteException
    {
        // JDBC URL includes database name, so we need our own setting here.
        return settings.getString("jdbcurl", null);
    }

    protected String getJdbcUsername() throws RemoteException
    {
        return jvereinMySqlService.getJdbcUsername();
    }

    public void checkVersion() throws RemoteException, ApplicationException
    {
        try
        {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version FROM version");

            if (rs.next())
            {
                int dbVersion = rs.getInt(1);

                if (ourVersion != dbVersion)
                    throw new ApplicationException("Version mismatch between program and database!");
            }
        }
        catch (SQLException e)
        {
            throw new RemoteException(e.getMessage());
        }
    }

    public int getTransactionIsolationLevel()
    {
        // See database updates by others without issueing a COMMIT command.
        // Needed to see entries changed in JVerein.
        return Connection.TRANSACTION_READ_COMMITTED;
    }
}
