# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/4/1:19:15
@email:Player_simple@163.com
@IDE:PyCharm

receiver，接收来自rabbitMq deepseek_requests的 请求。
将请求解析，使用多线程调用 deepseek接口，将生成的消息传给generator。
============================
"""
import logging
import threading
import time

import pika
import json
from openai import OpenAI
from concurrent.futures import ThreadPoolExecutor

# RabbitMQ配置
from DeepSeek.generateQuestion.channel_manager import ChannelManager

RABBITMQ_HOST = 'localhost'
RABBITMQ_PORT = 5672
receiver_queue_name = "deepseek_requests"
productor_queue_name = "deepseek_response"
productor_exchange_name = "deepseek_response_exchange"
productor_routing_key = "deepseek_response"
productor_binding_key = "deepseek_response"
REQUEST_QUEUE = "deepseek_requests"
RESPONSE_QUEUE = "deepseek_response"
RESPONSE_SCORING_QUEUE = "deepseek_scoring_response"

# DeepSeek API配置
base_url = "https://api.deepseek.com"
api_key = "***REMOVED***"


class RabbitMQProducer:
    """RabbitMQ生产者（线程安全单例模式）"""
    _instance = None
    _connection = None
    _channel = None
    _lock = threading.Lock()

    def __new__(cls):
        if not cls._instance:
            with cls._lock:
                if not cls._instance:
                    cls._instance = super().__new__(cls)
                    cls._connect()
        return cls._instance

    @classmethod
    def _connect(cls):
        credentials = pika.PlainCredentials('guest', 'guest')
        cls._connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=RABBITMQ_HOST,
                port=RABBITMQ_PORT,
                credentials=credentials
            )
        )
        cls._channel = cls._connection.channel()
        cls._channel.queue_declare(queue=RESPONSE_QUEUE, durable=True)
        cls._channel.queue_declare(queue=RESPONSE_SCORING_QUEUE, durable=True)

    # def publish_message(self, message, routing_key):
    #     try:
    #         self._channel.basic_publish(
    #             exchange='',
    #             routing_key=routing_key,
    #             body=json.dumps(message),
    #             properties=pika.BasicProperties(delivery_mode=2)
    #         )
    #     except pika.exceptions.ConnectionClosed:
    #         self._connect()  # 重连机制
    #         self.publish_message(message, routing_key)


def process_deepseek_request(system_prompt, file):
    """调用DeepSeek API并返回响应"""
    try:
        client = OpenAI(api_key=api_key, base_url=base_url)
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": file},
            ],
            stream=False,
            response_format={
                'type': 'json_object'
            }
        )
        print(f"deepseek replay: {0}", response.choices[0].message.content)
        return response.choices[0].message.content
    except Exception as e:
        return {"deepseek error ": str(e)}


def callback_wrapper(ch, method, properties, body):
    """RabbitMQ消息回调包装器"""
    try:
        request_data = json.loads(body)
        fileId = request_data.get("fileId")
        userId = request_data.get("userId")
        request_enum = request_data.get("requestEnum")

        print("[111]收到请求: ", request_enum, request_data)
        # 提交任务到线程池
        future = executor.submit(process_deepseek_request, request_data.get('systemPrompt'),
                                 request_data.get('userContent'))

        # 添加回调处理
        future.add_done_callback(
            lambda f: handle_response(f.result(), userId, fileId, request_enum)
        )

    except Exception as e:
        print(f"错误 processing message: {e}")
    finally:
        ch.basic_ack(delivery_tag=method.delivery_tag)


# 用于处理deepseek的返回
def handle_response(response, userId, fileId, request_enum):
    print("[129]获取到deepseek返回 over")
    print("deep seek response: ", response)
    response_queue = ""
    messageIdEndWith = ""
    # 判断请求类型
    if request_enum == "GENERATE_QUESTION" or request_enum == 0:
        response_queue = RESPONSE_QUEUE
    elif request_enum == "GENERATE_SCORING" or request_enum == 1:
        response_queue = RESPONSE_SCORING_QUEU
    else:
        response_queue = RESPONSE_SCORING_QUEU
        messageIdEndWith = "exam"
    """增强型回调处理"""
    max_retries = 3
    for attempt in range(max_retries):
        try:
            # 每次获取新通道
            channel0 = ChannelManager().get_channel()
            message_id = userId+":"+fileId+messageIdEndWith
            message = {
                'id': message_id,
                'response': response,
            }

            channel0.basic_publish(
                exchange='',
                routing_key=response_queue,
                body=json.dumps(message),
                properties=pika.BasicProperties(
                    delivery_mode=2,
                    content_type='application/json'
                )
            )
            logging.info(f"消息发送成功 request_id : {message_id}")
            return
        except (pika.exceptions.ChannelClosed,
                pika.exceptions.ConnectionClosed) as e:
            logging.warning(f"通道异常 (尝试 {attempt + 1}/{max_retries}): {str(e)}")
            ChannelManager()._create_connection()  # 强制重置连接
            time.sleep(1)
        except Exception as e:
            logging.error(f"未知错误: {str(e)}")
            break

    # 最终失败处理
    logging.critical(f"无法发送响应: {message_id}")
    with open('failed_messages.log', 'a') as f:
        f.write(json.dumps(message) + '\n')


# def handle_response(response, request_id):
#     """处理响应并发送到RabbitMQ"""
#     producer = RabbitMQProducer()
#     message = {
#         'id': request_id,
#         'response': response
#     }
#     producer.publish_message(message)


if __name__ == "__main__":
    # 初始化线程池（根据需求调整max_workers）
    executor = ThreadPoolExecutor(max_workers=10)

    # 连接RabbitMQ
    credentials = pika.PlainCredentials('guest', 'guest')
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            credentials=credentials
        )
    )
    channel = connection.channel()
    channel.queue_declare(queue=REQUEST_QUEUE, durable=True)
    channel.queue_declare(queue=RESPONSE_QUEUE, durable=True)
    channel.queue_declare(queue=RESPONSE_SCORING_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=20)  # 控制流量

    # 开始消费
    channel.basic_consume(
        queue=REQUEST_QUEUE,
        on_message_callback=callback_wrapper
    )

    print(" [*] Waiting for messages. To exit press CTRL+C")
    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        executor.shutdown()
        connection.close()
