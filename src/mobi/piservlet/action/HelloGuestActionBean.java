/**
 * Copyright (c) 2014 Nord Trading Ltd.
 */
package mobi.piservlet.action;

import com.nordpos.device.ticket.DeviceTicketFactory;
import com.nordpos.device.ticket.TicketParser;
import com.nordpos.device.ticket.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class HelloGuestActionBean extends BaseActionBean {

    private static final String HELLO = "/WEB-INF/jsp/hello_guest.jsp";
    private static final String TICKET_XML = "/META-INF/templates/Printer.Ticket.xml";

    private String guestName;

    public Resolution helloGuest() {

        DeviceTicketFactory deviceTicketFactory = new DeviceTicketFactory(getServletContext().getInitParameter("machine.printer"));
        TicketParser ticketParser = new TicketParser(deviceTicketFactory);
        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            script.put("this", this);
            ticketParser.printTicket(TICKET_XML, script);
        } catch (TicketPrinterException | ScriptException ex) {
            Logger.getLogger(HelloGuestActionBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ForwardResolution(HELLO);
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

}
