import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

const patientView = p => ({
  ...p,
  name: [p.firstName, p.lastName].filter(Boolean).join(' '),
  canonicalId: p.canonicalPatientId,
  birthDate: p.dateOfBirth,
  sourceIdentities: p.identities || []
})

const timelineView = e => ({
  ...e,
  timestamp: e.time,
  sourceSystem: e.source,
  resourceType: e.type,
  rawJson: e.rawFhirJson
})

const trialView = t => ({
  ...t,
  code: t.trialCode,
  eligibilityText: t.originalEligibilityText,
  criteria: (t.criteria || []).map(c => ({
    ...c,
    type: c.criterionType,
    field: c.clinicalField,
    value: c.value ?? (c.minValue != null && c.maxValue != null
      ? `${c.minValue} – ${c.maxValue}`
      : c.minValue ?? c.maxValue ?? '—')
  }))
})

const matchView = m => ({
  ...m,
  patient: { id: m.patientId, name: m.patientName, canonicalId: m.canonicalPatientId },
  trial: { id: m.trialId, title: m.trialTitle, code: m.trialCode },
  results: (m.criterionResults || []).map(r => ({
    ...r,
    result: r.status,
    criterion: {
      type: r.criterionType,
      field: r.clinicalField,
      operator: r.operator,
      value: r.criterionValue ?? (r.criterionMin != null && r.criterionMax != null
        ? `${r.criterionMin} – ${r.criterionMax} ${r.unit || ''}`.trim()
        : r.criterionMin ?? r.criterionMax ?? '—'),
      description: r.criterionDescription
    },
    evidenceEvent: r.sourceSystem ? {
      id: r.sourceEventId,
      sourceSystem: r.sourceSystem,
      sourceRecordId: r.sourceRecordId,
      resourceType: r.clinicalField === 'condition' ? 'Condition' : 'Observation',
      timestamp: r.eventTimestamp,
      rawJson: r.rawFhirJson
      ,sourceFormat: r.sourceFormat,
      rawSourceText: r.rawSourceText,
      sourceDocumentName: r.sourceDocumentName
    } : null
  }))
})

// Dashboard
export const getDashboard = () => api.get('/dashboard').then(r => ({
  ...r.data,
  stats: {
    totalPatients: r.data.totalPatients,
    clinicalEvents: r.data.totalClinicalEvents,
    clinicalTrials: r.data.totalTrials,
    potentialMatches: r.data.potentialMatches,
    needsReview: r.data.needsReview
  },
  systemStatus: { ingestion: 'ONLINE', identity: 'ACTIVE', aiService: 'ACTIVE', kafka: 'READY' },
  recentEvents: (r.data.recentEvents || []).map(timelineView)
}))
export const getAuditLogs = (page = 0, size = 20) => api.get(`/audit-logs?page=${page}&size=${size}`).then(r => r.data)

// Patients
export const getPatients = (search = '', page = 0, size = 20) =>
  api.get('/patients', { params: { search, page, size } }).then(r => ({ ...r.data, content: r.data.content.map(patientView) }))
export const getPatient = (id) => api.get(`/patients/${id}`).then(r => patientView(r.data))
export const getPatientTimeline = (id) => api.get(`/patients/${id}/timeline`).then(r => r.data.map(timelineView))
export const getPatientMatches = (id) => api.get(`/patients/${id}/matches`).then(r => r.data.map(matchView))

// Trials
export const getTrials = () => api.get('/trials').then(r => r.data.map(trialView))
export const getTrial = (id) => api.get(`/trials/${id}`).then(r => trialView(r.data))
export const createTrial = (data) => api.post('/trials', {
  title: data.title,
  trialCode: data.code,
  description: data.description,
  originalEligibilityText: data.eligibilityText
}).then(r => trialView(r.data))
export const extractCriteria = (trialId, text) =>
  api.post(`/trials/${trialId}/extract-criteria`, { text }).then(r => trialView(r.data))
export const runMatching = (trialId, patientId) =>
  api.post(`/trials/${trialId}/match/${patientId}`).then(r => matchView(r.data))
export const confirmAndScreenCohort = (trialId) =>
  api.post(`/trials/${trialId}/confirm-and-screen`).then(r => r.data.map(matchView))
export const getTrialMatches = (trialId) => api.get(`/trials/${trialId}/matches`).then(r => r.data.map(matchView))
export const getTrialOperations = (trialId) => api.get(`/trials/${trialId}/operations`).then(r => r.data)
export const uploadPdf = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/trials/upload-pdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(r => r.data)
}

// Matches
export const getMatch = (id) => api.get(`/matches/${id}`).then(r => matchView(r.data))
export const resolveReview = (id, notes, decision) =>
  api.post(`/matches/${id}/resolve`, { notes, decision }).then(r => matchView(r.data))

// Review Queue
export const getReviewQueue = () => api.get('/review-queue').then(r => r.data.map(i => ({
  ...i,
  id: i.matchId,
  status: i.currentStatus,
  timestamp: i.matchedAt,
  patient: { name: i.patientName, canonicalId: i.canonicalPatientId },
  trial: { title: i.trialTitle, code: i.trialCode },
  results: [
    ...Array(Number(i.missingCriteria || 0)).fill({ result: 'MISSING' }),
    ...Array(Number(i.failedCriteria || 0)).fill({ result: 'REVIEW_REQUIRED' })
  ]
})))

// FHIR Ingestion
export const ingestFhir = (data) => api.post('/fhir/ingest', data).then(r => r.data)
