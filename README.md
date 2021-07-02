# Overview
The goal of this project is to offer an efficient way to transform every in/outgoing packets without butchering the game server performance. 

The project is far from done, and nothing say that it will ever be. It can however be used to force current proxy developers to care a bit more 
about pipeline control & buffer allocation/pooling :)

# Performance
The proxy is able to run with 30MB of heap and around 6MB of direct memory per network thread, other than that the code is almost free of any allocation.
Early tests show x2-8 cpu reduction compared to the current top of the line, to be taken with a grain of salt considering the lack of parity 
(no multi-server support & no encryption)

# Packet transformation
Scripts are done in JS thanks to [GraalJS](https://github.com/oracle/graaljs), example are present [here](https://github.com/TheMode/Proxy/tree/master/scripts).

# Run
Paxy being an experimental project, we should not be afraid to use experimental features. Testing is being done on an [JDK 17 Early-Access build](https://jdk.java.net/17/) with the plan of using the WIP [Foreign Function & Memory API](https://openjdk.java.net/jeps/412).
