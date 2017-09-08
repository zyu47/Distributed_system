1 ./src/ contains all the source files, including two packages hw1 (major classes) and dep (dependencies)
2. Send.java handles sending messages; Receive.java handles receiving messages; UpdateCollate.java handles reporting sent/received statistics to Collator, which will be running the Collate.java process
3. server_list.txt contains the server addresses with the following format: 
	ipAddress:port:machine_name
4. collate_addr.txt contains the collator process address
5. main.sh will read server addresses and collator address, and ssh to each computer and start the process
