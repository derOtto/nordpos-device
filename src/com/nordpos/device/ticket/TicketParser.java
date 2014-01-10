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
 * @author: Andrey Svininykh <svininykh@gmail.com>
 */
public class TicketParser extends DefaultHandler {

    private final DeviceTicketFactory printer;

    private static SAXParser m_sp = null;
    private static XMLReader m_sr = null;

    private StringBuffer text;

    private int m_iTextAlign;
    private int m_iTextLength;
    private int m_iTextStyle;

    private int m_iOutputType;
    private static final int OUTPUT_NONE = 0;
    private static final int OUTPUT_TICKET = 2;

    private DevicePrinter m_oOutputPrinter;
    private InputStream InputStream;

    private static final String PRINTER_SHEMA = "/META-INF/templates/Schema.Printer.xsd";

    public TicketParser(DeviceTicketFactory printer) {
        this.printer = printer;
    }

    public void printTicket(String scriptFile, ScriptEngine script) throws TicketPrinterException, ScriptException {
        InputStream = getClass().getResourceAsStream(scriptFile);
        String sXML = new Scanner(InputStream,"UTF-8").useDelimiter("\\A").next();
        printTicket(new StringReader(script.eval(sXML).toString()));
    }

    public void printTicket(Reader in) throws TicketPrinterException {
        try {

            if (m_sp == null) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);
                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                InputStream = getClass().getResourceAsStream(PRINTER_SHEMA);
                spf.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(InputStream)}));
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
        m_oOutputPrinter = null;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        switch (m_iOutputType) {
            case OUTPUT_NONE:
                if ("ticket".equals(qName)) {
                    m_iOutputType = OUTPUT_TICKET;
                    m_oOutputPrinter = printer.getDevicePrinter();
                    m_oOutputPrinter.beginReceipt();
                }
                break;
            case OUTPUT_TICKET:
                if ("line".equals(qName)) {
                    m_oOutputPrinter.beginLine(parseInt(attributes.getValue("size"), DevicePrinter.SIZE_0));
                } else if ("text".equals(qName)) {
                    text = new StringBuffer();
                    m_iTextStyle = ("true".equals(attributes.getValue("bold")) ? DevicePrinter.STYLE_BOLD : DevicePrinter.STYLE_PLAIN)
                            | ("true".equals(attributes.getValue("underline")) ? DevicePrinter.STYLE_UNDERLINE : DevicePrinter.STYLE_PLAIN);
                    String sAlign = readString(attributes.getValue("align"), "left");
                    if ("right".equals(sAlign)) {
                        m_iTextAlign = DevicePrinter.ALIGN_RIGHT;
                    } else if ("center".equals(sAlign)) {
                        m_iTextAlign = DevicePrinter.ALIGN_CENTER;
                    } else {
                        m_iTextAlign = DevicePrinter.ALIGN_LEFT;
                    }
                    m_iTextLength = parseInt(attributes.getValue("length"), 0);
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
                if ("text".equals(qName)) {
                    if (m_iTextLength > 0) {
                        switch (m_iTextAlign) {
                            case DevicePrinter.ALIGN_RIGHT:
                                m_oOutputPrinter.printText(m_iTextStyle, StringUtils.alignRight(text.toString(), m_iTextLength));
                                break;
                            case DevicePrinter.ALIGN_CENTER:
                                m_oOutputPrinter.printText(m_iTextStyle, StringUtils.alignCenter(text.toString(), m_iTextLength));
                                break;
                            default:
                                m_oOutputPrinter.printText(m_iTextStyle, StringUtils.alignLeft(text.toString(), m_iTextLength));
                                break;
                        }
                    } else {
                        m_oOutputPrinter.printText(m_iTextStyle, text.toString());
                    }
                    text = null;
                } else if ("line".equals(qName)) {
                    m_oOutputPrinter.endLine();
                } else if ("ticket".equals(qName)) {
                    m_oOutputPrinter.endReceipt();
                    m_iOutputType = OUTPUT_NONE;
                    m_oOutputPrinter = null;
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

    private int parseInt(String sValue) {
        return parseInt(sValue, 0);
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

    private String readString(String sValue, String sDefault) {
        if (sValue == null || sValue.equals("")) {
            return sDefault;
        } else {
            return sValue;
        }
    }

    private boolean readBoolean(String sValue, boolean bDefault) {
        if (sValue == null || sValue.equals("")) {
            return bDefault;
        } else {
            return Boolean.parseBoolean(sValue);
        }
    }
}
