// API配置文件

// API基础URL
export const BASE_URL = 'http://localhost:80'

// API路径配置
export const API_PATHS = {
  // 登录相关
  LOGIN: '/public/login',
  GET_CAPTCHA: '/public/image',
  REFRESH_CAPTCHA: '/public/image/flash',
  
  // 注册相关
  REGISTER: '/public/register',
  SEND_EMAIL_CODE: '/public/register/send/email',

  //查询用户文档数量
  DocumentCount: '/file_info/file_count',
  //查询用户文档列表
  TitleList: '/file_info/docs',

  //提交文档
  SubmitDocument: '/deepseek/sendFile',
  //生成题目
  GenerateQuestions: '/deepseek/generateQuestion',

  // 获取文档相关信息
  GET_ORIGINAL_FILE: '/file_info/original_file',
  GET_HISTORY_FILE: '/file_info/file_history',
  GET_ANALYSIS_FILE: '/file_info/controller_file',
  GET_CURRENT_QUESTION: '/file_info/current_question',
  // 提交答案
  SUBMIT_ANSWERS: '/scoring/answers',
  // 获取WebSocket URL
  WebScoketUrl : '/spider/websocket-endpoint',
  // 获取平均等待时间
  AvarageWaitTime : '/file_info/averageWaitTime',
  // 获取当前请求状态
  RequestStatus : '/file_info/requestStatus',
}

// 获取完整的API URL
export const getFullUrl = (path) => `${BASE_URL}${path}`