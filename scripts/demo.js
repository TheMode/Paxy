proxy.registerOutgoing("chat-message", (context, packet) => {
    packet.message = `{"text":"No message for you."}`
})