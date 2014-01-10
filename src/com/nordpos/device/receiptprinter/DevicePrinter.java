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

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * @author Adrián Romero
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public interface DevicePrinter {

    public static final int SIZE_0 = 0;
    public static final int SIZE_1 = 1;
    public static final int SIZE_2 = 2;
    public static final int SIZE_3 = 3;

    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_UNDERLINE = 2;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;

    public static final String POSITION_TOP = "top";
    public static final String POSITION_BOTTOM = "bottom";
    public static final String POSITION_NONE = "none";

    public String getPrinterName();
    public String getPrinterDescription();
    public void reset();

    public void beginReceipt();
    public void beginLine(int iTextSize);
    public void printText(int iStyle, String sText);
    public void endLine();
    public void endReceipt();

}
