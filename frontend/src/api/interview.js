import request from './request'

export function createSession(data) {
  return request.post('/interview/sessions', data)
}

export function submitAnswer(sessionId, data) {
  return request.post(`/interview/${sessionId}/answers`, data)
}

export function getInterviewList() {
  return request.get('/interview/sessions')
}

export function getInterviewDetail(sessionId) {
  return request.get(`/interview/sessions/${sessionId}/details`)
}

export function deleteSession(sessionId) {
  return request.delete(`/interview/sessions/${sessionId}`)
}
