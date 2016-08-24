/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.dialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.widgets.Composite;
import org.reactos.ev.jameicaplugin.JameicaPlugin;

public class PayPalAPICredentials extends AbstractDialog<Object>
{
    public static final Settings settings = new Settings(PayPalAPICredentials.class);
    private TextInput username;
    private TextInput password;
    private TextInput signature;

    public PayPalAPICredentials(int position)
    {
        super(position);
        setTitle(JameicaPlugin.i18n().tr("PayPal API Credentials"));
    }

    @Override
    protected void paint(Composite parent) throws Exception
    {
        username = new TextInput(settings.getString("username", null));
        username.setName(JameicaPlugin.i18n().tr("Username"));
        password = new TextInput(settings.getString("password", null));
        password.setName(JameicaPlugin.i18n().tr("Password"));
        signature = new TextInput(settings.getString("signature", null));
        signature.setName(JameicaPlugin.i18n().tr("Signature"));

        SimpleContainer container = new SimpleContainer(parent);
        container.addInput(username);
        container.addInput(password);
        container.addInput(signature);

        final Button ok = new Button("OK", new Action()
        {
            public void handleAction(Object context) throws ApplicationException
            {
                settings.setAttribute("username", (String) username.getValue());
                settings.setAttribute("password", (String) password.getValue());
                settings.setAttribute("signature", (String) signature.getValue());
                close();
            }
        }, null, true, "ok.png");

        ButtonArea buttons = new ButtonArea();
        buttons.addButton(ok);
        buttons.addButton(new Cancel());

        container.addButtonArea(buttons);
    }

    @Override
    protected Object getData() throws Exception
    {
        return null;
    }
}
