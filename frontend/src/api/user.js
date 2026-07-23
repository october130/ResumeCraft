import request from './request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function sendSms(phone) {
  return request.post('/user/sms/send', null, { params: { phone } })
}
