/*
 * PROJECT:    ReactOS Deutschland e.V. Helper Plugin
 * LICENSE:    GNU GPL v2 or any later version as published by the Free Software Foundation
 * COPYRIGHT:  Copyright 2010-2016 ReactOS Deutschland e.V. <deutschland@reactos.org>
 * AUTHORS:    Colin Finck <colin@reactos.org>
 */

package org.reactos.ev.jameicaplugin.gui.view;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import org.reactos.ev.jameicaplugin.JameicaPlugin;
import org.reactos.ev.jameicaplugin.gui.action.NewAdditionalDonation;
import org.reactos.ev.jameicaplugin.gui.control.AdditionalDonationControl;

public class AdditionalDonationDetail extends AbstractView
{
    @Override
    public void bind() throws Exception
    {
        GUI.getView().setTitle(JameicaPlugin.i18n().tr("Additional donation"));

        final AdditionalDonationControl control = new AdditionalDonationControl(this);
        ScrolledContainer scrolled = new ScrolledContainer(getParent());

        LabelGroup group = new LabelGroup(scrolled.getComposite(),
                JameicaPlugin.i18n().tr("Donation"));
        group.addLabelPair("ID", control.getID());
        group.addLabelPair(JameicaPlugin.i18n().tr("Date"), control.getDate());
        group.addLabelPair(JameicaPlugin.i18n().tr("Name"), control.getName());
        group.addLabelPair(JameicaPlugin.i18n().tr("Anonymous"), control.getAnonymous());
        group.addLabelPair(JameicaPlugin.i18n().tr("Amount"), control.getAmount());
        group.addLabelPair(JameicaPlugin.i18n().tr("Currency Code"), control.getCurrency());
        group.addLabelPair(JameicaPlugin.i18n().tr("Comment"), control.getComment());

        de.willuhn.jameica.gui.parts.ButtonArea buttons = new ButtonArea();
        buttons.addButton(JameicaPlugin.i18n().tr("New additional donation"), new NewAdditionalDonation(), null, true, "document-new.png");
        buttons.addButton(JameicaPlugin.i18n().tr("Save"), new Action()
        {
            public void handleAction(Object context)
            {
                control.handleStore();
            }
        }, null, true, "document-save.png");
        buttons.paint(getParent());
    }
}
