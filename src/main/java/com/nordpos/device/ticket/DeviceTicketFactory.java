/**
 *
 * NORD POS is a fork of Openbravo POS.
 *
 * Copyright (C) 2009-2013 Nord Trading Ltd. <http://www.nordpos.com>
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
package com.nordpos.device.ticket;

import com.nordpos.device.receiptprinter.DevicePrinter;
import com.nordpos.device.receiptprinter.DevicePrinterNull;
import com.nordpos.device.display.DeviceDisplay;
import com.nordpos.device.display.DeviceDisplayNull;
import com.nordpos.device.display.DisplayInterface;
import com.nordpos.device.receiptprinter.ReceiptPrinterInterface;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Svininykh <svininykh@gmail.com>
 * @version NORD POS 3.0
 */
public class DeviceTicketFactory {

    private static final Logger logger = Logger.getLogger(DeviceTicketFactory.class.getName());

    private DevicePrinter devicePrinter;
    private DeviceDisplay deviceDisplay;

    private String sReceiptPrinterParam;
    private String sDisplayParam;

    public DeviceTicketFactory() {
        devicePrinter = new DevicePrinterNull();
        deviceDisplay = new DeviceDisplayNull();
    }

    public DevicePrinter getDevicePrinter() {
        ServiceLoader<ReceiptPrinterInterface> receiptPrinterLoader = ServiceLoader.load(ReceiptPrinterInterface.class);
        for (ReceiptPrinterInterface machineInterface : receiptPrinterLoader) {
            try {
                devicePrinter = machineInterface.getReceiptPrinter(getReceiptPrinterParameter());
            } catch (Exception e) {
                devicePrinter = new DevicePrinterNull();
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return devicePrinter;
    }

    public String getReceiptPrinterParameter() {
        return sReceiptPrinterParam;
    }

    public void setReceiptPrinterParameter(String sReceiptPrinterParam) {
        this.sReceiptPrinterParam = sReceiptPrinterParam;
    }

    public String getDisplayParameter() {
        return sDisplayParam;
    }

    public void setDisplayParameter(String sDisplayParam) {
        this.sDisplayParam = sDisplayParam;
    }

    public DeviceDisplay getDeviceDisplay() {
        ServiceLoader<DisplayInterface> displayLoader = ServiceLoader.load(DisplayInterface.class);

        for (DisplayInterface machineInterface : displayLoader) {
            try {
                deviceDisplay = machineInterface.getDisplay(sDisplayParam);
            } catch (Exception e) {
                deviceDisplay = new DeviceDisplayNull();
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return deviceDisplay;
    }
}
