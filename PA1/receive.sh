#!/bin/sh

test_home=$HOME/CS555/PA1/bin

for i in `cat $test_home/machine_list.txt`
do
echo 'logging into '${i}
gnome-terminal -x bash -c "ssh -t ${i} 'cd ${test_home}; java hw1.Receive 6666; bash;'"  &
#gnome-terminal -x bash -c "ssh -t ${i} 'hostname -I;bash;'"  &
done

