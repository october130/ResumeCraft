import request from './request'

// 获取知识库文档列表
export function getKnowledgeBaseList() {
  return request.get('/knowledge-base/list')
}

// 获取单个文档详情
export function getKnowledgeDoc(docId) {
  return request.get(`/knowledge-base/doc/${docId}`)
}

// 语义搜索知识库
export function searchKnowledgeBase(params) {
  return request.get('/knowledge-base/search', { params })
}
