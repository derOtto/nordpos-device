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

package com.nordpos.device.display;

public class DeviceDisplayNull implements DeviceDisplay {

    private String m_sName;
    private String m_sDescription;

    /** Creates a new instance of DeviceDisplayNull */
    public DeviceDisplayNull() {
        this(null);
    }

    /** Creates a new instance of DeviceDisplayNull */
    public DeviceDisplayNull(String desc) {
        m_sName = "Display.Null";
        m_sDescription = desc;
    }

    public String getDisplayName() {
        return m_sName;
    }
    public String getDisplayDescription() {
        return m_sDescription;
    }
    public javax.swing.JComponent getDisplayComponent() {
        return null;
    }

    public void clearVisor() {
    }
    public void writeVisor(String sLine1, String sLine2) {
    }
    public void writeVisor(int animation, String sLine1, String sLine2) {
    }
}
