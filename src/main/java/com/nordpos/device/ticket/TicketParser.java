//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008-2009 Openbravo, S.L.
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

import com.nordpos.device.display.DeviceDisplay;
import com.nordpos.device.display.DeviceDisplayBase;
import com.nordpos.device.receiptprinter.DevicePrinter;
import com.nordpos.device.util.StringUtils;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Andrey Svininykh <svininykh@gmail.com>
 * @version NORD POS 3.0
 */
public class TicketParser extends DefaultHandler {

    private final DeviceTicketFactory printer;
    private static SAXParser m_sp = null;
    private static XMLReader m_sr = null;
    private StringBuffer text;
    private int m_iTextAlign;
    private int m_iTextLength;
    private Integer integerCharacterSize;
    private String sUnderline;
    private boolean bBold;
    private StringBuffer m_sVisorLine;
    private int m_iVisorAnimation;
    private String m_sVisorLine1;
    private String m_sVisorLine2;
    private int m_iOutputType;
    private static final int OUTPUT_NONE = 0;
    private static final int OUTPUT_DISPLAY = 1;
    private static final int OUTPUT_TICKET = 2;
    private DevicePrinter outputPrinter;
    private DeviceDisplay outputDisplay;
    private InputStream shemaFile;

    public TicketParser(InputStream shemaFile, DeviceTicketFactory printer) {
        this.shemaFile = shemaFile;
        this.printer = printer;
    }

    public void printTicket(InputStream scriptFile, ScriptEngine script) throws TicketPrinterException, ScriptException {
        String sXML = new Scanner(scriptFile, "UTF-8").useDelimiter("\\A").next();
        printTicket(new StringReader(script.eval(sXML).toString()));
    }

