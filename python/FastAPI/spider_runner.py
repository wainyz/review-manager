# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/2/25:16:56
@email:Player_simple@163.com
@IDE:PyCharm
============================
"""
from scrapy.crawler import  CrawlerRunner
from scrapy.utils.project import get_project_settings
from crochet import setup
from QA_Spider.QA_Spider.spiders.websiteSpider.base_smartedu import BaseSmarteduSpider
from scrapy.signalmanager import dispatcher
from scrapy import signals

# 初始化 crochet
setup()


def run_spider(keyword,offset,max_results):
    items = []

    # 注册信号回调
    def collect_items(sender, item, **kwargs):
        items.append(item)
        return item

    # 绑定信号
    dispatcher.connect(collect_items, signal=signals.item_scraped)

    # 启动爬虫
    runner = CrawlerRunner(get_project_settings())
    deferred = runner.crawl(BaseSmarteduSpider, keyword=keyword, offset = offset, max_pages = max_results)
    deferred.addCallback(lambda _: items)
    return deferred

def choose_difference_crawl(keyword):
    # 如果是中小学，可以调用base_smartedu
    #TODO: 调用大模型判断需要爬取哪一个网站
