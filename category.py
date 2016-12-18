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


def hhssee():
    response = requests.get('http://www.hhssee.com/')
    soup = bs4.BeautifulSoup(response.text, 'lxml')
    result = [("全部", "")]
    for node in soup.select('#iHBG > div.cHNav > div > span > a'):
        result.append((node.get_text(), re.split('_|\\.', node.get('href'))[1]))
    return result


def dm5():
    response = requests.get('http://www.dm5.com/manhua-latest/')
    soup = bs4.BeautifulSoup(response.text, 'lxml')
    result = [("全部", "")]
    for node in soup.select('#index_left > div.inkk > div.syzm > span.new_span_bak > a'):
        result.append((node.get_text(), node.get('href').split('-')[2][2:-1]))
    return result


def u17():
    response = requests.get('http://www.u17.com/comic_list/th99_gr99_ca99_ss0_ob0_ac0_as0_wm0_co99_ct99_p1.html')
    soup = bs4.BeautifulSoup(response.text, 'lxml')
    result = [("全部", "th99")]
    for node in soup.select('#select > div.fr > dl.subject > dd > a'):
            result.append((node.get_text(), node.get('href').split('/')[-1].split('_')[0]))
    return result


def build(func):
    print('List<Pair<String, String>> list = new ArrayList<>();')
    for t in func():
        print('list.add(Pair.create("{}", "{}"));'.format(t[0], t[1]))
    print('return list;')


if __name__ == '__main__':
    build(u17)
