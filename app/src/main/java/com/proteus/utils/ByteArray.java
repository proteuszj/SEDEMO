package com.proteus.utils;

import android.support.annotation.NonNull;

public final class ByteArray {

    private byte[] buffer;

    public ByteArray(String hexString) {
        StringBuilder sb = new StringBuilder(hexString);
        int i = 0;
        while (i < sb.length()) {
            if (isHexChar(sb.charAt(i))) i++;
            else
                sb.deleteCharAt(i);
        }
        if (0 == sb.length() % 2) {
            buffer = new byte[sb.length() / 2];
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = (byte) Integer.parseInt(sb.substring(j * 2, j * 2 + 2), 16);
            }
        }
    }

    public ByteArray(byte[] byteArray) {
        buffer = byteArray.clone();
    }

    public byte[] data() {
        return buffer.clone();
    }

    public boolean isHexChar(char character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'f' || character >= 'A' && character <= 'F';
    }

    public String toString(char delimiter) {
        if (null == buffer || 0 == buffer.length)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
            sb.append(String.format("%02X", buffer[i]));
            if (i < buffer.length - 1)
                sb.append(delimiter);
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String toString() {
        if (null == buffer || 0 == buffer.length)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length; i++)
            sb.append(String.format("%02X", buffer[i]));
        return sb.toString();
    }
}
