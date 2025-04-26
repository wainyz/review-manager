# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/2/27:10:03
@email:Player_simple@163.com
@IDE:PyCharm
============================
"""
import scrapy


# 提供构造函数，输入一个str
class RenrenSpider(scrapy.Spider):
    name = "renren"
    allowed_domains = ["."]
    start_urls = []

    def parse(self, response):

        pass
