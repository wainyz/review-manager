import scrapy
import json

headers = {
    'User-Agent': 'Apifox/1.0.0 (https://apifox.com)',
    'Content-Type': 'application/json',
    'Accept': '*/*',
    'Connection': 'keep-alive',
}


class MyItem(scrapy.Item):
    data = scrapy.Field()


# 提供构造函数，输入一个str
class BaseSmarteduSpider(scrapy.Spider):
    name = "base_smartedu"
    allowed_domains = ["basic.smartedu.cn",
                       "r2-ndr.ykt.cbern.com.cn",
                       "r3-ndr.ykt.cbern.com.cn",
                       "r1-ndr.ykt.cbern.com.cn",
                       "e-teacher-studio-gateway.ykt.eduyun.cn",
                       "x-search.ykt.eduyun.cn"]
    search_url = 'https://x-search.ykt.eduyun.cn/v1/resources/search'
    source_url = 'https://e-teacher-studio-gateway.ykt.eduyun.cn/v2/contents/${content_id}?studio_id=${studio_id}'
    base_url = 'https://r2-ndr.ykt.cbern.com.cn'
    start_urls = []

    def __init__(self, keyword: str, max_pages: int = 10, offset=0, collector=None, **kwargs):
        super().__init__(**kwargs)
        self.keyword = keyword
        self.max_pages = max_pages
        self.offset = offset
        self.collector = collector  # 数据收集器

    def start_requests(self):
        payload = {
            "keyword": self.keyword,
            "tab_codes": [
                "examinationPapers",
                "labourEdu",
                "schoolService",
                "specialEdu",
                "teacherTraining",
                "eduReform",
                "tchMaterial",
                "areaSite",
                "studio-inst",
                "studio-inst-res",
                "teach-studio",
                "teach-studio-res",
                "expert-studio",
                "expert-studio-res",
                "school-space",
                "school-space-res"
            ],
            "cross_tenant": True,
            "duplicate_filter": True,
            "search_order": {
                "field": "_score",
                "direction": "desc"
            },
            "offset": self.offset,
            "limit": self.max_pages
        }
        # JSON 负载
        # 将字典转换为 JSON 字符串
        body = json.dumps(payload)

        yield scrapy.Request(BaseSmarteduSpider.search_url, callback=self.parse, method="POST", body=body,
                             headers=headers)

    def parse(self, response):
        # print("response: " + response.text)
        # 如果是search结果(通过判断response，如果第一个字段是total）
        if response.url.__contains__('/search'):
            data = json.loads(response.body)
            items = data['items']
            for page in items:
                # 通过 title判断是否是试卷
                if str(page['title']).__contains__("题") or str(page['title']).__contains__("考试"):
                    studio_id = str(page['library_id']).split("_")[0]
                    content_id = str(page['resource_id'])
                    sourceUrl = BaseSmarteduSpider.source_url.replace("${content_id}", content_id).replace(
                        "${studio_id}", studio_id)
                    yield scrapy.Request(sourceUrl, headers=headers,
                                         callback=self.parse_source, method="GET", errback=self.handle_error)

            # 如果是分析资源（判断response如果第一个字段是tenant_id）

    def parse_source(self, response):
        # 在这个response中寻找pdf链接
        pdf_url = ''
        data = json.loads(response.body)
        title = data['global_title']['zh-CN']
        for source in data['ti_items']:
            if str(source["ti_file_flag"]).__eq__('pdf'):
                pdf_url = BaseSmarteduSpider.base_url + str(source["ti_storage"]).split("${ref-path}")[1]
                break
        print(title + ".pdf:" + pdf_url)
        yield MyItem(data=pdf_url)

    def handle_error(self, failure):
        self.logger.error(f"Request failed: {failure.value}")
