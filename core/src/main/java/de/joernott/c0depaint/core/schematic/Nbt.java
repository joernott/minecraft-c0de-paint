package de.joernott.c0depaint.core.schematic;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Named Binary Tag, the format of the files of Minecraft, without Minecraft (task 9.4). Tags are plain Java values:
 * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@code byte[]},
 * {@link String}, {@link List} (a list tag), {@link Map} with insertion order (a compound), {@code int[]} and
 * {@code long[]}.
 *
 * <p>Reading is limited, so that a hostile file can't exhaust the server: the depth of nesting, the number of
 * elements of lists and arrays and the size of the uncompressed data.
 */
public final class Nbt {
    private static final int END = 0;
    private static final int BYTE = 1;
    private static final int SHORT = 2;
    private static final int INT = 3;
    private static final int LONG = 4;
    private static final int FLOAT = 5;
    private static final int DOUBLE = 6;
    private static final int BYTE_ARRAY = 7;
    private static final int STRING = 8;
    private static final int LIST = 9;
    private static final int COMPOUND = 10;
    private static final int INT_ARRAY = 11;
    private static final int LONG_ARRAY = 12;

    /** The deepest nesting that Minecraft accepts too. */
    private static final int MAX_DEPTH = 512;

    private static final Pattern PLAIN_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    private Nbt() {}

    /** The problem of a file that isn't valid NBT, or is too large. */
    public static final class NbtException extends RuntimeException {
        public NbtException(String message) {
            super(message);
        }
    }

    /**
     * Reads the root compound of gzipped (or uncompressed) NBT.
     *
     * @param data the bytes of the file
     * @param maxBytes the most bytes the uncompressed data may have
     * @throws NbtException if the data isn't NBT with a compound as root or is larger than allowed
     */
    public static Map<String, Object> read(byte[] data, long maxBytes) {
        try (InputStream raw = open(data); DataInputStream in = new DataInputStream(new Limited(raw, maxBytes))) {
            int type = in.readUnsignedByte();
            if (type != COMPOUND) {
                throw new NbtException("The root of the file is not a compound");
            }
            in.readUTF();
            return compound(in, 0, maxBytes);
        } catch (EOFException e) {
            throw new NbtException("The file ends too early");
        } catch (IOException e) {
            throw new NbtException(e.getMessage() == null ? "The file can't be read" : e.getMessage());
        }
    }

    private static InputStream open(byte[] data) throws IOException {
        boolean gzip = data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B;
        InputStream in = new ByteArrayInputStream(data);
        return new BufferedInputStream(gzip ? new GZIPInputStream(in) : in);
    }

