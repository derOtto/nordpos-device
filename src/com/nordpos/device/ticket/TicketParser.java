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
                    m_oOutputPrinter.beginLine(parseInteger(attributes.getValue("size")));
                } else if ("text".equals(qName)) {
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
                            m_oOutputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignRight(text.toString(), m_iTextLength));
                            break;
                        case DevicePrinter.ALIGN_CENTER:
                            m_oOutputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignCenter(text.toString(), m_iTextLength));
                            break;
                        default:
                            m_oOutputPrinter.printText(integerCharacterSize, sUnderline, bBold, StringUtils.alignLeft(text.toString(), m_iTextLength));
                            break;
                    }
                } else {
                    m_oOutputPrinter.printText(integerCharacterSize, sUnderline, bBold, text.toString());
                }
                text = null;
                break;
            case "line":
                m_oOutputPrinter.endLine();
                break;
            case "ticket":
                m_oOutputPrinter.endReceipt();
                m_iOutputType = OUTPUT_NONE;
                m_oOutputPrinter = null;
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
}