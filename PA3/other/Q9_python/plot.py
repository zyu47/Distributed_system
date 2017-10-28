import matplotlib.pyplot as plt

f =open('q9_out', 'r')
years = []
tempos = []
for line in f:
    years.append(line.split('\t')[0])
    tempos.append(line.split('\t')[2])
f.close()

plt.plot(years, tempos)
plt.savefig('q9.png')
