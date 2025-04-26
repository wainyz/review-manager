# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/4/1:19:06
@email:Player_simple@163.com
@IDE:PyCharm
============================
定义 一些常量。
    public static final String QUEUE_NAME = "deepseek_requests";
    public static final String EXCHANGE_NAME = "deepseek_exchange";
    public static final String ROUTING_KEY = "deepseek_requests";
"""
receiver_queue_name = "deepseek_requests"
productor_queue_name = "deepseek_response"
productor_exchange_name = "deepseek_response_exchange"
productor_routing_key = "deepseek_response"
productor_binding_key = "deepseek_response"



if __name__ == '__main__':
    # 启动receiver，以receiver为驱动，带动productor运行
    print("hello ")