    private static Map<String, Object> compound(DataInputStream in, int depth, long maxBytes) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new NbtException("The data is nested deeper than " + MAX_DEPTH);
        }
        Map<String, Object> compound = new LinkedHashMap<>();
        while (true) {
            int type = in.readUnsignedByte();
            if (type == END) {
                return compound;
            }
            String name = in.readUTF();
            compound.put(name, payload(in, type, depth + 1, maxBytes));
        }
    }

    private static Object payload(DataInputStream in, int type, int depth, long maxBytes) throws IOException {
        return switch (type) {
            case BYTE -> in.readByte();
            case SHORT -> in.readShort();
            case INT -> in.readInt();
            case LONG -> in.readLong();
            case FLOAT -> in.readFloat();
            case DOUBLE -> in.readDouble();
            case BYTE_ARRAY -> {
                byte[] array = new byte[length(in, 1, maxBytes)];
                in.readFully(array);
                yield array;
            }
            case STRING -> in.readUTF();
            case LIST -> {
                int elementType = in.readUnsignedByte();
                int length = length(in, 1, maxBytes);
                if (depth > MAX_DEPTH) {
                    throw new NbtException("The data is nested deeper than " + MAX_DEPTH);
                }
                List<Object> list = new ArrayList<>(Math.min(length, 1024));
                for (int i = 0; i < length; i++) {
                    if (elementType == END) {
                        throw new NbtException("A list of END tags has elements");
                    }
                    list.add(payload(in, elementType, depth + 1, maxBytes));
                }
                yield list;
            }
            case COMPOUND -> compound(in, depth, maxBytes);
            case INT_ARRAY -> {
                int[] array = new int[length(in, 4, maxBytes)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = in.readInt();
                }
                yield array;
            }
            case LONG_ARRAY -> {
                long[] array = new long[length(in, 8, maxBytes)];
                for (int i = 0; i < array.length; i++) {
                    array[i] = in.readLong();
                }
                yield array;
            }
            default -> throw new NbtException("Unknown tag type " + type);
        };
    }

    /** The length of a list or array, which can't be larger than the data allows. */
    private static int length(DataInputStream in, int bytesPerElement, long maxBytes) throws IOException {
        int length = in.readInt();
        if (length < 0 || (long) length * bytesPerElement > maxBytes) {
            throw new NbtException("A list or array has " + length + " elements, more than the file may have");
        }
        return length;
    }

    /** Writes a root compound as gzipped NBT, e.g. for the tests and the runner library. */
    public static byte[] write(Map<String, Object> root) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bytes))) {
            out.writeByte(COMPOUND);
            out.writeUTF("");
            writeCompound(out, root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static void writeCompound(DataOutputStream out, Map<?, ?> compound) throws IOException {
        for (Map.Entry<?, ?> entry : compound.entrySet()) {
            out.writeByte(type(entry.getValue()));
            out.writeUTF((String) entry.getKey());
            writePayload(out, entry.getValue());
        }
        out.writeByte(END);
    }

    private static void writePayload(DataOutputStream out, Object value) throws IOException {
        switch (value) {
            case Byte b -> out.writeByte(b);
            case Short s -> out.writeShort(s);
            case Integer i -> out.writeInt(i);
            case Long l -> out.writeLong(l);
            case Float f -> out.writeFloat(f);
            case Double d -> out.writeDouble(d);
            case byte[] array -> {
                out.writeInt(array.length);
                out.write(array);
            }
            case String s -> out.writeUTF(s);
            case List<?> list -> {
                out.writeByte(list.isEmpty() ? END : type(list.getFirst()));
                out.writeInt(list.size());
                for (Object element : list) {
                    writePayload(out, element);
                }
            }
            case Map<?, ?> map -> writeCompound(out, map);
            case int[] array -> {
                out.writeInt(array.length);
                for (int i : array) {
                    out.writeInt(i);
                }
            }
            case long[] array -> {
                out.writeInt(array.length);
                for (long l : array) {
                    out.writeLong(l);
                }
            }
            default -> throw new IllegalArgumentException("Not a tag: " + value.getClass().getSimpleName());
        }
    }

    private static int type(Object value) {
        return switch (value) {
            case Byte b -> BYTE;
            case Short s -> SHORT;
            case Integer i -> INT;
            case Long l -> LONG;
            case Float f -> FLOAT;
            case Double d -> DOUBLE;
            case byte[] array -> BYTE_ARRAY;
            case String s -> STRING;
            case List<?> list -> LIST;
            case Map<?, ?> map -> COMPOUND;
            case int[] array -> INT_ARRAY;
            case long[] array -> LONG_ARRAY;
            default -> throw new IllegalArgumentException("Not a tag: " + value.getClass().getSimpleName());
        };
    }

    /** The SNBT text of a tag, as {@code /data get} shows it and Minecraft's TagParser reads it. */
    public static String snbt(Object value) {
        StringBuilder text = new StringBuilder();
        snbt(text, value);
        return text.toString();
    }

    private static void snbt(StringBuilder text, Object value) {
        switch (value) {
            case Byte b -> text.append(b).append('b');
            case Short s -> text.append(s).append('s');
            case Integer i -> text.append(i);
            case Long l -> text.append(l).append('L');
            case Float f -> text.append(f).append('f');
            case Double d -> text.append(d).append('d');
            case String s -> quote(text, s);
            case byte[] array -> {
                text.append("[B;");
                for (int i = 0; i < array.length; i++) {
                    text.append(i == 0 ? "" : ",").append(array[i]).append('B');
                }
                text.append(']');
            }
            case int[] array -> {
                text.append("[I;");
                for (int i = 0; i < array.length; i++) {
                    text.append(i == 0 ? "" : ",").append(array[i]);
                }
                text.append(']');
            }
            case long[] array -> {
                text.append("[L;");
                for (int i = 0; i < array.length; i++) {
                    text.append(i == 0 ? "" : ",").append(array[i]).append('L');
                }
                text.append(']');
            }
            case List<?> list -> {
                text.append('[');
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) {
                        text.append(',');
                    }
                    snbt(text, list.get(i));
                }
                text.append(']');
            }
            case Map<?, ?> map -> {
                text.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) {
                        text.append(',');
                    }
                    first = false;
                    String key = (String) entry.getKey();
                    if (PLAIN_KEY.matcher(key).matches()) {
                        text.append(key);
                    } else {
                        quote(text, key);
                    }
                    text.append(':');
                    snbt(text, entry.getValue());
                }
                text.append('}');
            }
            default -> throw new IllegalArgumentException("Not a tag: " + value.getClass().getSimpleName());
        }
    }

    /** A string in double quotes with the escapes of SNBT; line breaks become \n, so the text stays on one line. */
    private static void quote(StringBuilder text, String s) {
        text.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> text.append("\\\"");
                case '\\' -> text.append("\\\\");
                case '\n' -> text.append("\\n");
                case '\r' -> text.append("\\r");
                case '\t' -> text.append("\\t");
                default -> text.append(c);
            }
        }
        text.append('"');
    }

    /** A stream that fails when more than a number of bytes are read from it. */
    private static final class Limited extends java.io.FilterInputStream {
        private final long max;
        private long read;

        Limited(InputStream in, long max) {
            super(in);
            this.max = max;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0) {
                count(n);
            }
            return n;
        }

        private void count(long n) {
            read += n;
            if (read > max) {
                throw new NbtException("The data is larger than " + max + " bytes");
            }
        }
    }
}
