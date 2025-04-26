from openai import OpenAI

DeepSeekAPI = '***REMOVED***'

# 用于测试是否能正常连接到deepseek
if __name__ == '__main__':
    client = OpenAI(api_key=DeepSeekAPI, base_url="https://api.deepseek.com")

    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {"role": "system", "content": "You are a helpful assistant"},
            {"role": "user", "content": "Hello"},
        ],
        stream=False
    )

    print(response)
