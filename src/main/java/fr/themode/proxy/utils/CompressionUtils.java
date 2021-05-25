package fr.themode.proxy.utils;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressionUtils {

    public static void decompress(Inflater inflater, ByteBuffer input, int size, ByteBuffer output) throws DataFormatException {
        output.clear().limit(size);

        inflater.setInput(input);
        inflater.inflate(output);
        inflater.reset();

        output.flip();
    }
}
