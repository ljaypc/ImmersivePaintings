package immersive_paintings.network;

import immersive_paintings.cobalt.network.Message;
import immersive_paintings.resources.ByteImage;
import net.minecraft.entity.player.PlayerEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class SegmentedPaintingMessage implements Message {
    @Serial
    private static final long serialVersionUID = -6584975870115489847L;

    private final byte[] data;
    private final int segment;
    private final int totalSegments;

    private static final Map<String, List<byte[]>> buffer = new HashMap<>();

    public SegmentedPaintingMessage(byte[] data, int segment, int totalSegments) {
        this.data = data;
        this.segment = segment;
        this.totalSegments = totalSegments;
    }

    abstract protected String getIdentifier(PlayerEntity e);

    abstract protected void process(PlayerEntity e, ByteImage image);

    @Override
    public void receive(PlayerEntity e) {
        String i = getIdentifier(e);

        List<byte[]> byteBuffer = buffer.computeIfAbsent(i, (k) -> new LinkedList<>());
        byteBuffer.add(data);

        if (segment + 1 == totalSegments) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (byte[] b : byteBuffer) {
                try {
                    outputStream.write(b);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                ByteImage image = ByteImage.read(outputStream.toByteArray());
                process(e, image);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            buffer.remove(i);
        }
    }
}
