import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import Login from '../Login.vue'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  },
  ElLoading: {
    service: vi.fn().mockReturnValue({
      close: vi.fn()
    })
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/home',
      component: { template: '<div>Home</div>' }
    }
  ]
})

describe('Login.vue', () => {
  let wrapper

  beforeEach(() => {
    wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })
    vi.clearAllMocks()
  })

  it('validates email format', async () => {
    const emailInput = wrapper.find('input[placeholder="请输入邮箱"]')
    await emailInput.setValue('invalid-email')
    await emailInput.trigger('blur')
    
    expect(wrapper.text()).toContain('请输入正确的邮箱格式')
  })

  it('validates password length', async () => {
    const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
    await passwordInput.setValue('12345')
    await passwordInput.trigger('blur')
    
    expect(wrapper.text()).toContain('密码长度不能小于6位')
  })

  it('validates captcha length', async () => {
    const captchaInput = wrapper.find('input[placeholder="请输入验证码"]')
    await captchaInput.setValue('123')
    await captchaInput.trigger('blur')
    
    expect(wrapper.text()).toContain('验证码长度必须为4位')
  })

  it('refreshes captcha when clicking on the image', async () => {
    const captchaImage = wrapper.find('.captcha-image')
    const oldSrc = captchaImage.attributes('src')
    await captchaImage.trigger('click')
    const newSrc = captchaImage.attributes('src')
    
    expect(newSrc).not.toBe(oldSrc)
  })

  it('handles successful login', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({
        token: 'test-token',
        user: { id: 1, email: 'test@example.com' }
      })
    })

    const emailInput = wrapper.find('input[placeholder="请输入邮箱"]')
    const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
    const captchaInput = wrapper.find('input[placeholder="请输入验证码"]')
    
    await emailInput.setValue('test@example.com')
    await passwordInput.setValue('password123')
    await captchaInput.setValue('1234')
    
    const loginButton = wrapper.find('.login-button')
    await loginButton.trigger('click')

    expect(ElLoading.service).toHaveBeenCalled()
    expect(global.fetch).toHaveBeenCalledWith('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'password123',
        captcha: '1234'
      })
    })
    expect(ElMessage.success).toHaveBeenCalledWith('登录成功')
  })

  it('handles login failure', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({ message: '登录失败' })
    })

    const emailInput = wrapper.find('input[placeholder="请输入邮箱"]')
    const passwordInput = wrapper.find('input[placeholder="请输入密码"]')
    const captchaInput = wrapper.find('input[placeholder="请输入验证码"]')
    
    await emailInput.setValue('test@example.com')
    await passwordInput.setValue('password123')
    await captchaInput.setValue('1234')
    
    const loginButton = wrapper.find('.login-button')
    await loginButton.trigger('click')

    expect(ElMessage.error).toHaveBeenCalledWith('登录失败')
  })
})