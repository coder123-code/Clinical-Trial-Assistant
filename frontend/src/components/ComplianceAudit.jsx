import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ShieldCheck, CheckCircle2, AlertTriangle, FileJson, ChevronLeft, ChevronRight } from 'lucide-react';
import { getAuditLogs } from '../api/client';
import { format } from 'date-fns';

const ComplianceAudit = () => {
  const [page, setPage] = useState(0);
  
  const { data, isLoading } = useQuery({
    queryKey: ['audit-logs', page],
    queryFn: () => getAuditLogs(page, 20),
    refetchInterval: 10000 // Real-time feel
  });

  const getActionColor = (action) => {
    if (action.includes('INGESTED')) return 'bg-blue-100 text-blue-800 border-blue-200';
    if (action.includes('MATCH')) return 'bg-purple-100 text-purple-800 border-purple-200';
    if (action.includes('RESOLVED')) return 'bg-green-100 text-green-800 border-green-200';
    if (action.includes('EXTRACTED')) return 'bg-amber-100 text-amber-800 border-amber-200';
    return 'bg-gray-100 text-gray-800 border-gray-200';
  };

  return (
    <div className="space-y-6">
      {/* Compliance Summary Header */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 flex flex-col md:flex-row gap-8 items-center">
        <div className="w-24 h-24 bg-green-50 rounded-full flex items-center justify-center shrink-0 border-4 border-green-100">
          <ShieldCheck className="w-12 h-12 text-green-600" />
        </div>
        <div className="flex-1">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Compliance & Data Governance</h2>
          <p className="text-gray-600 mb-4">
            This system adheres to strict data provenance, deterministic AI evaluation, and human-in-the-loop workflows for clinical trial matching.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2">
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> Synthetic data only (no real PHI)
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> Source data provenance preserved
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> Deterministic eligibility engine
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> AI does NOT make final decisions
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> Human review workflow active
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-700 font-medium">
              <CheckCircle2 className="w-4 h-4 text-green-500" /> Immutable audit trail maintained
            </div>
          </div>
        </div>
        <div className="bg-amber-50 border border-amber-200 p-4 rounded-xl max-w-xs text-sm text-amber-900 shrink-0">
          <div className="flex items-center gap-2 font-bold mb-1">
            <AlertTriangle className="w-4 h-4 text-amber-600" /> RESEARCH USE
          </div>
          Decision support requires qualified human oversight and is not a substitute for clinical judgment.
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col h-[600px]">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
          <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <FileJson className="w-5 h-5 text-gray-500" /> System Audit Trail
          </h3>
          <div className="text-sm text-gray-500 flex items-center gap-2">
            <span className="relative flex h-2.5 w-2.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
            </span>
            Live Monitoring
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          <table className="w-full text-sm text-left text-gray-600">
            <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b border-gray-100 sticky top-0 z-10">
              <tr>
                <th className="px-6 py-4 font-semibold w-48">Timestamp</th>
                <th className="px-6 py-4 font-semibold w-40">Action</th>
                <th className="px-6 py-4 font-semibold w-32">Entity Type</th>
                <th className="px-6 py-4 font-semibold w-32">Entity ID</th>
                <th className="px-6 py-4 font-semibold">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {isLoading ? (
                [...Array(10)].map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td className="px-6 py-3"><div className="h-4 bg-gray-200 rounded w-32"></div></td>
                    <td className="px-6 py-3"><div className="h-6 bg-gray-200 rounded-full w-24"></div></td>
                    <td className="px-6 py-3"><div className="h-4 bg-gray-200 rounded w-20"></div></td>
                    <td className="px-6 py-3"><div className="h-4 bg-gray-200 rounded w-24"></div></td>
                    <td className="px-6 py-3"><div className="h-4 bg-gray-200 rounded w-full"></div></td>
                  </tr>
                ))
              ) : data?.content?.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                    No audit logs available.
                  </td>
                </tr>
              ) : (
                data?.content?.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50 transition-colors font-mono text-[13px]">
                    <td className="px-6 py-3 whitespace-nowrap text-gray-500">
                      {format(new Date(log.timestamp), 'yyyy-MM-dd HH:mm:ss.SSS')}
                    </td>
                    <td className="px-6 py-3 whitespace-nowrap">
                      <span className={`px-2 py-1 rounded text-xs font-bold border ${getActionColor(log.action)}`}>
                        {log.action}
                      </span>
                    </td>
                    <td className="px-6 py-3 whitespace-nowrap font-semibold text-gray-700">
                      {log.entityType}
                    </td>
                    <td className="px-6 py-3 whitespace-nowrap">
                      {log.entityId || '-'}
                    </td>
                    <td className="px-6 py-3 text-gray-600 truncate max-w-md" title={log.details}>
                      {log.details}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="p-4 border-t border-gray-100 bg-white flex items-center justify-between">
          <span className="text-sm text-gray-500">
            Page <span className="font-medium text-gray-900">{page + 1}</span> of {data?.totalPages || 1}
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0 || isLoading}
              className="p-1.5 border border-gray-200 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={!data || data.last || isLoading}
              className="p-1.5 border border-gray-200 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ComplianceAudit;
