package com.daniel.myrecipes.utils;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Utils {

    public static <T> String stringJoin(String delim, T[] col) {
        return stringJoin(delim, col, Object::toString);
    }

    public static <T> String stringJoin(String delim, Collection<T> col) {
        return stringJoin(delim, col, Object::toString);
    }

    public static <T> String stringJoin(String delim, T[] col, Function<T, String> toStringFunc) {
        if (col == null || col.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < col.length; i++) {
            T element = col[i];
            if (i != 0)
                sb.append(delim);

            sb.append(toStringFunc.apply(element));
        }

        return sb.toString();
    }

    public static <T> String stringJoin(String delim, Collection<T> col, Function<T, String> toStringFunc) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (T element: col) {
            if (i != 0)
                sb.append(delim);
            sb.append(toStringFunc.apply(element));
            i++;
        }
        return sb.toString();
    }

    public static <T> boolean existsInArray(T value, T[] arr) {
        for (T element: arr) {
            if (element.equals(value))
                return true;
        }
        return false;
    }

    public static String formatSQLSafeString(String s) {
        StringBuilder sb = new StringBuilder();

        sb.append('\'');

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
                case '\0': sb.append("\\0"); break;
                case '\'': sb.append("\\'"); break;
                case '\"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\r': sb.append("\\r"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\\': sb.append("\\\\"); break;
                case '%': sb.append("\\%"); break;
                case '_': sb.append("\\_"); break;
                default: sb.append(c); break;
            }
        }

        sb.append('\'');

        return sb.toString();
    }

    public static String generateRandomString(int len) {
        Random random = new Random(System.nanoTime());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            int selected;
            do {
                selected = random.nextInt(42) + 48;
            } while (selected >= 58 && selected <= 64);
            sb.append((char) selected);
        }

        return sb.toString();
    }

    public static void writeToStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) != -1)
            output.write(buffer, 0, len);
    }

    public static byte[] serialize(Serializable object) throws IOException {
        if (object == null)
            return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(baos)) {
            output.writeObject(object);
            output.flush();
            return baos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0)
            return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream input = new ObjectInputStream(bais)) {
            return (T) input.readObject();
        }
    }

}
