package fr.themode.proxy.utils;

import fr.themode.proxy.ConnectionState;
import fr.themode.proxy.connection.ConnectionContext;

import java.nio.ByteBuffer;

public class ConnectionStateUtils {

    public static void handleClientState(ConnectionContext context, int packetId, ByteBuffer payload) {
        final var state = context.getState();
        if (state == ConnectionState.HANDSHAKE && packetId == 0) {
            // Change to Status/Login state
            final int protocol = ProtocolUtils.readVarInt(payload);
            final String address = ProtocolUtils.readString(payload, 255);
            final short port = payload.getShort();
            final int stateId = ProtocolUtils.readVarInt(payload);
            final ConnectionState nextState = switch (stateId) {
                case 1 -> ConnectionState.STATUS;
                case 2 -> ConnectionState.LOGIN;
                default -> throw new IllegalStateException("Unexpected value: " + stateId);
            };
            context.setState(nextState);
            context.getTargetContext().setState(nextState);
        } else if (state == ConnectionState.LOGIN && packetId == 0) {
            // Change to Play state
            context.setState(ConnectionState.PLAY);
        }
    }

    public static void handleServerState(ConnectionContext context, int packetId, ByteBuffer payload) {
        final var state = context.getState();
        if (state == ConnectionState.LOGIN) {
            // Change to Play State
            if (packetId == 2) {
                context.setState(ConnectionState.PLAY);
            } else if (packetId == 3) {
                // Compression
                final int threshold = ProtocolUtils.readVarInt(payload);
                context.setCompression(threshold);
                context.getTargetContext().setCompression(threshold);
            }
            context.setState(ConnectionState.PLAY);
        }
    }
}
