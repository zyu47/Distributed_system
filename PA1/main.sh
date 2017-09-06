#!/bin/sh

path=$HOME/CS555/PA1/bin
server_list_path=$HOME/CS555/PA1/server_list.txt
collate_addr_path=$HOME/CS555/PA1/collate_addr.txt

# Address format	->	ipAddr:Port:Name

# First start collate
collate_addr=$(head -n 1 $collate_addr_path)
IFS=':' read -r -a collate_array <<< "${collate_addr}"
gnome-terminal -x bash -c "ssh -t ${collate_array[0]} 'cd ${path}; java hw1.Collate ${collate_array[1]}; bash;'"  &

# Then start processes
for i in `cat $server_list_path`
do
IFS=':' read -r -a array <<< "${i}"
echo 'logging into '${array[2]}
gnome-terminal -x bash -c "ssh -t ${array[0]} 'cd ${path}; java hw1.Main ${array[1]}; bash;'"  &
done

