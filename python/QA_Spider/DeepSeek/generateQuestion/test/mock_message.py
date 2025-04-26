# -*- coding:utf-8 _*-
"""
============================
@author:笨磁
@time:2025/4/3:19:29
@email:Player_simple@163.com
@IDE:PyCharm
============================
发送消息到mq中
"""
import json
import random

from DeepSeek.generateQuestion.recevier import RESPONSE_SCORING_QUEUE, RESPONSE_QUEUE

body1 = """
{
    "coverage": 0.3,
    "all_mastery": 0.5,
    "mastery_records": [
      {
        "raw": "我们最常用的就是食指,以及下一行移动,所以j就是下一行,以及<enter>也是下一行的意思",
        "mastery": 0.9,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "左右就很好记了,最左的左,最右的l是右,空格也就是确定的意思,从左到右的习惯里面,确认然后就是右移的意思",
        "mastery": 0.2,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--插入命令 a向后插入 i向前插入 o向下插入新行 这些常常跟随在 shift后面表示加强 例如 shift a 在行尾插入 shift i在行首插入 shift o 在上一行插入新行",
        "mastery": 0.7,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--w和b以及e和ge以及shift强化为字符串 w和b都是常用的,也就是移动到单词的开头位置,如果用shift强化的话就是移动到字符串的开头,字符串之间需要用空格隔开",
        "mastery": 0.7,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--gg和shift g 移动到文档的开头和文档的结尾",
        "mastery": 0.2,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--移动在行最前面或最后面的字符 0 和 $ 命令",
        "mastery": 0.9,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--f <shift-f> t <shitf-t>命令,表示在一行中find一个字符,f是常用的表示向右查找,shift f表示向左查找 ,t表示在find的字符串的前一个字符",
        "mastery": 0.2,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "在:命令模式下,w表示write是保存的意思,q表示quit会退出文件,q!表示强制退出",
        "mastery": 0.9,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--删除光标选中的字符 x 一般的用法就是跟随在数字后面 3x 表示重复三次删除命令 他的进阶版就是 c 也是删除,不过后面可以跟随其他命令例如 c3w 意思就是删除后面的三个单词",
        "mastery": 0.2,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      },
      {
        "raw": "--u撤销以及<ctrl-r>反撤销,大写U指的是整行撤销修改",
        "mastery": 0.9,
        "attenuation_level": 1,
        "first_time": "2023-04-01"
      }
    ]
  },
  "result": {
    "score": 5,
    "questions": [
      {
        "question": "在vim的normal模式下，哪个键用于将光标移动到下一行？",
        "correct": "j",
        "answer_result": true
      },
      {
        "question": "在vim的normal模式下，哪个键用于将光标向右移动？",
        "correct": "l",
        "answer_result": false
      },
      {
        "question": "在vim中，哪个命令用于在行尾插入文本？",
        "correct": "shift a",
        "answer_result": false
      },
      {
        "question": "在vim中，哪个命令用于移动到当前单词的开头？",
        "correct": "w",
        "answer_result": false
      },
      {
        "question": "在vim中，哪个命令用于移动到文档的开头？",
        "correct": "gg",
        "answer_result": false
      },
      {
        "question": "在vim中，哪个命令用于移动到当前行的行首？",
        "correct": "0",
        "answer_result": true
      },
      {
        "question": "在vim中，哪个命令用于在当前行向右查找一个字符？",
        "correct": "f",
        "answer_result": false
      },
      {
        "question": "在vim的命令模式下，哪个命令用于保存文件？",
        "correct": "w",
        "answer_result": true
      },
      {
        "question": "在vim中，哪个命令用于删除光标下的字符？",
        "correct": "x",
        "answer_result": false
      },
      {
        "question": "在vim中，哪个命令用于撤销上一次的修改？",
        "correct": "u",
        "answer_result": true
      }
    ]
  }
}
"""
body2 = """
 {
  "questionNum": 5,
  "questions": [
    {
      "raw": "--d 以及 c 删除命令 常用的是d2w 或者,不同的是c在删除之后会直接进驻insert模式,也就是说c不是一个原子命令,而是一个组合命令,就像d+对象+a一样的操作,如果想要删除并且进入插入模式的话,;使用c命令将会更加简洁",
      "question": "在vim中，d和c命令的主要区别是什么？",
      "correct": "d命令仅删除内容，而c命令在删除内容后会进入插入模式。",
      "recordIndex": -1
    },
    {
      "raw": "--hjkl 以及在normal模式下的<enter><back><空格>",
      "question": "在vim的normal模式下，哪个键用于向下移动一行？",
      "correct": "j",
      "recordIndex": 1
    },
    {
      "raw": "--插入命令 a向后插入 i向前插入 o向下插入新行 这些常常跟随在 shift后面表示加强 例如 shift a 在行尾插入 shift i在行首插入 shift o 在上一行插入新行",
      "question": "在vim中，Shift + o命令的作用是什么？",
      "correct": "在上一行插入新行",
      "recordIndex": 4
    },
    {
      "raw": "--^和g_表示移动到行首的非空白字符和行尾的非空白字符,其实可以用0w 和$b代替,不过看情况吧,哪种好用用那种",
      "question": "在vim中，哪个命令可以移动到行首的非空白字符？",
      "correct": "^",
      "recordIndex": 8
    },
    {
      "raw": "--f <shift-f> t <shitf-t>命令,表示在一行中find一个字符,f是常用的表示向右查找,shift f表示向左查找 ,t表示在find的字符串的前一个字符,注意没有后一个字符的命令 注意在使用了f或者t等命令之后可以使用;表示继续向下查找使用,号表示反方向查找,也就是说可以通过f一个字符之后用,代替使用shift f",
      "question": "在vim中，f和Shift + f命令的区别是什么？",
      "correct": "f命令向右查找字符，Shift + f命令向左查找字符。",
      "recordIndex": 9
    }
  ]
}
"""
scoring_response = r"""{"scores":[0,0,100,0,100,0,100,100],"advice":"1. 在Vim中，移动到前一个单词的结尾的正确命令是'ge'，而不是'shift b'。\n2. 在Vim中，'j'键用于移动到下一行，但题目问的是不能用于移动到下一行的键或组合键，正确答案是'<back>'。\n3. 删除整行的命令'dd'回答正确。\n4. 撤销整行修改的命令是'U'，而不是'ctrl r'。\n5. 如果粘贴的内容是一个整行，内容会被放在下一行，回答正确。\n6. 移动到前一个字符串的结尾的正确命令是'gE'，而不是'shift b'。\n7. 'j'键用于移动到下一行，回答正确。\n8. 删除整行的命令'dd'回答正确。","similar":"1. Vim中移动命令：'w'移动到下一个单词的开头，'b'移动到前一个单词的开头，'e'移动到当前单词的结尾，'ge'移动到前一个单词的结尾。\n2. Vim中移动行的命令：'j'下一行，'k'上一行，'0'行首，'$'行尾。\n3. Vim中删除命令：'dd'删除整行，'dw'删除到下一个单词的开头，'de'删除到当前单词的结尾。\n4. Vim中撤销和重做命令：'u'撤销上一次操作，'U'撤销整行的修改，'ctrl r'重做。\n5. Vim中粘贴命令：'p'在光标后粘贴，'P'在光标前粘贴，整行粘贴时会放在当前行的下一行或上一行。"}"""
message = {
    'id': "1:1908132350760460288",
    'response': scoring_response
}
import pika


def mock_scoring_response():
    mock_message(RESPONSE_SCORING_QUEUE)


def mock_generate_response():
    mock_message(RESPONSE_QUEUE)


def mock_message(queueName):
    # 连接到RabbitMQ服务器（默认使用本地）
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    # 声明一个队列（如果不存在则创建）
    # channel.queue_declare(queue='deepseek_scoring_response')

    # 发送消息到队列
    channel.basic_publish(
        exchange='',  # 使用默认交换器
        routing_key=queueName,  # 队列名称
        body= json.dumps(message).encode("utf-8"),
        properties=pika.BasicProperties(
            delivery_mode=2,
            content_type='application/json'
        )
    )

    print("消息已发送")
    connection.close()


if __name__ == '__main__':
    mock_scoring_response()
