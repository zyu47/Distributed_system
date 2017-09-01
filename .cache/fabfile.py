from fabric.api import run

cd = 'cd /s/chopin/k/grad/zhixian/CS555/PA1/bin && '
port = '6666'


def receive():
	run(cd + 'java hw1.Receive ' + port)

def send():
	run(cd + 'java hw1.Send')

def collate():
	run(cd + 'java hw1.Collate ' + port)
