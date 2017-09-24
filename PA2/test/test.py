import matplotlib.pyplot as plt

#x = ['8cd6', 'ce61',  'da2f', 'ecc9']
x = ['ce61',  'da2f']#, 'ecc9']
plt.plot(range(2**16), [0 for i in range(2**16)])
for i in x:
	plt.plot(int(i, 16), 0, 'r.')
	
for i, v in enumerate(x):
	for j in range(16):
		plt.plot((int(v, 16) + 2**j)%(2**16), 0.1+i*0.5, 'g.')
plt.show()
