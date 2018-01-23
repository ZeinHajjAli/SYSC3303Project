Theodore Hronowsky
101008637
SYSC 3303 assignment1

Instructions:
1. Run Server -> main by running as >> Java Application 
2. Run Host -> main by running as >> Java Application
3. Run Client -> main by running as >> Java Application
4. View actions in console 

FILES:

Client.java
-Client class that performs 11 messages including read, write and invalid. 
-Host and Server must be running before Client will work.
-Client sends requests to the intermediate host.

Host.java
-Host class sends requests from the Client to the Server
-Host must be running before the Client can send requests
-Server must be running in order to pass on the requests. 

Server.java
-Server class that waits to receive Datagram Packets from the Host
-Server class sends back responses to the client after being passed on by the Host