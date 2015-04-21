//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2009 Openbravo, S.L.
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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author adrianromero
 */
public class PreviewBasicTicket {

    private static final int H_GAP = 8;
    private static final int V_GAP = 8;
    private static final int COLUMNS = 42;
    private static final int LINEWIDTH = COLUMNS * 7;

    private final int imageable_width;
    private final int imageable_height;
    private final int imageable_x;
    private final int imageable_y;

    private final BasicTicket ticket;

    public PreviewBasicTicket(BasicTicket ticket, int imageable_x, int imageable_y, int imageable_width, int imageable_height) {
        this.ticket = ticket;
        this.imageable_x = imageable_x;
        this.imageable_y = imageable_y;
        this.imageable_width = imageable_width;
        this.imageable_height = imageable_height;
    }

    public BufferedImage paint() throws IOException {

        BufferedImage bi = new BufferedImage(imageable_width, ticket.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bi.createGraphics();

        g2d.setPaint(new GradientPaint(imageable_x, imageable_y, Color.WHITE, imageable_width, imageable_height, new Color(0xf0f0f0), true));
        g2d.fillRect(imageable_x, imageable_y, imageable_width, imageable_height);

        g2d.setColor(Color.BLACK);
        ticket.draw(g2d, imageable_x + H_GAP, imageable_y + V_GAP, LINEWIDTH);

        return bi;
    }

}
