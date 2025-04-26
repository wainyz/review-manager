from fastapi import FastAPI, Request, HTTPException
from pydantic import BaseModel
from typing import Optional
from twisted.internet import reactor


# 允许访问的 IP 列表
from FastAPI.spider_runner import run_spider

ALLOWED_IPS = ["127.0.0.1", "localhost","::1"]  # 替换为允许的 IP 地址

# 创建 FastAPI 应用
app = FastAPI()


# 定义请求体模型
class CrawlRequest(BaseModel):
    keyword: str  # 爬取的关键词
    offsite: int = 0
    max_results: Optional[int] = 10  # 最大结果数，默认为 10



# IP 限制中间件
@app.middleware("http")
async def ip_restriction_middleware(request: Request, call_next):
    """
    中间件：检查客户端 IP 是否在允许的 IP 列表中。
    """
    client_ip = request.client.host  # 获取客户端 IP

    if client_ip not in ALLOWED_IPS:
        raise HTTPException(status_code=403, detail=f"IP {client_ip} is not allowed")

    response = await call_next(request)
    return response


# 定义 API 路由
@app.post("/crawl")
async def crawl(request: CrawlRequest, client_ip: str = None):
    """
    接收爬取关键词，返回爬取结果。
    """
    keyword = request.keyword
    if not keyword:
        raise HTTPException(status_code=400, detail="Keyword cannot be empty")

    # 调用爬取函数
    try:
        # 异步启动爬虫（非阻塞）
        deferred = run_spider(keyword=keyword, offsite=request.offsite, max_results=request.max_results)
        return {"status": "success", "data": deferred.result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 运行 FastAPI 应用
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="localhost", port=8080)
