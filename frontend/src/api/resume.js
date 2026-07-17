import request from './request'

export function uploadResume(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/resume/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getResumeList(params) {
  return request.get('/resume/list', { params })
}

export function getResumeDetail(id) {
  return request.get(`/resume/${id}`)
}

export function deleteResume(id) {
  return request.delete(`/resume/${id}`)
}

export function reanalyzeResume(id) {
  return request.post(`/resume/${id}/reanalyze`)
}

export function exportPdf(id) {
  return request.get(`/resume/${id}/export`, {
    responseType: 'blob'
  })
}
