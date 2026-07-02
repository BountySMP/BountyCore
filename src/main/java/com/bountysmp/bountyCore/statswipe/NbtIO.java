package com.bountysmp.bountyCore.statswipe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal NBT codec for editing player .dat files. Round-trips every tag
 * type losslessly. Compounds are LinkedHashMap&lt;String,Object&gt;, lists are
 * {@link NbtList}, primitives are their boxed types, arrays stay arrays.
 */
final class NbtIO {

    record NbtList(byte elementType, List<Object> values) {}

    record Root(String name, LinkedHashMap<String, Object> compound) {}

    private NbtIO() {}

    static Root readRoot(DataInput in) throws IOException {
        byte type = in.readByte();
        if (type != 10) throw new IOException("Root tag is not a compound (type " + type + ")");
        String name = in.readUTF();
        return new Root(name, readCompound(in));
    }

    static void writeRoot(DataOutput out, Root root) throws IOException {
        out.writeByte(10);
        out.writeUTF(root.name());
        writeCompound(out, root.compound());
    }

    private static LinkedHashMap<String, Object> readCompound(DataInput in) throws IOException {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        while (true) {
            byte type = in.readByte();
            if (type == 0) return map;
            String name = in.readUTF();
            map.put(name, readPayload(in, type));
        }
    }

    private static Object readPayload(DataInput in, byte type) throws IOException {
        return switch (type) {
            case 1 -> in.readByte();
            case 2 -> in.readShort();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readFloat();
            case 6 -> in.readDouble();
            case 7 -> {
                byte[] a = new byte[in.readInt()];
                in.readFully(a);
                yield a;
            }
            case 8 -> in.readUTF();
            case 9 -> {
                byte elementType = in.readByte();
                int length = in.readInt();
                List<Object> values = new ArrayList<>(Math.max(0, length));
                for (int i = 0; i < length; i++) values.add(readPayload(in, elementType));
                yield new NbtList(elementType, values);
            }
            case 10 -> readCompound(in);
            case 11 -> {
                int[] a = new int[in.readInt()];
                for (int i = 0; i < a.length; i++) a[i] = in.readInt();
                yield a;
            }
            case 12 -> {
                long[] a = new long[in.readInt()];
                for (int i = 0; i < a.length; i++) a[i] = in.readLong();
                yield a;
            }
            default -> throw new IOException("Unknown NBT tag type " + type);
        };
    }

    private static void writeCompound(DataOutput out, Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            byte type = typeOf(entry.getValue());
            out.writeByte(type);
            out.writeUTF(entry.getKey());
            writePayload(out, type, entry.getValue());
        }
        out.writeByte(0);
    }

    private static byte typeOf(Object value) {
        if (value instanceof Byte)    return 1;
        if (value instanceof Short)   return 2;
        if (value instanceof Integer) return 3;
        if (value instanceof Long)    return 4;
        if (value instanceof Float)   return 5;
        if (value instanceof Double)  return 6;
        if (value instanceof byte[])  return 7;
        if (value instanceof String)  return 8;
        if (value instanceof NbtList) return 9;
        if (value instanceof Map)     return 10;
        if (value instanceof int[])   return 11;
        if (value instanceof long[])  return 12;
        throw new IllegalArgumentException("Unsupported NBT value type: " + value.getClass());
    }

    @SuppressWarnings("unchecked")
    private static void writePayload(DataOutput out, byte type, Object value) throws IOException {
        switch (type) {
            case 1 -> out.writeByte((Byte) value);
            case 2 -> out.writeShort((Short) value);
            case 3 -> out.writeInt((Integer) value);
            case 4 -> out.writeLong((Long) value);
            case 5 -> out.writeFloat((Float) value);
            case 6 -> out.writeDouble((Double) value);
            case 7 -> {
                byte[] a = (byte[]) value;
                out.writeInt(a.length);
                out.write(a);
            }
            case 8 -> out.writeUTF((String) value);
            case 9 -> {
                NbtList list = (NbtList) value;
                out.writeByte(list.elementType());
                out.writeInt(list.values().size());
                for (Object element : list.values()) writePayload(out, list.elementType(), element);
            }
            case 10 -> writeCompound(out, (Map<String, Object>) value);
            case 11 -> {
                int[] a = (int[]) value;
                out.writeInt(a.length);
                for (int i : a) out.writeInt(i);
            }
            case 12 -> {
                long[] a = (long[]) value;
                out.writeInt(a.length);
                for (long l : a) out.writeLong(l);
            }
        }
    }
}
