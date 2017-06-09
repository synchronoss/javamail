/*
 * Copyright (c) 2017 Synchronoss Messaging, Inc.
 * All Rights Reserved.
 *
 * This software is the proprietary information of Synchronoss Messaging, Inc.
 * Use is subject to license terms.
 */
package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*; 

/**
 * This class represents the FIRSTLINE data item
 */

public class FIRSTLINE implements Item {
    
    static final char[] name = {'F','I','R','S','T','L','I','N','E'};
    public int msgno;

    public String firstLine;

    /**
     * Constructor
     */
    public FIRSTLINE(FetchResponse r) throws ParsingException {
        msgno = r.getNumber();

        r.skipSpaces();

        // expect data of the form ("the quoted first line \" (maybe with escaped chars in it)")

        if (r.readByte() != '(')
            throw new ParsingException("FIRSTLINE parse error");

        // readString() parses a quoted string value and returns null if it fails
        firstLine = r.readString();

        if ((firstLine == null) || (r.readByte() != ')'))
            throw new ParsingException("FIRSTLINE parse error");
    }
}
