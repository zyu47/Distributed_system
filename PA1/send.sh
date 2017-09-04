#!/bin/sh

path=$HOME/CS555/PA1/bin
machine_list=$path/proc_set.txt

# ipAddr:Port:Name

for i in `cat $machine_list`
do
IFS=':' read -r -a array <<< "${i}"
echo 'logging into '${array[2]}
gnome-terminal -x bash -c "ssh -t ${array[0]} 'cd ${path}; java hw1.Send; bash;'"  &
done

