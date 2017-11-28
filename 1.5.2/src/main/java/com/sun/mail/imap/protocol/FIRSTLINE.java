/*
 * Copyright (c) 2017 Synchronoss Messaging, Inc.
 * All Rights Reserved.
 *
 * This software is the proprietary information of Synchronoss Messaging, Inc.
 * Use is subject to license terms.
 */
package com.sun.mail.imap.protocol;

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

        // expect data of the form with quoted-string ("the quoted first line \" (maybe with escaped chars in it)")
        // or of the form with literal ("{" number "}" CRLF *CHAR8)
        // but note that the summary may be UTF-8 (or a different encoding)

        if (r.readByte() != '(')
            throw new ParsingException("FIRSTLINE parse error");

        // parse a quoted string value or literal value and return null if it fails
        ByteArray bArray = r.parseFirstLine();
        if (bArray == null || r.readByte() != ')') {
            throw new ParsingException("FIRSTLINE parse error");
        }
        // also expose the byte array to allow clients to use different charsets (eg Big5)
        firstLineAsBytes = bArray.getNewBytes();
        // this only supports UTF-8
        firstLine = new String(firstLineAsBytes, Charset.forName("UTF-8"));
    }
}
