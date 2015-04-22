/**
 *
 * NORD POS is a fork of Openbravo POS.
 *
 * Copyright (C) 2009-2015 Nord Trading Ltd. <http://www.nordpos.com>
 *
 * This file is part of NORD POS.
 *
 * NORD POS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * NORD POS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NORD POS. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nordpos.device.receiptprinter;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 * @version NORD POS 3.0
 */
public class DevicePrinterImage implements DevicePrinter {

    private BasicTicket m_ticketcurrent;
    private PreviewBasicTicket previewBasicTicket;
    private int lineCount = 0;

    @Override
    public String getPrinterName() {
        return null;
    }

    @Override
    public String getPrinterDescription() {
        return null;
    }

    @Override
    public BufferedImage getPrinterPreview() {
        try {
            return previewBasicTicket.paint();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void reset() {
        m_ticketcurrent = null;
    }

    @Override
    public void beginReceipt() {
        m_ticketcurrent = new BasicTicketForScreen();
    }

    @Override
    public void beginLine(Integer iTextSize) {
        lineCount = lineCount + 1;
        m_ticketcurrent.beginLine(iTextSize);
    }

    @Override
    public void printText(Integer iCharacterSize, String sUnderlineType, Boolean bBold, String sText) {
        m_ticketcurrent.printText(iCharacterSize == null ? 0 : iCharacterSize, sText);
    }

    @Override
    public void endLine() {
        m_ticketcurrent.endLine();
    }

    @Override
    public void endReceipt() {
        previewBasicTicket = new PreviewBasicTicket(m_ticketcurrent, 1, 1, 320, 32 * lineCount);
        m_ticketcurrent = null;
    }

    @Override
    public void printBarCode(String type, String position, String code) {
        m_ticketcurrent.printBarCode(type, position, code);
    }

    @Override
    public void printImage(BufferedImage image) {
        m_ticketcurrent.printImage(image);
    }

    @Override
    public void cutPaper(boolean complete) {
    }

    @Override
    public void openDrawer() {
    }
}
