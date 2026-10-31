package me.TechsCode.UltraPermissions.base.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Read an external response without allowing it to exhaust heap or disk. */
public final class BoundedInput {
    private BoundedInput() {}

    public static byte[] read(InputStream source, int maxBytes) throws IOException {
        if (source == null || maxBytes < 0) throw new IllegalArgumentException("Invalid input limit");
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 8192));
        byte[] buffer = new byte[8192];
        int emptyReads = 0;
        int count;
        while ((count = source.read(buffer)) != -1) {
            if (count == 0) {
                if (++emptyReads > 8) throw new IOException("External source made no progress");
                continue;
            }
            emptyReads = 0;
            if (count > maxBytes - out.size()) throw new IOException("External source exceeds size limit");
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }
}