    public void printTicket(Reader in) throws TicketPrinterException {
        try {
            if (m_sp == null) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);
                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                spf.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(shemaFile)}));
                m_sp = spf.newSAXParser();
                m_sr = m_sp.getXMLReader();
            }
            m_sr.setContentHandler(this);
            m_sr.parse(new InputSource(in));
        } catch (ParserConfigurationException ePC) {
            throw new TicketPrinterException("exception.parserconfig", ePC);
        } catch (SAXException eSAX) {
            throw new TicketPrinterException("exception.xmlfile", eSAX);
        } catch (IOException eIO) {
            throw new TicketPrinterException("exception.iofile", eIO);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        text = null;
        m_iOutputType = OUTPUT_NONE;
        outputPrinter = null;
        outputDisplay = null;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (m_iOutputType) {
            case OUTPUT_NONE:
                switch (qName) {
                    case "ticket":
                        m_iOutputType = OUTPUT_TICKET;
                        outputPrinter = printer.getDevicePrinter();
                        outputPrinter.beginReceipt();
                        break;
                    case "display":
                        m_iOutputType = OUTPUT_DISPLAY;
                        outputDisplay = printer.getDeviceDisplay();
                        String animation = readString(attributes.getValue("animation"), "none");
                        switch (animation) {
                            case "scroll":
                                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_SCROLL;
                                break;
                            case "flyer":
                                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_FLYER;
                                break;
                            case "blink":
                                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_BLINK;
                                break;
                            case "curtain":
                                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_CURTAIN;
                                break;
                            default:
                                m_iVisorAnimation = DeviceDisplayBase.ANIMATION_NULL;
                                break;
                        }
                        m_sVisorLine1 = null;
                        m_sVisorLine2 = null;
                }
                break;
            case OUTPUT_TICKET:
                switch (qName) {
                    case "line":
                        outputPrinter.beginLine(parseInteger(attributes.getValue("size")));
                        break;
                    case "text":
                        text = new StringBuffer();
                        integerCharacterSize = parseInteger(attributes.getValue("size"));
                        sUnderline = readString(attributes.getValue("underline"));
                        bBold = attributes.getValue("bold").equals("true");
                        String sAlign = readString(attributes.getValue("align"));
                        switch (sAlign) {
                            case "right":
                                m_iTextAlign = DevicePrinter.ALIGN_RIGHT;
                                break;
                            case "center":
                                m_iTextAlign = DevicePrinter.ALIGN_CENTER;
                                break;
                            default:
                                m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                                break;
                        }
                        m_iTextLength = parseInt(attributes.getValue("length"), 0);
                }
                break;
            case OUTPUT_DISPLAY:
                switch (qName) {
                    case "line":
                        m_sVisorLine = new StringBuffer();
                        break;
                    case "line1":
                        m_sVisorLine = new StringBuffer();
                        break;
                    case "line2":
                        m_sVisorLine = new StringBuffer();
                        break;
                    case "text":
                        text = new StringBuffer();
                        String sAlign = readString(attributes.getValue("align"), "center");
                        switch (sAlign) {
                            case "right":
                                m_iTextAlign = DevicePrinter.ALIGN_RIGHT;
                                break;
                            case "center":
                                m_iTextAlign = DevicePrinter.ALIGN_CENTER;
                                break;
                            default:
                                m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                                break;
                        }
                        m_iTextLength = parseInt(attributes.getValue("length"), 0);
                        break;
                }
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (m_iOutputType) {
            case OUTPUT_NONE:
                break;
            case OUTPUT_TICKET:
                switch (qName) {
                    case "text":
                        if (m_iTextLength > 0) {
                            switch (m_iTextAlign) {
                                case DevicePrinter.ALIGN_RIGHT:
                                    outputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignRight(text.toString(), m_iTextLength));
                                    break;
                                case DevicePrinter.ALIGN_CENTER:
                                    outputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignCenter(text.toString(), m_iTextLength));
                                    break;
                                default:
                                    outputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignLeft(text.toString(), m_iTextLength));
                                    break;
                            }
                        } else {
                            outputPrinter.printText(integerCharacterSize, sUnderline, bBold, text.toString());
                        }
                        text = null;
                        break;
                    case "line":
                        outputPrinter.endLine();
                        break;
                    case "ticket":
                        outputPrinter.endReceipt();
                        m_iOutputType = OUTPUT_NONE;
                        outputPrinter = null;
                        break;
                }
                break;
            case OUTPUT_DISPLAY:
                switch (qName) {
                    case "line":
                        if (m_sVisorLine1 == null) {
                            m_sVisorLine1 = m_sVisorLine.toString();
                        } else {
                            m_sVisorLine2 = m_sVisorLine.toString();
                        }
                        m_sVisorLine = null;
                        break;
                    case "line1":
                        m_sVisorLine1 = m_sVisorLine.toString();
                        m_sVisorLine = null;
                        break;
                    case "line2":
                        m_sVisorLine2 = m_sVisorLine.toString();
                        m_sVisorLine = null;
                        break;
                    case "text":
                        if (m_iTextLength > 0) {
                            switch (m_iTextAlign) {
                                case DevicePrinter.ALIGN_RIGHT:
                                    m_sVisorLine.append(StringUtils.alignRight(text.toString(), m_iTextLength));
                                    break;
                                case DevicePrinter.ALIGN_CENTER:
                                    m_sVisorLine.append(StringUtils.alignCenter(text.toString(), m_iTextLength));
                                    break;
                                default: // DevicePrinter.ALIGN_LEFT
                                    m_sVisorLine.append(StringUtils.alignLeft(text.toString(), m_iTextLength));
                                    break;
                            }
                        } else {
                            m_sVisorLine.append(text);
                        }
                        text = null;
                        break;
                    case "display":
                        outputDisplay.writeVisor(m_iVisorAnimation, m_sVisorLine1, m_sVisorLine2);
                        m_iVisorAnimation = DeviceDisplayBase.ANIMATION_NULL;
                        m_sVisorLine1 = null;
                        m_sVisorLine2 = null;
                        m_iOutputType = OUTPUT_NONE;
                        outputDisplay = null;
                        break;
                }
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (text != null) {
            text.append(ch, start, length);
        }
    }

    private int parseInt(String sValue, int iDefault) {
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException eNF) {
            return iDefault;
        }
    }

    private Integer parseInteger(String sValue) {
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException eNF) {
            return null;
        }
    }

    private double parseDouble(String sValue, double ddefault) {
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException eNF) {
            return ddefault;
        }
    }

    private double parseDouble(String sValue) {
        return parseDouble(sValue, 0.0);
    }

    private String readString(String sValue) {
        if (sValue == null || sValue.equals("")) {
            return null;
        } else {
            return sValue;
        }
    }

    private String readString(String sValue, String sDefault) {
        if (sValue == null || sValue.equals("")) {
            return sDefault;
        } else {
            return sValue;
        }
    }
}
