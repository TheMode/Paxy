proxy.registerOutgoing("chat-message", (context, packet) => {
    console.log("chat debug")
    packet.message = `{"text":"No message for you."}`
})