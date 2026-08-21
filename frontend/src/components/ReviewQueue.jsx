import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Target, AlertTriangle, HelpCircle, CheckCircle, Clock } from 'lucide-react';
import { getReviewQueue } from '../api/client';
import { formatDistanceToNow } from 'date-fns';

const ReviewQueue = () => {
  const navigate = useNavigate();
  const [filter, setFilter] = useState('ALL'); // ALL, NEEDS_REVIEW, POTENTIALLY_ELIGIBLE

  const { data, isLoading } = useQuery({
    queryKey: ['review-queue'],
    queryFn: getReviewQueue,
    refetchInterval: 30000 // Refresh every 30s
  });

  const queue = data || [];
  const filteredQueue = filter === 'ALL' 
    ? queue 
    : queue.filter(item => item.status === filter);

  const getInitials = (name) => {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex flex-col h-[calc(100vh-8rem)]">
      
      {/* Header */}
      <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-amber-100 text-amber-600 rounded-lg">
            <Target className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              Human Review Queue
              <span className="bg-amber-100 text-amber-800 text-sm py-0.5 px-2.5 rounded-full font-bold">
                {queue.length}
              </span>
            </h2>
            <p className="text-sm text-gray-500">Requires clinical judgement to resolve missing data or complex criteria.</p>
          </div>
        </div>

        <div className="flex bg-white rounded-lg border border-gray-200 p-1 shadow-sm">
          <button 
            onClick={() => setFilter('ALL')}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${filter === 'ALL' ? 'bg-gray-100 text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-900'}`}
          >
            All Items
          </button>
          <button 
            onClick={() => setFilter('NEEDS_REVIEW')}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${filter === 'NEEDS_REVIEW' ? 'bg-amber-50 text-amber-800 shadow-sm border border-amber-100' : 'text-gray-500 hover:text-gray-900'}`}
          >
            Needs Review
          </button>
          <button 
            onClick={() => setFilter('POTENTIALLY_ELIGIBLE')}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${filter === 'POTENTIALLY_ELIGIBLE' ? 'bg-blue-50 text-blue-800 shadow-sm border border-blue-100' : 'text-gray-500 hover:text-gray-900'}`}
          >
            Potentially Eligible
          </button>
        </div>
      </div>

      {/* Table Content */}
      <div className="flex-1 overflow-auto">
        <table className="w-full text-sm text-left text-gray-600">
          <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b border-gray-100 sticky top-0 z-10">
            <tr>
              <th className="px-6 py-4 font-semibold">Patient</th>
              <th className="px-6 py-4 font-semibold">Clinical Trial</th>
              <th className="px-6 py-4 font-semibold">Status</th>
              <th className="px-6 py-4 font-semibold">Issues</th>
              <th className="px-6 py-4 font-semibold">Time in Queue</th>
              <th className="px-6 py-4 font-semibold text-right">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {isLoading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  <td className="px-6 py-5">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-gray-200 rounded-full"></div>
                      <div className="h-4 bg-gray-200 rounded w-32"></div>
                    </div>
                  </td>
                  <td className="px-6 py-5"><div className="h-4 bg-gray-200 rounded w-40"></div></td>
                  <td className="px-6 py-5"><div className="h-6 bg-gray-200 rounded-full w-24"></div></td>
                  <td className="px-6 py-5"><div className="h-6 bg-gray-200 rounded-full w-16"></div></td>
                  <td className="px-6 py-5"><div className="h-4 bg-gray-200 rounded w-20"></div></td>
                  <td className="px-6 py-5 text-right"><div className="h-8 bg-gray-200 rounded w-24 ml-auto"></div></td>
                </tr>
              ))
            ) : filteredQueue.length === 0 ? (
              <tr>
                <td colSpan="6">
                  <div className="flex flex-col items-center justify-center py-24 text-center">
                    <div className="w-16 h-16 bg-green-50 rounded-full flex items-center justify-center mb-4">
                      <CheckCircle className="w-8 h-8 text-green-500" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-1">All caught up!</h3>
                    <p className="text-gray-500 max-w-sm">
                      There are no matches currently requiring human review in the queue.
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              filteredQueue.map((item) => {
                const missingCount = item.results?.filter(r => r.result === 'MISSING').length || 0;
                const reviewCount = item.results?.filter(r => r.result === 'REVIEW_REQUIRED').length || 0;
                
                return (
                  <tr key={item.id} className="hover:bg-amber-50/30 transition-colors group">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-sm border border-blue-200">
                          {getInitials(item.patient?.name)}
                        </div>
                        <div>
                          <div className="font-bold text-gray-900">{item.patient?.name}</div>
                          <div className="text-xs font-mono text-gray-500">{item.patient?.canonicalId}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-gray-900 line-clamp-1">{item.trial?.title}</div>
                      <div className="text-xs text-gray-500">{item.trial?.code}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold border ${
                        item.status === 'NEEDS_REVIEW' ? 'bg-amber-100 text-amber-800 border-amber-200' : 'bg-blue-100 text-blue-800 border-blue-200'
                      }`}>
                        {item.status === 'NEEDS_REVIEW' ? <AlertTriangle className="w-3.5 h-3.5" /> : <HelpCircle className="w-3.5 h-3.5" />}
                        {item.status.replace('_', ' ')}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex gap-2">
                        {missingCount > 0 && (
                          <span className="bg-gray-100 text-gray-700 text-xs px-2 py-1 rounded font-medium border border-gray-200" title={`${missingCount} missing data points`}>
                            {missingCount} Missing
                          </span>
                        )}
                        {reviewCount > 0 && (
                          <span className="bg-red-50 text-red-700 text-xs px-2 py-1 rounded font-medium border border-red-100" title={`${reviewCount} criteria failed/need review`}>
                            {reviewCount} Review
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-gray-500 text-sm flex items-center gap-1.5">
                      <Clock className="w-4 h-4 text-gray-400" />
                      {item.timestamp ? formatDistanceToNow(new Date(item.timestamp), { addSuffix: true }) : 'Recently'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <button
                        onClick={() => navigate(`/matches/${item.id}`)}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-bold hover:bg-blue-700 transition-colors shadow-sm"
                      >
                        Review Match
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ReviewQueue;
