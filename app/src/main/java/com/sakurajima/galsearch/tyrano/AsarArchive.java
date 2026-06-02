package com.sakurajima.galsearch.tyrano;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Minimal ASAR reader for Tyrano/NW.js style packages.
 *
 * Supports the common Electron ASAR layout used by Tyranor:
 * - header magic/version = 4
 * - header size
 * - json length
 * - header json
 * - file data area
 */
public class AsarArchive {
    private static final String TAG = "YukiAsar";

    private final File archiveFile;
    private final RandomAccessFile raf;
    private final long dataOffset;
    private final Map<String, Entry> entries = new HashMap<>();

    public AsarArchive(File file) throws Exception {
        this.archiveFile = file == null ? null : file.getCanonicalFile();
        if (this.archiveFile == null || !this.archiveFile.isFile()) {
            throw new IllegalArgumentException("asar file missing");
        }
        this.raf = new RandomAccessFile(this.archiveFile, "r");
        int magic = readIntLE(raf);
        if (magic != 4) {
            throw new IllegalStateException("not asar file");
        }
        long headerSize = readUInt32LE(raf);
        // Keep the same layout as Tyranor's F0.e:
        // read magic(4), read headerSize(4), dataOffset = 8 + headerSize,
        // skip one 4-byte field, then read the actual JSON byte length.
        this.dataOffset = 8L + headerSize;
        raf.skipBytes(4);
        long jsonLen = readUInt32LE(raf);
        byte[] jsonBytes = new byte[(int) jsonLen];
        raf.readFully(jsonBytes);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        parseNode("", new JSONObject(json));
        Log.i(TAG, "asar loaded file=" + this.archiveFile + " entries=" + entries.size() + " dataOffset=" + dataOffset);
    }

    public boolean has(String path) {
        return entries.containsKey(normalize(path));
    }

    public boolean isDirectory(String path) {
        Entry e = entries.get(normalize(path));
        return e != null && e.directory;
    }

    public byte[] read(String path) {
        try {
            Entry e = entries.get(normalize(path));
            if (e == null || e.directory) return null;
            byte[] data = new byte[(int) e.size];
            synchronized (raf) {
                raf.seek(dataOffset + e.offset);
                raf.readFully(data);
            }
            return data;
        } catch (Throwable t) {
            Log.w(TAG, "read failed path=" + path, t);
            return null;
        }
    }

    public String getArchiveName() {
        return archiveFile == null ? "" : archiveFile.getName();
    }

    private void parseNode(String prefix, JSONObject node) throws Exception {
        JSONObject files = node.optJSONObject("files");
        if (files == null) {
            if (!prefix.isEmpty()) {
                long size = parseLong(node.optString("size", "0"));
                long offset = parseLong(node.optString("offset", "0"));
                entries.put(normalize(prefix), new Entry(false, size, offset));
            }
            return;
        }
        if (!prefix.isEmpty()) {
            entries.put(normalize(prefix), new Entry(true, 0, 0));
        }
        Iterator<String> keys = files.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            JSONObject child = files.optJSONObject(name);
            if (child == null) continue;
            String next = prefix.isEmpty() ? name : prefix + "/" + name;
            parseNode(next, child);
        }
    }

    private static String normalize(String path) {
        if (path == null) return "";
        String p = path.trim().replace('\\', '/');
        while (p.startsWith("/")) p = p.substring(1);
        return p;
    }

    private static int readIntLE(RandomAccessFile raf) throws Exception {
        return (int) readUInt32LE(raf);
    }

    private static long readUInt32LE(RandomAccessFile raf) throws Exception {
        int b1 = raf.read();
        int b2 = raf.read();
        int b3 = raf.read();
        int b4 = raf.read();
        if ((b1 | b2 | b3 | b4) < 0) throw new java.io.EOFException();
        return ((long) b4 << 24) | ((long) b3 << 16) | ((long) b2 << 8) | (long) b1;
    }

    private static long parseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return 0L;
            return Long.parseLong(value.trim());
        } catch (Throwable ignored) {
            return 0L;
        }
    }

    private static final class Entry {
        final boolean directory;
        final long size;
        final long offset;

        Entry(boolean directory, long size, long offset) {
            this.directory = directory;
            this.size = size;
            this.offset = offset;
        }
    }
}