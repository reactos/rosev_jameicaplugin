/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.rmi;

import de.jost_net.JVerein.server.DBSupportMySqlImpl;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import java.rmi.RemoteException;

public interface JameicaPluginDBService extends DBService
{
    public static final Settings settings = new Settings(JameicaPluginDBService.class);
    public static final DBSupportMySqlImpl jvereinMySqlService = new DBSupportMySqlImpl();
    public static final int ourVersion = 1;

    /**
     * Checks the version of the database and compares it with DBVersion.
     * 
     * @param conn
     *        The database connection
     * 
     * @throws RemoteException
     *         If an error occurs while checking
     * 
     * @throws ApplicationException
     *         If the version of the application and database doesn't match
     */
    public void checkVersion() throws RemoteException, ApplicationException;
}
