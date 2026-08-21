import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { 
  ArrowLeft, Building2, Beaker, FileText, Activity, 
  Pill, Syringe, Clock, X, Target, CheckCircle2 
} from 'lucide-react';
import { getPatient, getPatientTimeline, getPatientMatches } from '../api/client';
import { format } from 'date-fns';
import Avatar from './Avatar';
import SourceDocumentContent from './SourceDocumentContent';

const SourceDataModal = ({ isOpen, onClose, event }) => {
  if (!isOpen || !event) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 backdrop-blur-sm">
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
        
        <div className="p-6 bg-gray-50 border-b border-gray-100 grid grid-cols-3 gap-4">
          <div>
            <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Source System</p>
            <p className="font-medium text-gray-900">{event.sourceSystem}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Source Record ID</p>
            <p className="font-mono text-sm text-gray-900">{event.sourceRecordId}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 uppercase font-semibold mb-1">Source Format</p>
            <p className="font-medium text-gray-900">{(event.sourceFormat || 'FHIR').replaceAll('_', ' ')} · {event.resourceType}</p>
          </div>
        </div>

        <SourceDocumentContent event={event} />
      </div>
    </div>
  );
};

// Database icon for the modal since it's not imported at the top
import { Database } from 'lucide-react';

const PatientProfile = () => {
  const { id } = useParams();
  const [activeTab, setActiveTab] = useState('ALL');
  const [selectedEvent, setSelectedEvent] = useState(null);

  const { data: patient, isLoading: pLoading } = useQuery({
    queryKey: ['patient', id],
    queryFn: () => getPatient(id)
  });

  const { data: timeline, isLoading: tLoading } = useQuery({
    queryKey: ['timeline', id],
    queryFn: () => getPatientTimeline(id)
  });

  const { data: matches } = useQuery({
    queryKey: ['patient-matches', id],
    queryFn: () => getPatientMatches(id)
  });

  if (pLoading) {
    return <div className="p-8">Loading patient profile...</div>;
  }

  const events = timeline || [];
  const filteredEvents = activeTab === 'ALL' 
    ? events 
    : events.filter(e => e.resourceType.toUpperCase() === activeTab);

  const getEventIcon = (type) => {
    switch(type.toUpperCase()) {
      case 'OBSERVATION': return <Activity className="w-4 h-4" />;
      case 'CONDITION': return <Target className="w-4 h-4" />;
      case 'MEDICATION':
      case 'MEDICATIONREQUEST': return <Pill className="w-4 h-4" />;
      case 'PROCEDURE': return <Syringe className="w-4 h-4" />;
      default: return <FileText className="w-4 h-4" />;
    }
  };

  const getSourceColor = (source) => {
    if (source.includes('HOSPITAL_A')) return 'bg-blue-100 text-blue-800 border-blue-200';
    if (source.includes('LAB_A')) return 'bg-green-100 text-green-800 border-green-200';
    if (source.includes('HOSPITAL_B')) return 'bg-indigo-100 text-indigo-800 border-indigo-200';
    return 'bg-purple-100 text-purple-800 border-purple-200';
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <Link to="/patients" className="inline-flex items-center text-sm text-blue-600 hover:underline mb-4">
          <ArrowLeft className="w-4 h-4 mr-1" /> Back to Patients
        </Link>
        <div className="flex items-end justify-between">
          <div>
            <div className="flex items-center gap-4 mb-2">
              <Avatar name={patient?.name} size="lg" />
              <h1 className="text-3xl font-bold text-gray-900">{patient?.name}</h1>
              <span className="bg-gray-900 text-white font-mono px-3 py-1 rounded-md text-sm shadow-sm">
                {patient?.canonicalId}
              </span>
            </div>
            <div className="flex items-center gap-4 text-gray-600">
              <span className="flex items-center gap-1.5"><Clock className="w-4 h-4"/> DOB: {patient?.birthDate}</span>
              <span className="bg-gray-200 w-1.5 h-1.5 rounded-full"></span>
              <span>Age: <strong className="text-gray-900">{patient?.age}</strong></span>
              <span className="bg-gray-200 w-1.5 h-1.5 rounded-full"></span>
              <span>Gender: <strong className="text-gray-900">{patient?.gender}</strong></span>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Column */}
        <div className="space-y-6 lg:col-span-1">
          {/* Identity Map */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4 border-b border-gray-100 pb-3">
              Identity Map — Source Systems
            </h3>
            <div className="space-y-4">
              {patient?.sourceIdentities?.map((id, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 rounded-lg border border-gray-100 bg-gray-50">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-white rounded shadow-sm">
                      {id.sourceSystem.includes('LAB') ? <Beaker className="w-4 h-4 text-purple-600"/> : <Building2 className="w-4 h-4 text-blue-600"/>}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-gray-900">{id.sourceSystem}</p>
                      <p className="text-xs font-mono text-gray-500">{id.sourcePatientId}</p>
                    </div>
                  </div>
                  <span className="bg-green-100 text-green-700 text-xs px-2 py-1 rounded-full font-bold flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3" /> CONFIRMED
                  </span>
                </div>
              ))}
              {(!patient?.sourceIdentities || patient.sourceIdentities.length === 0) && (
                <div className="text-sm text-gray-500 text-center py-4">No source identities mapped.</div>
              )}
            </div>
          </div>

          {/* Trial Matches */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4 border-b border-gray-100 pb-3 flex items-center justify-between">
              Trial Matches
              <span className="bg-blue-100 text-blue-700 text-xs py-0.5 px-2 rounded-full">{matches?.length || 0}</span>
            </h3>
            <div className="space-y-3">
              {matches?.map((match) => (
                <div key={match.id} className="border border-gray-200 rounded-lg p-4 hover:border-blue-300 transition-colors">
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-bold text-sm text-gray-900 line-clamp-2">{match.trial?.title || 'Unknown Trial'}</h4>
                  </div>
                  <div className="flex items-center justify-between mt-3">
                    <span className={`text-xs font-bold px-2 py-1 rounded-md ${
                      match.status === 'ELIGIBLE' ? 'bg-green-100 text-green-700' :
                      match.status === 'NEEDS_REVIEW' ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700'
                    }`}>
                      {match.status}
                    </span>
                    <Link to={`/matches/${match.id}`} className="text-xs text-blue-600 font-medium hover:underline">
                      View Details &rarr;
                    </Link>
                  </div>
                </div>
              ))}
              {(!matches || matches.length === 0) && (
                <div className="text-sm text-gray-500 text-center py-4">No trial matches run yet.</div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column - Timeline */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col h-[800px]">
            <div className="p-6 border-b border-gray-100">
              <h3 className="text-lg font-bold text-gray-900 mb-4">Longitudinal Clinical Timeline</h3>
              
              {/* Tabs */}
              <div className="flex flex-wrap gap-2">
                {['ALL', 'OBSERVATION', 'CONDITION', 'MEDICATION', 'PROCEDURE'].map(tab => (
                  <button
                    key={tab}
                    onClick={() => setActiveTab(tab)}
                    className={`px-3 py-1.5 text-xs font-bold rounded-full transition-colors ${
                      activeTab === tab 
                        ? 'bg-navy-900 text-white' 
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    }`}
                  >
                    {tab}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex-1 overflow-auto p-6">
              {tLoading ? (
                <div className="space-y-8">
                  {[1, 2, 3].map(i => (
                    <div key={i} className="flex gap-4 animate-pulse">
                      <div className="w-24 h-4 bg-gray-200 rounded"></div>
                      <div className="flex-1 space-y-2">
                        <div className="h-4 bg-gray-200 rounded w-1/4"></div>
                        <div className="h-16 bg-gray-200 rounded-lg w-full"></div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : filteredEvents.length === 0 ? (
                <div className="h-full flex flex-col items-center justify-center text-gray-500">
                  <Activity className="w-12 h-12 mb-4 text-gray-300" />
                  <p>No clinical events found for this filter.</p>
                </div>
              ) : (
                <div className="relative border-l-2 border-gray-200 ml-4 space-y-8 pb-8">
                  {filteredEvents.map((event, idx) => (
                    <div key={idx} className="relative pl-6">
                      {/* Timeline dot */}
                      <div className="absolute -left-[9px] top-1.5 w-4 h-4 rounded-full bg-white border-2 border-blue-500"></div>
                      
                      <div className="flex flex-col sm:flex-row sm:items-baseline gap-2 mb-2">
                        <span className="text-sm font-bold text-gray-900 w-32 shrink-0">
                          {format(new Date(event.timestamp || Date.now()), 'MMM d, yyyy')}
                        </span>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className={`text-xs px-2 py-0.5 rounded border font-medium ${getSourceColor(event.sourceSystem)}`}>
                            {event.sourceSystem}
                          </span>
                          <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded flex items-center gap-1 font-medium">
                            {getEventIcon(event.resourceType)} {event.resourceType}
                          </span>
                        </div>
                      </div>

                      <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow group relative">
                        <div className="pr-24">
                          <h4 className="font-bold text-gray-900 text-base">{event.name}</h4>
                          {event.value && (
                            <p className="mt-1 text-gray-700">
                              Result: <span className="font-bold bg-yellow-50 px-1 rounded">{event.value}</span> {event.unit}
                            </p>
                          )}
                        </div>
                        <button 
                          onClick={() => setSelectedEvent(event)}
                          className="absolute right-4 top-4 inline-flex items-center gap-1.5 rounded-lg border border-blue-200 bg-blue-600 px-3 py-2 text-xs font-bold text-white shadow-sm transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
                        >
                          <FileText className="h-3.5 w-3.5" /> View source
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <SourceDataModal 
        isOpen={!!selectedEvent} 
        onClose={() => setSelectedEvent(null)} 
        event={selectedEvent} 
      />
    </div>
  );
};

export default PatientProfile;
