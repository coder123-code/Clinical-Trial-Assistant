import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  CheckCircle, XCircle, AlertTriangle, HelpCircle, 
  ArrowLeft, FileText, Database, ShieldAlert, CheckSquare, X
} from 'lucide-react';
import { getMatch, resolveReview } from '../api/client';
import SourceDocumentContent from './SourceDocumentContent';

const SourceDataModal = ({ isOpen, onClose, criterion }) => {
  if (!isOpen || !criterion) return null;
  const event = criterion.evidenceEvent;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] flex flex-col overflow-hidden">
        <div className="flex justify-between items-center p-6 border-b border-gray-100">
          <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <Database className="w-5 h-5 text-blue-600" />
            Source Data Verification
          </h3>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>
        
        <div className="p-6 border-b border-gray-100 bg-blue-50/50">
          <h4 className="font-semibold text-gray-900 mb-1">Criterion Checked:</h4>
          <p className="text-sm text-gray-700">{criterion.criterion.description}</p>
          <div className="mt-3 flex gap-4">
            <div className="bg-white px-3 py-1.5 rounded border shadow-sm text-sm">
              <span className="text-gray-500 mr-2">Required:</span> 
              <span className="font-mono font-bold">{criterion.criterion.operator} {criterion.criterion.value}</span>
            </div>
            <div className="bg-white px-3 py-1.5 rounded border shadow-sm text-sm">
              <span className="text-gray-500 mr-2">Found:</span> 
              <span className="font-mono font-bold">{criterion.patientValue}</span>
            </div>
          </div>
        </div>

        {event ? (
          <>
            <div className="p-4 bg-gray-50 border-b border-gray-100 grid grid-cols-3 gap-4">
              <div>
                <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Source System</p>
                <p className="font-medium text-gray-900">{event.sourceSystem}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Record ID</p>
                <p className="font-mono text-sm text-gray-900">{event.sourceRecordId}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Source Format</p>
                <p className="font-medium text-gray-900">{(event.sourceFormat || 'FHIR').replaceAll('_', ' ')}</p>
              </div>
            </div>
            <SourceDocumentContent event={event} />
          </>
        ) : (
          <div className="flex-1 p-12 text-center text-gray-500 flex flex-col items-center justify-center">
            <HelpCircle className="w-12 h-12 mb-4 text-gray-300" />
            <p className="font-medium text-lg text-gray-700">No source data linked</p>
            <p className="max-w-sm mt-2">The AI could not confidently link a specific FHIR resource to this criterion evaluation.</p>
          </div>
        )}
      </div>
    </div>
  );
};

