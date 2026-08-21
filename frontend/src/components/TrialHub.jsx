import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, BrainCircuit, Target, Check, AlertCircle, X, FileText, Upload, RefreshCw } from 'lucide-react';
import { getTrials, createTrial, extractCriteria, confirmAndScreenCohort, uploadPdf } from '../api/client';

const CreateTrialModal = ({ isOpen, onClose }) => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({ title: '', code: '', description: '', eligibilityText: '' });
  const [pdfLoading, setPdfLoading] = useState(false);

  const loadDemoProtocol = () => setFormData({
    title: 'PRECISION-DM: Renal-Safe Diabetes Therapy Study',
    code: 'NCT-CTIQ-2026',
    description: 'A multicenter study evaluating precision glucose management in adults with Type 2 Diabetes while protecting renal function.',
    eligibilityText: `INCLUSION CRITERIA:\n- Adults aged between 18 and 65 years.\n- Confirmed diagnosis of Type 2 Diabetes Mellitus.\n- HbA1c between 7% and 10%.\n- eGFR greater than 30 mL/min/1.73m2.\n\nEXCLUSION CRITERIA:\n- Current treatment with insulin pump therapy.\n- History of severe hypoglycemia within the previous 90 days.`
  });
  
  const mutation = useMutation({
    mutationFn: createTrial,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trials'] });
      onClose();
    }
  });

  const handlePdfUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setPdfLoading(true);
    try {
      const data = await uploadPdf(file);
      setFormData(prev => ({
        ...prev,
        eligibilityText: data.text
      }));
    } catch (err) {
      console.error(err);
      alert('Failed to parse PDF: ' + (err.response?.data?.error || err.message));
    } finally {
      setPdfLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col">
        <div className="flex justify-between items-center p-6 border-b border-gray-100">
          <div><h3 className="text-xl font-bold text-gray-900">Create New Clinical Trial</h3><p className="text-xs text-gray-500 mt-1">Upload a protocol or begin with a structured protocol template.</p></div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors"><X className="w-5 h-5" /></button>
        </div>
        
        <div className="p-6 overflow-auto flex-1 space-y-4">
          <button type="button" onClick={loadDemoProtocol} className="w-full rounded-xl border border-purple-200 bg-purple-50 p-3 text-sm font-bold text-purple-800 hover:bg-purple-100 flex items-center justify-center gap-2">
            <BrainCircuit className="w-4 h-4" /> Load protocol template
          </button>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Trial Title *</label>
              <input 
                type="text" required
                className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Trial Code</label>
              <input 
                type="text"
                className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-mono"
                value={formData.code} onChange={e => setFormData({...formData, code: e.target.value})}
                placeholder="e.g. NCT01234567"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea 
              className="w-full border border-gray-300 rounded-lg p-2.5 h-20 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})}
            />
          </div>

          {/* PDF Protocol Upload Area */}
          <div className="border border-dashed border-gray-300 rounded-lg p-4 bg-gray-50 flex flex-col items-center">
            <FileText className="w-8 h-8 text-gray-400 mb-2" />
            <span className="text-sm font-medium text-gray-700 mb-1">Upload Trial Protocol PDF</span>
            <span className="text-xs text-gray-500 mb-3">Extract criteria text directly from protocol PDF</span>
            
            <label className="cursor-pointer inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-bold text-gray-700 hover:bg-gray-50 shadow-sm transition-all">
              {pdfLoading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin text-blue-600" />
                  <span>Parsing PDF...</span>
                </>
              ) : (
                <>
                  <Upload className="w-4 h-4 text-blue-600" />
                  <span>Choose PDF File</span>
                </>
              )}
              <input 
                type="file" accept=".pdf" className="hidden" 
                onChange={handlePdfUpload} disabled={pdfLoading}
              />
            </label>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Raw Eligibility Criteria (Optional)</label>
            <p className="text-xs text-gray-500 mb-2">You can paste protocol text here or let the PDF upload populate it.</p>
            <textarea 
              className="w-full border border-gray-300 rounded-lg p-2.5 h-40 font-mono text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              value={formData.eligibilityText} onChange={e => setFormData({...formData, eligibilityText: e.target.value})}
              placeholder="Inclusion Criteria:&#10;- Age 18-65&#10;- Diagnosed with Type 2 Diabetes&#10;Exclusion Criteria:&#10;- Pregnant or nursing"
            />
          </div>
        </div>
        
        <div className="p-6 border-t border-gray-100 bg-gray-50 flex justify-end gap-3">
          <button onClick={onClose} className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-100">Cancel</button>
          <button 
            onClick={() => mutation.mutate(formData)}
            disabled={!formData.title || mutation.isPending}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            {mutation.isPending ? 'Creating...' : 'Create Trial'}
          </button>
        </div>
      </div>
    </div>
  );
};

