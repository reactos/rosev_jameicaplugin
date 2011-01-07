/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Locale;
import org.reactos.ev.jameicaplugin.rmi.JameicaPluginDBService;
import org.reactos.ev.jameicaplugin.server.JameicaPluginDBServiceImpl;

public class JameicaPlugin extends AbstractPlugin
{
    public static final DecimalFormat currencyFormat = (DecimalFormat) DecimalFormat.getInstance(Application.getConfig().getLocale());
    public static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, Application.getConfig().getLocale());
    private static DBService db;
    private static I18N i18n;

    static
    {
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);
    }

    public JameicaPlugin()
    {
        super();
        i18n = new I18N("lang/rosev_jameicaplugin_messages", Locale.getDefault(),
                JameicaPlugin.class.getClassLoader());
    }

    public void init() throws ApplicationException
    {
        // Only check the version on the server if we're in client/server mode
        if (Application.inClientMode())
            return;

        JameicaPluginDBService service = null;

        try
        {
            service = new JameicaPluginDBServiceImpl();
            service.start();
            service.checkVersion();
        }
        catch (ApplicationException e)
        {
            throw e;
        }
        catch (RemoteException e)
        {
            throw new ApplicationException("Error initializing the database", e);
        }
        finally
        {
            if (service != null)
            {
                try
                {
                    service.stop(true);
                }
                catch (RemoteException e)
                {
                    throw new ApplicationException("Error closing the database", e);
                }
            }
        }

    }

    public static DBService getDBService() throws RemoteException
    {
        if (db != null)
            return db;

        try
        {
            db = (DBService) Application.getServiceFactory().lookup(JameicaPlugin.class, "database");
            return db;
        }
        catch (Exception e)
        {
            throw new RemoteException("Error while getting the database service", e);
        }
    }

    public static I18N i18n()
    {
        return i18n;
    }
}
