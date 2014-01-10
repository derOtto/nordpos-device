//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.
package com.nordpos.device.receiptprinter;

import com.nordpos.device.ticket.TicketPrinterException;
import com.nordpos.device.writter.Writter;

public class DevicePrinterPlainText implements DevicePrinter {

    private final byte[] bEndOfLine;

    private final Writter out;

    public DevicePrinterPlainText(Writter CommOutputPrinter, byte[] bEndOfLine) throws TicketPrinterException {
        out = CommOutputPrinter;
        this.bEndOfLine = bEndOfLine;
    }

    @Override
    public String getPrinterName() {
        return "label.ReceiptPrinterPlainText";
    }

    @Override
    public String getPrinterDescription() {
        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public void beginReceipt() {
    }

    @Override
    public void beginLine(int iTextSize) {
    }

    @Override
    public void printText(int iStyle, String sText) {
        out.write(sText);
    }

    @Override
    public void endLine() {
        out.write(bEndOfLine);
    }

    @Override
    public void endReceipt() {
        out.write(bEndOfLine);
        out.write(bEndOfLine);
        out.write(bEndOfLine);
        out.write(bEndOfLine);
        out.write(bEndOfLine);
        out.flush();
    }

}
