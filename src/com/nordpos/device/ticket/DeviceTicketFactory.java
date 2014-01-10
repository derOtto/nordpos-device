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
package com.nordpos.device.ticket;

import com.nordpos.device.receiptprinter.DevicePrinter;
import com.nordpos.device.receiptprinter.DevicePrinterNull;
import com.nordpos.device.ReceiptPrinterInterface;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceTicketFactory {

    private static final Logger logger = Logger.getLogger(DeviceTicketFactory.class.getName());

    private DevicePrinter devicePrinter;
    
    public DeviceTicketFactory(String sProperty) {

        devicePrinter = new DevicePrinterNull();

        ServiceLoader<ReceiptPrinterInterface> receiptPrinterLoader = ServiceLoader.load(ReceiptPrinterInterface.class);

        for (ReceiptPrinterInterface machineInterface : receiptPrinterLoader) {
            try {
                devicePrinter = machineInterface.getReceiptPrinter(sProperty);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

    }

    public DevicePrinter getDevicePrinter() {
        return devicePrinter;
    }
}
