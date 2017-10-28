import numpy as np
import os
import itertools
import operator
from operator import itemgetter

samplePath = '/s/chopin/k/grad/zhixian/CS555/PA3/sampleData/sample'
files = os.listdir(samplePath)
info_set = []

def readInfo():
    for fileName in files:
        f = open(os.path.join(samplePath, fileName),'r')
        f.readline()
        for line in f:
            tags = line.split('\t')
            if len(tags) != 54:
                continue
            try:
                genList = getGenre(tags[13], tags[14])
                for gen in genList:
                    info_set.append({})
                    try:
                        info_set[-1]['artistID'] = tags[4]
                    except:
                        pass
                    try:
                        info_set[-1]['genre'] = gen#getGenre(tags[13], tags[14])
                    except:
                        pass
                    try:
                        info_set[-1]['tempo'] = float(tags[47])
                    except:
                        pass
                    try:
                        info_set[-1]['hotness'] = float(tags[42])
                    except:
                        pass
                    try:
                        info_set[-1]['artistName'] = tags[11]
                    except:
                        pass
                    try:
                        info_set[-1]['title'] = tags[50]
                    except:
                        pass
                    try:
                        info_set[-1]['year'] = int(tags[53])
                    except:
                        pass
                    try:
                        info_set[-1]['loudness'] = float(tags[27])
                    except:
                        pass
            except:
                pass
        f.close()

def getGenre(terms, freqs):
    term_list = (terms[4:-4]).split("\"\", \"\"")
    freq_list_string = (freqs[1:-1]).split(', ')
    freq_list = [float(i) for i in freq_list_string]
    fmax = np.max(freq_list)
    res = []
    if len(term_list) == len(freq_list):
        for i,f in enumerate(freq_list):
            if f == fmax:
                res.append(term_list[i])
    return res

def most_common(L):
    # get an iterable of (item, iterable) pairs
    SL = sorted((x, i) for i, x in enumerate(L))
    # print 'SL:', SL
    groups = itertools.groupby(SL, key=operator.itemgetter(0))
    # auxiliary function to get "quality" for an item
    def _auxfun(g):
        item, iterable = g
        count = 0
        min_index = len(L)
        for _, where in iterable:
            count += 1
            min_index = min(min_index, where)
        # print 'item %r, count %r, minind %r' % (item, count, min_index)
        return count, -min_index
    # pick the highest-count/earliest item
    return max(groups, key=_auxfun)[0]

def q1():
    res = {}
    for song in info_set:
        if 'genre' in song:
            if song['artistID'] in res:
                res[song['artistID']].append(song['genre'])
            else:
                res[song['artistID']] = [song['genre']]
    f = open('./result/q1.txt', 'w')
    for k in res:
        f.write('%s\t%s' %(k, most_common(res[k])))
        f.write('\n')
    f.close()

def q2():
    tempos = []
    for song in info_set:
        if 'tempo' in song:
            tempos.append(song['tempo'])
    f = open('./result/q2.txt', 'w')
    f.write('%s\t%s' %(len(tempos), np.mean(tempos)))
    f.close()    

def q4():
    tempos = []
    artistIDs = []
    for song in info_set:
        if 'tempo' in song and 'artistName' in song:
            tempos.append(song['tempo'])
            artistIDs.append(song['artistName'])
    sorted_index = sorted(range(len(tempos)), key=lambda k: tempos[k])
    
    f = open('./result/q4.txt', 'w')
    for i in range(1,11):
        f.write('%s\t%s' %(tempos[sorted_index[-i]], artistIDs[sorted_index[-i]]))
        f.write('\n')
    f.close()  

def q5():
    res = {}
    for song in info_set:
        if 'genre' in song and 'title' in song and 'hotness' in song and 'artistName' in song:
            if song['genre'] in res:
                res[song['genre']][0].append(song['hotness'])
                res[song['genre']][1].append(song['artistName'])
                res[song['genre']][2].append(song['title'])
            else:
                res[song['genre']] = [[song['hotness']],[song['artistName']],[song['title']]]  
                
    f = open('./result/q5.txt', 'w')
    for gen in res:
        sorted_index = sorted(range(len(res[gen][0])), key=lambda k: res[gen][0][k])
        if len(sorted_index) > 10:
            for i in range(1, 11):
                f.write('%s\t%s\t%s\t%s\n' %(gen, res[gen][0][sorted_index[-i]], res[gen][1][sorted_index[-i]],res[gen][2][sorted_index[-i]]))
        else:
            for i in sorted_index:
                f.write('%s\t%s\t%s\t%s\n' %(gen, res[gen][0][i], res[gen][1][i],res[gen][2][i]))
            
    f.close()

def q6():
    res = {}
    for song in info_set:
        if 'year' in song and 'loudness' in song :
            if song['year'] in res:
                res[song['year']].append(song['loudness'])
            else:
                res[song['year']] = [song['loudness']]         

    f = open('./result/q6.txt', 'w')
    for y in res:
        f.write(str(y))
        f.write('\t')
        f.write(str(np.mean(res[y])))
        f.write('\n')
            
    f.close()
    
def q7():
    res = {}
    for song in info_set:
        if 'artistID' in song:
            if song['artistID'] in res:
                res[song['artistID']] += 1
            else:
                res[song['artistID']] = 1        

    f = open('./result/q7.txt', 'w')
    for y in res:
        f.write(y)
        f.write('\t')
        f.write(str(res[y]))
        f.write('\n')
            
    f.close()    

def q8():
    res = {}
    for song in info_set:
        if 'genre' in song:
            if song['genre'] in res:
                res[song['genre']] += 1
                print(song['genre'], ' ', res[song['genre']])
            else:
                res[song['genre']] = 1        

    f = open('./result/q8.txt', 'w')
    sorted_res = sorted(res.items(), key=operator.itemgetter(1))
    for i in range(1, 11):
        f.write(sorted_res[-i][0])
        f.write('\t')
        f.write(str(sorted_res[-i][1]))
        f.write('\n')
            
    f.close()  

if __name__ == '__main__':
    readInfo()
    #q1()
   # q2()
    #q4()
    q5()
    #q8()
    
