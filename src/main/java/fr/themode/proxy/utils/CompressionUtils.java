package fr.themode.proxy.utils;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {

    public static void compress(Deflater deflater, ByteBuffer input, ByteBuffer output) throws DataFormatException {
        deflater.setInput(input);
        deflater.deflate(output);
        deflater.reset();
        output.flip();
    }

    public static void decompress(Inflater inflater, ByteBuffer input, ByteBuffer output) throws DataFormatException {
        inflater.setInput(input);
        inflater.inflate(output);
        inflater.reset();
        output.flip();
    }
}
