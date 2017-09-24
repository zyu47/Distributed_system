#!/bin/sh

path=$HOME/CS555/PA2
bin=$path/bin
server_list_path=$path/server_list.txt
collate_addr_path=$path/discovery_addr.txt

# Address format	->	ipAddr:Port:Name

# First start discovery node
collate_addr=$(head -n 1 $collate_addr_path)
IFS=':' read -r -a collate_array <<< "${collate_addr}"
gnome-terminal -x bash -c "ssh -t ${collate_array[0]} 'cd ${bin}; java dis.DiscoveryNode ${collate_array[1]}; bash;'"  &

# Then start processes
for i in `cat $server_list_path`
do
IFS=':' read -r -a array <<< "${i}"
echo 'logging into '${array[2]}
#gnome-terminal -x bash -c "ssh -t ${array[0]} 'cd ${path}; java hw1.Main ${array[1]}; bash;'"  &
gnome-terminal -x bash -c "ssh -t ${array[0]} 'cd ${bin}; bash;'" &
done

