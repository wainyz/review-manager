import scrapy


# 提供构造函数，输入一个str
class QaSpiderSpider(scrapy.Spider):
    name = "QA_Spider"
    allowed_domains = ["."]
    start_urls = []

    def parse(self, response):
        pass
