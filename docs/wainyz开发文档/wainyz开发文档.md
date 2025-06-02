# 画出手脚

本次开发需要的功能如下：

## 班级系列功能

### 1 创建班级

1. 测试1: 使用userId=1的账户，创建班级 高三17班，描述为：2021届高三，高三十七班。学生有wzy，wainyz两人，老师为wainyz

### 2 修改班级元信息

1. 测试1: 使用userId=1用户，修改班级 高三17班，修改描述为 2018级高三十七班。

### 3 申请加入班级

1. 测试1: 使用用户fuvhzc用户，申请加入 高三17班 ，申请理由为 测试班级申请功能。

### 4 同意或者拒绝加入班级

1. 测试1: 使用用户fuvhzc用户，申请加入 高三17班 ，申请理由为 测试班级申请功能。使用wainyz用户同意申请。

### 5 退出、踢出班级

1. 测试1: 使用wainyz用户，踢出用户fuvhzc用户。

2. 测试2: wzy自行退出班级，后续需要重新加入进行方便后续测试。

### 6 发送班级聊天

1. 测试1: wainyz和wzy两个用户的聊天。

### 7 发布班级考试

可以将考试发布在班级中，此时所有学生会收到通知，另外在班级聊天中也会出现发布考试的老师的自动消息。

1. 测试：发布考试后观察在班级中是否出现提示。

### 8 获取所有自己当前加入的班级

### 9 搜索所有公开的班级

### 10 获取班级的全部信息

### 11 获取班级的元信息

## 考试系列功能

### 1 创建考试

1. 测试1: 用户wainyz创建考试。

2. 修改考试元信息（包括人员调整）
   
   1. 测试1：用户wainyz修改考试的信息。

3. 发布考试
   
   1. 测试1: 用户wainyz发布考试

4. 在线考试：用户点击参加考试即可。

5. 审批考试结果

6. 生成考试总结

# 连接手脚

班级系统功能与其他功能的联系：

  1. 与notice功能的联系：（1）申请与同意功能（2）聊天功能（3）班级考试功能需要通知（4）踢出班级之后需要通知

  2. 与考试系统的联系：（1）发布考试的时候可以选择发布到班级。（2）在班级中可以查看历史考试和正在进行的考试

考试系统功能与其他功能的联系：

  1. 与生成试卷的联系：（1）需要遍历自己的所有的试卷

  2. 与打分复习管理系统的联系：（1）都需要打分，可以重用这个。

# 描绘肌肉

> 班级系统功能

- 创建班级
  
    前端需要一个对象收集班级所需的信息，然后需要一个对象负责发送和处理请求到后端。
    后端需要一个对象处理并返回请求，还需要一个对象去实际实现，创建班级对象并更新到数据库中。

- 修改班级元信息
  
    前端需要对象一个展示班级信息，并可以接受新的信息的界面，还需要一个对象负责发送和处理更新请求。
    后端需要一个对象处理并返回请求，还需要一个对象实际实现更改班级的逻辑到数据库中。

- 申请加入班级
  
  前端需要一个对象去搜索和展示班级，还需要一个对象负责发送和处理申请请求。
  后端需要一个对象处理并返回请求，还需要一个对象实际负责处理添加请求

- 同意或者拒绝加入班级
  
  前端需要一个对象去展示请求通知（notice系统的联系），还需要一个前端对象负责发送和处理同意或者拒绝的请求。
  后端需要一个对象处理并返回请求，还需要一个对象实际处理同意或者拒绝加入班级的逻辑，并且删除notice。

- 退出、踢出班级
  
  前端需要一个对象展示班级所有的成员并可以搜索，还需要一个对象负责发送和处理退出和踢出班级的轻轻。
  后端需要一个对象处理并返回对象，还需要一个对象实际处理退出班级的逻辑，并生成notice。

