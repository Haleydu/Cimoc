import requests
import bs4
import re
import urllib.request


def ikanman():
    response = requests.get('http://www.ikanman.com/list/')
    soup = bs4.BeautifulSoup(response.content.decode('utf-8'), 'lxml')
    result = []
    for node in soup.select('div.filter-nav > div.filter > ul > li > a'):
        result.append((node.get_text(), node.get('href')[6:-1]))
    return result


def chuiyao():
    response = requests.get('http://m.chuiyao.com/')
    soup = bs4.BeautifulSoup(response.content.decode('utf-8'), 'lxml')
    result = []
    count = 0
    for node in soup.select('ul.cat-list > li > a'):
        if count == 0:
            result.append((node.get_text(), ''))
            result.append(('最近更新', '0'))
        else:
            result.append((node.get_text(), str(count)))
        count += 1
    return result


def cctuku():
    response = requests.get('http://m.tuku.cc/')
    soup = bs4.BeautifulSoup(response.text, 'lxml')
    result = []
    for node in soup.select('ul.pp > li > a[href*=/list/list]'):
        if node.get_text():
            result.append((node.get_text(), node.get('href').split('_')[1]))
    return result


def dmzj():
    response = requests.get('http://m.dmzj.com/classify.html')
    soup = bs4.BeautifulSoup(response.text, 'lxml')
    result = []
    for node in soup.select('#classCon > ul > li > a'):
        if node.get_text():
            result.append((node.get_text(), re.split('\\D+', node.get('onclick'))[2]))
    return result


def mh57():
    response = requests.get('http://www.57mh.com/list/')
    soup = bs4.BeautifulSoup(response.content.decode('utf-8'), 'lxml')
    result = []
    for node in soup.select('div.filter-nav > div.filter > ul > li > a'):
        result.append((node.get_text(), urllib.request.unquote(node.get('href').split('-')[1])))
    return result


def build(func):
    print('List<Pair<String, String>> list = new ArrayList<>();')
    for t in func():
        print('list.add(Pair.create("{}", "{}"));'.format(t[0], t[1]))
    print('return list;')


if __name__ == '__main__':
    build(mh57)
