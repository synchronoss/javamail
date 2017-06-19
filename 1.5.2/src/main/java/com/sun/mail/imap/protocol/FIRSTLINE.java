/*
 * Copyright (c) 2017 Synchronoss Messaging, Inc.
 * All Rights Reserved.
 *
 * This software is the proprietary information of Synchronoss Messaging, Inc.
 * Use is subject to license terms.
 */
package com.sun.mail.imap.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.sun.mail.iap.*; 

/**
 * This class represents the FIRSTLINE data item
 */

public class FIRSTLINE implements Item {
    
    static final char[] name = {'F','I','R','S','T','L','I','N','E'};
    public int msgno;

    public String firstLine;
    public byte[] firstLineAsBytes;

    /**
     * Constructor
     */
    public FIRSTLINE(FetchResponse r) throws ParsingException {
        msgno = r.getNumber();

        r.skipSpaces();

        // expect data of the form ("the quoted first line \" (maybe with escaped chars in it)")
        // but note that the summary may be UTF-8 (or a different encoding)

        if (r.readByte() != '(')
            throw new ParsingException("FIRSTLINE parse error");

        // parse a quoted string value and return null if it fails
        // readString is ASCII based so use readBytes
        ByteArrayInputStream bais = r.readBytes();

        if ((bais == null) || (r.readByte() != ')'))
            throw new ParsingException("FIRSTLINE parse error");

        byte arr[] = new byte[bais.available()];
        try {
            bais.read(arr);
        }
        catch (IOException e) {
            throw new ParsingException("FIRSTLINE parse error");
        }

        // this only supports UTF-8
        firstLine = new String(arr, Charset.forName("UTF-8"));

        // also expose the byte array to allow clients to use different charsets (eg Big5)
        firstLineAsBytes = arr;
    }
}
