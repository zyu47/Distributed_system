#!/bin/sh

path=$HOME/CS555/PA3/conf/slaves

# Address format	->	ipAddr:Port:Name

# Then start processes
for i in `cat $path`
do
echo 'logging into '${i}
gnome-terminal -x bash -c "ssh -t ${i} 'cd /s/${i}/a/nobackup/cs555/zhixian; rm -rf *; bash;'"  &
done

