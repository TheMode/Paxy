registerOutgoing("play", "chat-message", (context, packet) => {
    context.count = (context.count ?? 0) + 1
    console.log("test " + context.count)
    packet.message = `{"text":"No message for you."}`
})