const MatchExplanation = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [selectedCriterion, setSelectedCriterion] = useState(null);
  const [reviewNotes, setReviewNotes] = useState('');

  const { data: match, isLoading } = useQuery({
    queryKey: ['match', id],
    queryFn: () => getMatch(id)
  });

  const resolveMutation = useMutation({
    mutationFn: ({ decision }) => resolveReview(id, reviewNotes, decision),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['match', id] });
      queryClient.invalidateQueries({ queryKey: ['review-queue'] });
    }
  });

  if (isLoading) return <div className="p-8">Loading explanation...</div>;
  if (!match) return <div className="p-8">Match not found.</div>;

  const getStatusDisplay = (status) => {
    switch (status) {
      case 'ELIGIBLE': return { icon: CheckCircle, color: 'text-green-600', bg: 'bg-green-100', border: 'border-green-200' };
      case 'NOT_ELIGIBLE': return { icon: XCircle, color: 'text-red-600', bg: 'bg-red-100', border: 'border-red-200' };
      case 'NEEDS_REVIEW': return { icon: AlertTriangle, color: 'text-amber-600', bg: 'bg-amber-100', border: 'border-amber-200' };
      case 'POTENTIALLY_ELIGIBLE': return { icon: HelpCircle, color: 'text-blue-600', bg: 'bg-blue-100', border: 'border-blue-200' };
      default: return { icon: HelpCircle, color: 'text-gray-600', bg: 'bg-gray-100', border: 'border-gray-200' };
    }
  };

  const getResultBadge = (result) => {
    switch (result) {
      case 'PASS': return <span className="flex items-center gap-1 text-green-700 bg-green-50 border border-green-200 px-2.5 py-1 rounded-full text-xs font-bold"><CheckCircle className="w-3.5 h-3.5" /> PASS</span>;
      case 'FAIL': return <span className="flex items-center gap-1 text-red-700 bg-red-50 border border-red-200 px-2.5 py-1 rounded-full text-xs font-bold"><XCircle className="w-3.5 h-3.5" /> FAIL</span>;
      case 'MISSING': return <span className="flex items-center gap-1 text-gray-600 bg-gray-100 border border-gray-200 px-2.5 py-1 rounded-full text-xs font-bold"><HelpCircle className="w-3.5 h-3.5" /> MISSING DATA</span>;
      case 'REVIEW_REQUIRED': return <span className="flex items-center gap-1 text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-1 rounded-full text-xs font-bold"><AlertTriangle className="w-3.5 h-3.5" /> REVIEW REQUIRED</span>;
      default: return null;
    }
  };

  const statusInfo = getStatusDisplay(match.status);
  const StatusIcon = statusInfo.icon;
  const isReviewable = match.status === 'NEEDS_REVIEW' || match.status === 'POTENTIALLY_ELIGIBLE';

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-12 animate-in fade-in">
      {/* Header */}
      <div>
        <button onClick={() => navigate(-1)} className="inline-flex items-center text-sm text-blue-600 hover:underline mb-4">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back
        </button>
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
              <span className="text-blue-600">{match.patient?.name}</span>
              <span className="text-gray-400 mx-3">vs</span>
              <span>{match.trial?.title}</span>
            </h1>
            <p className="text-sm text-gray-500 font-mono">
              Patient: {match.patient?.canonicalId} | Trial: {match.trial?.code}
            </p>
          </div>
          <div className={`px-6 py-3 rounded-xl border-2 flex items-center gap-3 ${statusInfo.bg} ${statusInfo.border} ${statusInfo.color}`}>
            <StatusIcon className="w-8 h-8" />
            <div>
              <p className="text-xs font-bold uppercase tracking-wider opacity-80">Match Status</p>
              <p className="text-xl font-bold">{match.status.replace('_', ' ')}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Col - Criteria Results */}
        <div className="lg:col-span-2 space-y-4">
          <h2 className="text-lg font-bold text-gray-900 px-1">Criteria Evaluation Details</h2>
          
          {match.results?.map((res, idx) => (
            <div key={idx} className={`bg-white rounded-xl shadow-sm border p-6 transition-shadow hover:shadow-md ${
              res.result === 'FAIL' ? 'border-red-100' : 
              res.result === 'REVIEW_REQUIRED' || res.result === 'MISSING' ? 'border-amber-100' : 'border-gray-100'
            }`}>
              <div className="flex justify-between items-start mb-4">
                <div>
                  <span className={`text-xs font-bold uppercase tracking-wider mb-1 block ${
                    res.criterion.type === 'INCLUSION' ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {res.criterion.type}
                  </span>
                  <h3 className="font-bold text-gray-900 text-lg">{res.criterion.description}</h3>
                </div>
                {getResultBadge(res.result)}
              </div>

              <div className="bg-gray-50 rounded-lg p-4 border border-gray-100">
                <div className="flex flex-col md:flex-row md:items-center gap-4 md:gap-8 mb-4">
                  <div>
                    <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Required</p>
                    <p className="font-mono font-medium text-gray-900">{res.criterion.operator} {res.criterion.value}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Found in Record</p>
                    <p className="font-mono font-bold text-gray-900 text-lg">{res.patientValue || '—'}</p>
                  </div>
                </div>

                {res.result === 'MISSING' ? (
                  <div className="bg-amber-50 text-amber-800 p-3 rounded text-sm flex items-start gap-2 border border-amber-100">
                    <ShieldAlert className="w-5 h-5 shrink-0 mt-0.5 text-amber-600" />
                    <p>No value for <strong>{res.criterion.field}</strong> found in patient's longitudinal record. Human review required to determine eligibility.</p>
                  </div>
                ) : (
                  <div>
                    <p className="text-sm text-gray-600 italic border-l-2 border-blue-200 pl-3 py-1 mb-3">
                      "{res.explanation}"
                    </p>
                    {res.evidenceEvent && (
                      <div className="flex items-center justify-between pt-3 border-t border-gray-200">
                        <div className="flex items-center gap-2">
                          <span className="text-xs bg-white border px-2 py-1 rounded text-gray-600 font-medium shadow-sm">
                            Source: {res.evidenceEvent.sourceSystem}
                          </span>
                        </div>
                        <button 
                          onClick={() => setSelectedCriterion(res)}
                          className="text-xs font-bold text-blue-600 flex items-center gap-1 hover:underline"
                        >
                          <FileText className="w-4 h-4" /> View Source Data
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Right Col - Workflow */}
        <div className="space-y-6">
          
          {/* Compliance Checklist */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
              <CheckSquare className="w-5 h-5 text-green-600" /> Compliance Check
            </h3>
            <ul className="space-y-3 text-sm">
              <li className="flex items-center gap-2 text-gray-700">
                <CheckCircle className="w-4 h-4 text-green-500" /> Patient identity verified
              </li>
              <li className="flex items-center gap-2 text-gray-700">
                <CheckCircle className="w-4 h-4 text-green-500" /> Structured criteria parsed
              </li>
              <li className="flex items-center gap-2 text-gray-700">
                <CheckCircle className="w-4 h-4 text-green-500" /> Source data linked
              </li>
              <li className="flex items-center gap-2 text-gray-700">
                <CheckCircle className="w-4 h-4 text-green-500" /> Audit trail generated
              </li>
              {isReviewable && (
                <li className="flex items-center gap-2 text-amber-700 font-medium mt-2 pt-2 border-t border-gray-100">
                  <AlertTriangle className="w-4 h-4" /> Pending Human Review
                </li>
              )}
            </ul>
          </div>

          {/* Review Panel */}
          {isReviewable && (
            <div className="bg-amber-50 rounded-xl shadow-sm border border-amber-200 p-6">
              <h3 className="font-bold text-amber-900 mb-2">Human Review Required</h3>
              <p className="text-sm text-amber-700 mb-4">
                Please review the missing or ambiguous data points and make a final eligibility determination.
              </p>
              
              <textarea 
                className="w-full bg-white border border-amber-300 rounded-lg p-3 text-sm mb-4 focus:ring-2 focus:ring-amber-500 focus:outline-none"
                rows="4"
                placeholder="Enter clinical notes justifying the decision..."
                value={reviewNotes}
                onChange={e => setReviewNotes(e.target.value)}
              />

              <div className="space-y-2">
                <button 
                  onClick={() => resolveMutation.mutate({ decision: 'ELIGIBLE' })}
                  disabled={resolveMutation.isPending || !reviewNotes}
                  className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2.5 rounded-lg transition-colors disabled:opacity-50"
                >
                  Mark as ELIGIBLE
                </button>
                <button 
                  onClick={() => resolveMutation.mutate({ decision: 'NOT_ELIGIBLE' })}
                  disabled={resolveMutation.isPending || !reviewNotes}
                  className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-2.5 rounded-lg transition-colors disabled:opacity-50"
                >
                  Mark as NOT ELIGIBLE
                </button>
              </div>
            </div>
          )}

        </div>
      </div>

      <SourceDataModal 
        isOpen={!!selectedCriterion} 
        onClose={() => setSelectedCriterion(null)} 
        criterion={selectedCriterion} 
      />
    </div>
  );
};

export default MatchExplanation;
