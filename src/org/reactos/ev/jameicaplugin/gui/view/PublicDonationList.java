/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.view;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.action.HTMLOutput;
import org.reactos.ev.jameicaplugin.gui.action.NewAdditionalDonation;
import org.reactos.ev.jameicaplugin.gui.control.DonationControl;

public class PublicDonationList extends AbstractView
{
    DonationControl control;

    @Override
    public void bind() throws Exception
    {
        GUI.getView().setTitle(JameicaPlugin.i18n().tr("Public Donation List"));

        control = new DonationControl(this);
        control.getShowInvalidCheckbox().paint(this.getParent());
        control.getDonationList().paint(this.getParent());

        ButtonArea buttons = new ButtonArea();
        buttons.addButton(JameicaPlugin.i18n().tr("HTML Output"), new HTMLOutput(), null, false, "text-html.png");
        buttons.addButton(JameicaPlugin.i18n().tr("New additional donation"), new NewAdditionalDonation(), null, true, "document-new.png");
        buttons.paint(this.getParent());
    }

    public DonationControl getDonationControl()
    {
        return control;
    }
}