- 发送班级聊天
  
  前端需要一个对象去展示班级聊天，还需要一个对象负责发送和处理聊天消息。（可以复用好友聊天系统）
  后端需要一个对象去接收聊天消息，还需要一个对象实际处理聊天逻辑(利用notice系统发送消息）。

- 发布班级考试：可以将考试发布在班级中，此时所有学生会收到通知，另外在班级聊天中也会出现发布考试的老师的自动消息（notice系统和考试系统）。

- 搜索所有公开的班级：直接通过sql搜索即可。

- 获取自己加入的所有班级：直接通过sql搜索即可。

- 获取班级的元信息，直接通过sql查询即可。

# 描绘关节

## 班级系统功能

### 1 创建班级

关节1：发送和处理请求的对象以及后端处理和发送请求的对象。

前端要求

POST /class/create

Header: Authorization {token值}

Body multipart/form-data

  className

  description

  isPublic int值

后端要求

@RequestParam 

  className

  description

  isPublic int值

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 2 修改班级元信息

关节1: 前后端交互。

前端要求

POST /class/update

Header: Authorization {token值}

Body multipart/form-data

  classId

  className

  description

  isPublic boolean值

  studentList 字符串使用逗号分隔

  teacherList 字符串使用逗号分隔

后端要求

@RequestParam

  classId

  className

  description

  isPublic boolean值

  studentList 字符串使用逗号分隔

  teacherList 字符串使用逗号分隔

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 3 申请加入班级

关节1: 前后端交互。

前端要求

POST /class/apply/add

Header: Authorization {token值}

Body multipart/form-data

  classId

  description

后端要求

@RequestParam

  classId 

  description

  param String类型额外的参数，可以为空，默认传0

@PathVarible("classId")

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 4 同意或者拒绝加入班级

关节1: 前后端交互。

前端要求

POST /class/apply/reback

Header: Authorization {token值}

Body multipart/form-data

  noticeId

  reback 0表示拒绝，1表示同意

后端要求

@RequestParam

  reback 0表示拒绝，1表示同意

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 5 退出班级或者拒绝加入班级

关节1: 前后端交互。

前端要求

POST /class/kick_out

Header: Authorization {token值}

Body multipart/form-data

  userId

  classId

后端要求

@RequestParam

  userId

  classId

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 6 发送班级聊天

关节1: 前后端交互。

前端要求：

发送时：

POST /class/message

Header: Authorization {token值}

Body multipart/form-data

  classId

  content 

接收时：

订阅频道/class/{classId}/message

判断 type=30 ，从content中分离出classId，className，sender用户名，消息内容。

后端要求

接收时：

@RequestParam

  classId

  content

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

发送时：

notice的type=30,content中包含classId，className，用户名，消息内容

注意这里notice的userId不再是通知的对象，而是消息发送的对象，通知的对象是全体classId订阅者

### 7 发布班级考试

关节1: 前后端交互。

前端要求：

POST /class/subscript

Header: Authorization {token值}

Body multipart/form-data

  classId

  examId

后端要求

@RequestParam

  classId

  examId

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 8 获取当前自己加入的所有班级

关节1: 前后端交互。

前端要求：

get /class/my_classes

Header: Authorization {token值}

后端要求

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

返回的json数组，内部元素的格式是"classId:className"使用分号分隔。

### 9 搜索所有公开的班级

关节1: 前后端交互。

前端要求：

post /class/search

Header: Authorization {token值}

Body multipart/form-data

  key

后端要求

@RequestParam

  key

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

### 10 获取班级全部消息

关节1: 前后端交互。

前端要求：

post /class/message/list

Header: Authorization {token值}

Body multipart/form-data

  classId

后端要求

@RequestParam

  classId

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

返回数据：

```json
[
        {
            "id": 1929174118423982080,
            "class_id": 1,
            "endtime": 1748785928000,
            "content": "[\"1:消息测试2\"]",
            "starttime": 1748785893000
        },
        {
            "id": 1929180924734959616,
            "class_id": 1,
            "endtime": 1748787549562,
            "content": "[\"1:消息测试23\",\"1消息测试2345\"]",
            "starttime": 1748787543379
        }
    ]
```

### 10 获取班级的元信息

关节1: 前后端交互。

前端要求：

post /class/info

Header: Authorization {token值}

Body multipart/form-data

  classId

后端要求

@RequestParam

  classId

  param String类型额外的参数，可以为空，默认传0

@RequestAttribute("userId") 这个是gateway网关系统配合下会得到的参数

返回数据：

```json
{
    "code": 200,
    "message": null,
    "data": {
        "id": "1",
        "owner": 1,
        "teacher_list": "[]",
        "class_name": "大数据处理-2",
        "description": "2021级西华大学，计算机科学与技术，大数据处理专业",
        "exam_list": null,
        "student_list": "[1, 3, 1, 1]",
        "is_public": 1
    }
}
```

# 联通经脉

## 班级系统功能