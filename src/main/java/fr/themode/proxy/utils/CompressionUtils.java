package fr.themode.proxy.utils;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {

    private static final ThreadLocal<Deflater> DEFLATER = ThreadLocal.withInitial(Deflater::new);
    private static final ThreadLocal<Inflater> INFLATER = ThreadLocal.withInitial(Inflater::new);

    public static void decompress(ByteBuffer input, int size, ByteBuffer output) throws DataFormatException {
        output.clear().limit(size);

        Inflater inflater = INFLATER.get();
        inflater.setInput(input);
        inflater.inflate(output);
        inflater.reset();

        output.flip();
    }
}