const TrialHub = () => {
  const queryClient = useQueryClient();
  const [selectedTrialId, setSelectedTrialId] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [extracting, setExtracting] = useState(false);
  const [cohortResults, setCohortResults] = useState(null);
  const [runningMatch, setRunningMatch] = useState(false);

  const { data: trials } = useQuery({ queryKey: ['trials'], queryFn: getTrials });
  
  const selectedTrial = trials?.find(t => t.id === selectedTrialId) || null;

  const handleExtract = async () => {
    if (!selectedTrial) return;
    setExtracting(true);
    try {
      await extractCriteria(selectedTrial.id, selectedTrial.eligibilityText || '');
      queryClient.invalidateQueries({ queryKey: ['trials'] });
    } catch (e) {
      console.error(e);
      alert('Extraction failed. Make sure AI service is running.');
    } finally {
      setExtracting(false);
    }
  };

  const handleCohortScreen = async () => {
    if (!selectedTrial) return;
    setRunningMatch(true);
    setCohortResults(null);
    try {
      const results = await confirmAndScreenCohort(selectedTrial.id);
      setCohortResults(results);
      queryClient.invalidateQueries({ queryKey: ['trials'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['review-queue'] });
    } catch (e) {
      alert(e.response?.data?.message || 'Cohort screening failed');
    } finally {
      setRunningMatch(false);
    }
  };

  return (
    <div className="flex gap-6 h-[calc(100vh-8rem)]">
      {/* Left Panel - Trials List */}
      <div className="w-1/3 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
          <h2 className="font-bold text-gray-900">Clinical Trials</h2>
          <button 
            onClick={() => setIsCreateModalOpen(true)}
            className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            title="Create Trial"
          >
            <Plus className="w-4 h-4" />
          </button>
        </div>
        
        <div className="flex-1 overflow-auto p-2 space-y-2">
          {trials?.map(trial => (
            <div 
              key={trial.id}
              onClick={() => { setSelectedTrialId(trial.id); setCohortResults(null); }}
              className={`p-4 rounded-lg cursor-pointer border transition-all ${
                selectedTrialId === trial.id 
                  ? 'border-blue-500 bg-blue-50 shadow-sm' 
                  : 'border-transparent hover:bg-gray-50 hover:border-gray-200'
              }`}
            >
              <div className="flex justify-between items-start mb-1">
                <h3 className="font-bold text-gray-900 text-sm line-clamp-1">{trial.code}</h3>
                <span className="text-xs font-medium bg-gray-200 text-gray-700 px-2 rounded-full">
                  {trial.criteria?.length || 0} criteria
                </span>
              </div>
              <p className="text-xs text-gray-600 line-clamp-2">{trial.title}</p>
            </div>
          ))}
          {trials?.length === 0 && (
            <div className="p-8 text-center text-gray-500 text-sm">
              No clinical trials found. Create one to get started.
            </div>
          )}
        </div>
      </div>

      {/* Right Panel - Trial Detail */}
      <div className="w-2/3 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col overflow-hidden">
        {selectedTrial ? (
          <>
            <div className="p-6 border-b border-gray-100 bg-gray-50/30 shrink-0">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <h2 className="text-2xl font-bold text-gray-900">{selectedTrial.code || 'Draft Trial'}</h2>
                  </div>
                  <h3 className="text-gray-700 font-medium">{selectedTrial.title}</h3>
                  <p className="text-sm text-gray-500 mt-2 line-clamp-2">{selectedTrial.description}</p>
                </div>
              </div>
            </div>

            <div className="flex-1 overflow-auto p-6 space-y-8 scrollbar-thin">
              
              {/* AI Extraction Section */}
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <h3 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                    <BrainCircuit className="w-5 h-5 text-purple-600" /> Structured Criteria
                  </h3>
                  <button 
                    onClick={handleExtract}
                    disabled={extracting}
                    className="px-3 py-1.5 bg-purple-100 text-purple-700 rounded-lg text-sm font-bold hover:bg-purple-200 transition-colors flex items-center gap-2 disabled:opacity-50"
                  >
                    {extracting ? <span className="animate-pulse">Extracting...</span> : 'Run AI Extraction'}
                  </button>
                </div>

                {selectedTrial.criteria && selectedTrial.criteria.length > 0 ? (
                  <div className="border border-gray-200 rounded-lg overflow-hidden">
                    <table className="w-full text-sm text-left">
                      <thead className="bg-gray-50 text-gray-600 text-xs uppercase border-b border-gray-200">
                        <tr>
                          <th className="px-4 py-3 font-semibold">Type</th>
                          <th className="px-4 py-3 font-semibold">Field</th>
                          <th className="px-4 py-3 font-semibold">Operator / Value</th>
                          <th className="px-4 py-3 font-semibold">Description</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {selectedTrial.criteria.map((c, i) => (
                          <tr key={i} className="hover:bg-gray-50">
                            <td className="px-4 py-3">
                              <span className={`px-2 py-1 rounded text-[10px] font-bold uppercase ${
                                c.type === 'INCLUSION' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                              }`}>
                                {c.type}
                              </span>
                            </td>
                            <td className="px-4 py-3 font-medium text-gray-900">{c.field}</td>
                            <td className="px-4 py-3 font-mono text-xs">
                              {c.operator} <span className="font-bold">{c.value}</span> {c.unit}
                            </td>
                            <td className="px-4 py-3 text-gray-600 text-xs">{c.description}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="p-8 border-2 border-dashed border-gray-200 rounded-xl text-center">
                    <BrainCircuit className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                    <p className="text-gray-500 font-medium">No structured criteria found.</p>
                    <p className="text-sm text-gray-400 mt-1">Run AI Extraction to parse the raw eligibility text.</p>
                  </div>
                )}
              </div>

              {/* Automatic cohort matching */}
              <div className="space-y-4 pt-4 border-t border-gray-100">
                <div className="rounded-2xl bg-gradient-to-r from-blue-950 to-indigo-900 p-5 text-white">
                  <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"><div><h3 className="flex items-center gap-2 text-lg font-black"><Target className="h-5 w-5 text-cyan-300" /> Automatic cohort screening</h3><p className="mt-1 text-sm text-blue-100">Confirm the extracted rules and evaluate every patient automatically. Missing evidence is routed to human review.</p></div><button onClick={handleCohortScreen} disabled={runningMatch || !selectedTrial.criteria?.length} className="shrink-0 rounded-xl bg-cyan-300 px-5 py-3 text-sm font-black text-slate-950 hover:bg-cyan-200 disabled:opacity-50">{runningMatch ? 'Screening cohort…' : 'Confirm & screen all patients'}</button></div>
                </div>
                {cohortResults && <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">{[['Eligible','ELIGIBLE','border-emerald-200 bg-emerald-50','text-emerald-700'],['Potential','POTENTIALLY_ELIGIBLE','border-blue-200 bg-blue-50','text-blue-700'],['Review','NEEDS_REVIEW','border-amber-200 bg-amber-50','text-amber-700'],['Not eligible','NOT_ELIGIBLE','border-rose-200 bg-rose-50','text-rose-700']].map(([label,status,card,value])=><div key={status} className={`rounded-xl border p-4 ${card}`}><p className={`text-2xl font-black ${value}`}>{cohortResults.filter(r=>r.status===status).length}</p><p className="text-xs font-bold uppercase tracking-wide text-slate-500">{label}</p></div>)}</div>}
                {cohortResults?.length > 0 && <div className="overflow-hidden rounded-xl border border-slate-200"><div className="bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700">Screened {cohortResults.length} patients</div><div className="max-h-72 divide-y divide-slate-100 overflow-auto">{cohortResults.map(result=><a key={result.id} href={`/matches/${result.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-blue-50"><div><p className="text-sm font-bold text-slate-900">{result.patient.name}</p><p className="text-xs text-slate-500">{result.patient.canonicalId}</p></div><span className={`rounded-full px-2.5 py-1 text-[11px] font-black ${result.status==='ELIGIBLE'?'bg-emerald-100 text-emerald-700':result.status==='NOT_ELIGIBLE'?'bg-rose-100 text-rose-700':'bg-amber-100 text-amber-700'}`}>{result.status.replaceAll('_',' ')}</span></a>)}</div></div>}
              </div>

            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-400 p-8 text-center">
            <FlaskConical className="w-16 h-16 mb-4 text-gray-200" />
            <h3 className="text-lg font-medium text-gray-900 mb-1">Select a Clinical Trial</h3>
            <p>Choose a trial from the list to view details, extract criteria, or run matching.</p>
          </div>
        )}
      </div>

      <CreateTrialModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} />
    </div>
  );
};

// FlaskConical missing import fallback
import { FlaskConical } from 'lucide-react';

export default TrialHub;
