# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/4/1:23:22
@email:Player_simple@163.com
@IDE:PyCharm
============================
"""
import pika
import threading
import logging
import time

class ChannelManager:
    """线程安全的通道管理器"""
    _instance = None
    _lock = threading.Lock()
    _connection = None
    _channel = None
    _last_checked = 0

    def __new__(cls):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super().__new__(cls)
        return cls._instance

    def _create_connection(self):
        """创建新连接和通道"""
        try:
            self._connection = pika.BlockingConnection(
                pika.ConnectionParameters(
                    host='localhost',
                    port=5672,
                    credentials=pika.PlainCredentials('guest', 'guest'),
                    heartbeat=600,
                    blocked_connection_timeout=300
                )
            )
            self._channel = self._connection.channel()
            self._channel.queue_declare(queue="deepseek_response", durable=True)
            logging.info("成功创建新连接和通道")
            return True
        except Exception as e:
            logging.error(f"连接失败: {str(e)}")
            self._connection = None
            self._channel = None
            return False

    def get_channel(self):
        """获取有效通道（自动重连）"""
        with self._lock:
            # 每5分钟强制检查一次连接
            force_check = time.time() - self._last_checked > 300

            if self._channel and not force_check:
                if self._channel.is_open:
                    return self._channel

            # 需要重建连接
            retries = 3
            for attempt in range(retries):
                if self._create_connection():
                    self._last_checked = time.time()
                    return self._channel
                logging.warning(f"连接尝试 {attempt + 1}/{retries} 失败")
                time.sleep(2 ** attempt)  # 指数退避

            raise Exception("无法建立RabbitMQ连接")




# # 使用示例
# def callback_wrapper(ch, method, properties, body):
#     """消息处理回调"""
#     try:
#         request_data = json.loads(body)
#         # 提交任务到线程池
#         future = executor.submit(process_request, request_data)
#         future.add_done_callback(
#             lambda f: handle_response(f.result(), request_data['request_id'])
#         )
#     finally:
#         # 使用原始通道确认消息
#         if ch.is_open:
#             ch.basic_ack(method.delivery_tag)
#         else:
#             logging.error("原始通道已关闭，无法确认消息")