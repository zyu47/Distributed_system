This is HW2 for CS555.

There are 4 packages:
dep (all dependency functions)
dis (discoveryNode)
peer (peer node class)
store (store program)

To start a discovery node: java dis.DiscoveryNode <port>
(The discovery node address should be stored in a text file so that every other node knows about it)

To start a storing program: java store.Store
This will read 1 or more lines of file path specified by a user, and the file name is used as the input for the hash function. If a user wants to specify an ID, user this format: filePath*ID.

To start a peer: java peer.Peer <port> <userID>
If a user ID is not specified, it will take the current time stamp to generate a 16-bit hash ID.

To print diagnostic information, type PRINT
To leave the network, type EXIT
