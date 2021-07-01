package fr.themode.proxy.protocol;

import fr.themode.proxy.State;
import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;

/**
 * Handles basic packets (e.g. game state) to ensure proper parsing.
 */
public interface ProtocolHandler {
    ProtocolHandler CLIENT = (context, packetId, payload) -> {
        final var state = context.getState();
        if (state == State.HANDSHAKE && packetId == 0) {
            // Change to Status/Login state
            final int protocol = ProtocolUtils.readVarInt(payload);
            final String address = ProtocolUtils.readString(payload, 255);
            final short port = payload.getShort();
            final int stateId = ProtocolUtils.readVarInt(payload);
            final State nextState = switch (stateId) {
                case 1 -> State.STATUS;
                case 2 -> State.LOGIN;
                default -> throw new IllegalStateException("Unexpected value: " + stateId);
            };
            context.setState(nextState);
            context.getTargetContext().setState(nextState);
        } else if (state == State.LOGIN && packetId == 0) {
            // Change to Play state
            context.setState(State.PLAY);
        }
    };
    ProtocolHandler SERVER = (context, packetId, payload) -> {
        final var state = context.getState();
        if (state == State.LOGIN) {
            // Change to Play State
            if (packetId == 2) {
                context.setState(State.PLAY);
            } else if (packetId == 3) {
                // Compression
                final int threshold = ProtocolUtils.readVarInt(payload);
                context.setCompression(threshold);
                context.getTargetContext().setCompression(threshold);
            }
            context.setState(State.PLAY);
        }
    };

    void process(ConnectionContext context, int packetId, ByteBuffer payload);
}
