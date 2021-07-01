package fr.themode.proxy.protocol;

import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.utils.ConnectionStateUtils;

import java.nio.ByteBuffer;

/**
 * Handles basic packets (e.g. game state) to ensure proper parsing.
 */
public interface ProtocolHandler {
    ProtocolHandler CLIENT = ConnectionStateUtils::handleClientState;
    ProtocolHandler SERVER = ConnectionStateUtils::handleServerState;

    void process(ConnectionContext context, int packetId, ByteBuffer payload);
}
