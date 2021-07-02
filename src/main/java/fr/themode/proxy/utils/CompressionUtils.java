package fr.themode.proxy.utils;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CompressionUtils {
    public static void compress(Deflater deflater, ByteBuffer input, ByteBuffer output) {
        deflater.setInput(input);
        deflater.finish();
        deflater.deflate(output);
        deflater.reset();
    }

    public static void decompress(Inflater inflater, ByteBuffer input, ByteBuffer output) throws DataFormatException {
        inflater.setInput(input);
        inflater.inflate(output);
        inflater.reset();
    }
}
