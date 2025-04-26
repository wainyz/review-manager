# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/2/27:9:05
@email:Player_simple@163.com
@IDE:PyCharm
============================
"""
import re

import scrapy

headers = {
    'User-Agent': 'Apifox/1.0.0 (https://apifox.com)',
    'Content-Type': 'application/json',
    'Accept': '*/*',
    'Connection': 'keep-alive',
}


# 提供构造函数，输入一个str
class EolSpider(scrapy.Spider):
    name = "eol"
    allowed_domains = ["eol.cn",
                       "gaokao.eol.cn"
                       ]
    search_url = 'https://www.eol.cn/e_html/gk/gkst/'
    start_urls = []

    def __init__(self, keyword: str, max_pages: int = 10, offset=0, collector=None, **kwargs):
        super().__init__(**kwargs)
        self.keyword = keyword
        self.max_pages = max_pages
        self.offset = offset
        self.collector = collector  # 数据收集器

    def start_requests(self):
        if self.keyword.__contains__("年"):
            # 查找字符串中的所有数字
            pattern = re.compile(r'\d+年')
            year = pattern.findall(self.keyword)
            url = EolSpider.search_url
            if not year.__contains__('24'):
                url.__add__(f'${year}.shtml')
            yield scrapy.Request(url, callback=self.parse, method="GET",
                                 headers=headers)
        else:
            # 遍历2024 - 2020年的对应考题
            for year in range(2020, 2023):
                url = EolSpider.search_url.__add__(f'{year}.shtml')
                yield scrapy.Request(url, callback=self.parse, method="GET",
                                     headers=headers)

    def parse(self, response):
        # 找到对应主题的题目
        keyword = self.keyword
        # 判断response状态码
        if response.status != 200:
            return
        # class="sline"
        parent = response.xpath("//div[class='sline']")
        for node in parent.xpath(".node()"):
            links = node.xpath("//div[class='xueke-a']/a")
            print(links)
            for link in links:
                yield scrapy.Request(url=link.attrib.get('href'), method="GET", headers=headers,callback=self.parse_source)

    def parse_source(self,response):
        # 如果是真题就找doc的链接
        1
#        if str(response.url).__contains__("zhenti") or str(response.url).__contains__('shiti')