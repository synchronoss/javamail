package com.sun.mail.imap.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.sun.mail.iap.ParsingException;

import org.junit.Test;

public class FetchResponseTest {

    @Test(expected = ParsingException.class)
    public void emptyFirstlineResponse() throws Exception {
        // When
        final FetchResponse r = new FetchResponse(new IMAPResponse("* 1 FETCH (FIRSTLINE ())"));

        // Then
        assertEquals(1, r.getItemCount());
        assertTrue(r.getItem(0) instanceof FIRSTLINE);

        final FIRSTLINE firstline = (FIRSTLINE) r.getItem(0);

        assertTrue(firstline.firstLine.isEmpty());
        assertEquals(0, firstline.firstLineAsBytes.length);
    }

    @Test
    public void blankFirstlineResponse() throws Exception {
        // When
        final FetchResponse r = new FetchResponse(new IMAPResponse("* 1 FETCH (FIRSTLINE (\"\"))"));

        // Then
        assertEquals(1, r.getItemCount());
        assertTrue(r.getItem(0) instanceof FIRSTLINE);

        final FIRSTLINE firstline = (FIRSTLINE) r.getItem(0);

        assertTrue(firstline.firstLine.isEmpty());
        assertEquals(0, firstline.firstLineAsBytes.length);
    }

    @Test
    public void textOnlyFirstlineResponse() throws Exception {
        // Given
        final String str = "abcdef";
        final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        // When
        final FetchResponse r = new FetchResponse(
                new IMAPResponse(String.format("* 1 FETCH (FIRSTLINE (\"%s\"))", str)));

        // Then
        assertEquals(1, r.getItemCount());
        assertTrue(r.getItem(0) instanceof FIRSTLINE);

        final FIRSTLINE firstline = (FIRSTLINE) r.getItem(0);

        assertFalse(firstline.firstLine.isEmpty());
        assertEquals(str, firstline.firstLine);
        assertEquals(bytes.length, firstline.firstLineAsBytes.length);
        assertArrayEquals(bytes, bytes);
    }

    @Test
    public void textAndSizeFirstlineResponse() throws Exception {
        // Given
        final String str = "abcdef";
        final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        // When
        final FetchResponse r = new FetchResponse(
                new IMAPResponse(String.format("* 1 FETCH (FIRSTLINE (\"{%d}\r\n%s\"))", str.length(), str)));

        // Then
        assertEquals(1, r.getItemCount());
        assertTrue(r.getItem(0) instanceof FIRSTLINE);

        final FIRSTLINE firstline = (FIRSTLINE) r.getItem(0);

        assertFalse(firstline.firstLine.isEmpty());
        assertEquals(str, firstline.firstLine);
        assertEquals(bytes.length, firstline.firstLineAsBytes.length);
        assertArrayEquals(bytes, bytes);
    }

    @Test(expected = ParsingException.class)
    public void invalidFirstlineResponse() throws Exception {
        // Given
        final byte[] bytes = new byte[10];
        new Random().nextBytes(bytes);
        final String str = new String(bytes, StandardCharsets.UTF_8);

        // When
        final FetchResponse r = new FetchResponse(new IMAPResponse(String.format("* 1 FETCH (FIRSTLINE (\"{%d}\r\n%s\"))", str.length() + 2, str)));

        // Then
        assertEquals(1, r.getItemCount());
        assertTrue(r.getItem(0) instanceof FIRSTLINE);

        final FIRSTLINE firstline = (FIRSTLINE)r.getItem(0);

        assertFalse(firstline.firstLine.isEmpty());
        assertEquals(str, firstline.firstLine);
        assertEquals(bytes.length, firstline.firstLineAsBytes.length);
        assertArrayEquals(bytes, bytes);
    }
}