定义前后端交互的api格式。

安全校验API：

  公开路径和静态路径: /public/** , 

用户管理系统API：

  验证码API，分为两个，1个用于获取新的验证码id，一个用于通过id获取验证

    refresh_image url:

    get_image url:

  注册API，url：

  登录API，url：

  修改信息API，包括修改头像，修改密码，修改邮箱等，url：

智能出题系统API：

  生成文档测验API ，

  生成试卷API，post /paper/generate 携带json参数，包括，直接传递给deepseek。

        return Maps.of("avergeWaitTime", String.valueOf(RabbitMQConsumer.getAverageWaitingTime())).and("noticeId", String.valueOf(notice.getId()))

```json
paperConfig:
{
    "name": "模拟试卷",
    "difficulty": 3,
    "content": "123",
    "scope": "123",
    "questionCounts": {
        "choice": 10,
        "fillBlank": 5,
        "solution": 5
    },
    "questionPoints": {
        "choice": 4,
        "fillBlank": 8,
        "solution": 4
    },
    "promptWords": "123",
    "referenceFile": null
}
```

  获取所有试卷API，get /paper/list 返回自己的所有试卷

  搜索试卷API, post /paper/search/{key} 搜索所有能搜索的试卷

  修改试卷API，post /paper/modify/{id} 覆盖试卷json

  删除试卷API，delete /paper/delete/{id} 删除试卷  

自动打分系统API：

  自动打分API，

  修改评分API，

考试API：
  创建考试API，post /exam/generate 携带json,包括考试主题、考试描述、试卷ID、考试人员、考试时间、考试批改人员 。 

        返回考试的id

  修改考试API，post /exam/modify/{id} 携带json,包括考试主题、考试描述、试卷ID、考试人员、考试时间、考试批改人员 。

  在线考试API，post /exam/begin/{id} 携带json，包括考试主题、考试描述、试卷ID、考试人员、考试时间、考试批改人员（空） 。

  提交考试API, post /exam/end/{id}，携带json，包括考试id，用户答题记录

  手动通过API，post /exam/scoring/pass, 携带json，包括考试id，学生id，通过的批改结果json

  重新批改API, post /exam/scoring/modify/{id}, 携带json，包括考试id，学生id，新的批改结果json

  查看批改结果API, get /exam/scoring/show/{id} 

      返回本人的打分结果

  查看考试批改结果API, get /exam/scoring/list/{id}

      返回本次考试的所有人的考试结果

班级API：

  创建班级API，post /class/create 携带param，班级名称

      返回 班级id

  删除班级API，delete /class/delete/{id}   

  搜索班级API，get /class/search/{key} 

      返回 班级列表json，元素中的属性分别是班级id和班级名称

  申请加入API, post /class/apply/{id}  携带descript string

  退出班级API，get /class/out/{id} 

  管理班级API：

    ~~用户权限修改API，post /class/permission/modify 携带json~~

    获取所有申请API, get /class/permission/list/apply 携带json，包括班级id，申请id

    同意、拒绝加入班级API，post /class/permission/reduce/apply 携带json，包括班级id，申请id，同意或者拒绝

    踢出班级API，post /class/permission/          携带json，包括班级id，踢出用户的id

    发布考试API，post /class/permission/publish/exam 携带json，班级id，考试id，

复习管理系统API：

  上传文档API，

  修改文档API，

  搜索文档API，

  修改掌握度控制文件API，

  删除历史记录API，

  生成测验题目API，

  在线测验API，

信息通知系统API，

  websocketAPI:

    NOTICE_URI="/notice";  
    WEBSOCKET_URI="/websocket";  
    MESSAGE_URI="/message";

  订阅全局通知API，/all/notice

  订阅个人通知API，/user/notice

  发送全用户通知API, post /notice/all ,携带body noticeContent

  发送指定用户的通知API, post  /notice/user/{userId} ，携带body noticeContent

  获取自己的全部的通知API，get /notice/list

      返回 json

```json
{
    "code": 200,
    "message": null,
    "data": [
        {
            "id": 1923750622996643841,
            "userid": 1,
            "timestamp": 1747492865000,
            "content": "考试批改中...",
            "type": 3
        }
    ]
}
```

  获取指定通知（会进行权限校验)API, /notice/get/{noticeId} 

    返回如下

```json
{
    "code": 200,
    "message": null,
    "data": {
        "id": 1923750622996643841,
        "userid": 1,
        "timestamp": 1747492865000,
        "content": "考试批改中...",
        "type": 3
    }
}
```

  获取自己所有未读消息API, get /notice/unread/list 

```json
{
    "code": 200,
    "message": null,
    "data": [
        {
            "id": 1923750622996643841,
            "userid": 1,
            "timestamp": 1747492865000,
            "content": "考试批改中...",
            "type": 3
        }
    ]
}
```

  更新自己的最近阅读通知时间API, post /notice/update/last_read_time 携带timestamp参数

    返回 returnModel状态码和消息有点用

  获取自己最近阅读通知时间API, get /notice/update/last_read_time

  发送好友申请通知API, get /friends/apply/add/{friendId}

    实际上存储的notice content格式是 applyUserId=xxx,applyGoalUserId=xxx,

  同意好友请求API, get /friends/apply/agree/{friendId}

    实际上存储的notice content格式也是 applyUserId=xxx,applyGoalUserId=xxx,

  拒绝好友请求API, get /friends/apply/reject/{friendId}

    在notice的content中存储 格式applyUserId=xxx,applyGoalUserId=xxx,

  删除好友通知API，get /friends/remove/{friendId}

  获取平均等待时间，get /notice/waiting/average_time

  获取等待通知预计等待时间API, get /notice/waiting/maybe_waiting_time/{noticeId}

      return data中给出预计等待的秒数。

  聊天 好友之间API, post /message/talk 携带param： friendId， content

  STOMP聊天消息API, 通过发送notice对象格式，但是notice的type是61 notice的userId是senderId

  STOMP群聊消息API，通过发送notice对象格式，但是notice的type是62 notice的content

  搜索用户API，get /search/user/{key} 返回所有用户的所有信息，这里先不管信息泄露的问题。

  获取自己的好友列表, get /friends/list 