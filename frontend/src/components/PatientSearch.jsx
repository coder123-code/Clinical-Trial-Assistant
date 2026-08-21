import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Search, ChevronLeft, ChevronRight, UserX, Database, Activity } from 'lucide-react';
import { getPatients } from '../api/client';
import Avatar from './Avatar';

const PatientSearch = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedTerm, setDebouncedTerm] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedTerm(searchTerm);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const { data, isLoading } = useQuery({
    queryKey: ['patients', debouncedTerm, page],
    queryFn: () => getPatients(debouncedTerm, page, 10),
  });

  const getInitials = (name) => {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex flex-col h-[calc(100vh-8rem)]">
      
      {/* Search Header */}
      <div className="p-6 border-b border-gray-100 bg-gray-50/50">
        <div className="relative max-w-2xl">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            className="block w-full pl-11 pr-4 py-3 border border-gray-200 rounded-xl leading-5 bg-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm transition-shadow shadow-sm"
            placeholder="Search by patient name or Canonical ID (e.g., P000001)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* Table Content */}
      <div className="flex-1 overflow-auto">
        <table className="w-full text-sm text-left text-gray-600">
          <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b border-gray-100 sticky top-0 z-10">
            <tr>
              <th className="px-6 py-4 font-semibold">Patient</th>
              <th className="px-6 py-4 font-semibold">Canonical ID</th>
              <th className="px-6 py-4 font-semibold">Demographics</th>
              <th className="px-6 py-4 font-semibold">Sources</th>
              <th className="px-6 py-4 font-semibold">Clinical Events</th>
              <th className="px-6 py-4 font-semibold text-right">Actions</th>
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
                  <td className="px-6 py-5"><div className="h-4 bg-gray-200 rounded w-20"></div></td>
                  <td className="px-6 py-5"><div className="h-4 bg-gray-200 rounded w-24"></div></td>
                  <td className="px-6 py-5"><div className="h-6 bg-gray-200 rounded-full w-12"></div></td>
                  <td className="px-6 py-5"><div className="h-4 bg-gray-200 rounded w-16"></div></td>
                  <td className="px-6 py-5 text-right"><div className="h-8 bg-gray-200 rounded w-24 ml-auto"></div></td>
                </tr>
              ))
            ) : data?.content?.length === 0 ? (
              <tr>
                <td colSpan="6">
                  <div className="flex flex-col items-center justify-center py-20 text-center">
                    <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                      <UserX className="w-8 h-8 text-gray-400" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-900 mb-1">No patients found</h3>
                    <p className="text-gray-500 max-w-sm">
                      We couldn't find any patients matching "{searchTerm}". Try adjusting your search term.
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              data?.content?.map((patient) => (
                <tr key={patient.id} className="hover:bg-blue-50/30 transition-colors group">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <Avatar name={patient.name} size="md" />
                      <div className="font-bold text-gray-900">{patient.name}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="bg-gray-100 text-gray-800 font-mono text-xs px-2.5 py-1 rounded border border-gray-200">
                      {patient.canonicalId}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {patient.age ? `${patient.age}y` : 'Unknown'} • {patient.gender || 'Unknown'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center gap-1.5 bg-indigo-50 text-indigo-700 px-2.5 py-1 rounded-full text-xs font-semibold border border-indigo-100">
                      <Database className="w-3.5 h-3.5" />
                      {patient.sourceCount || 1} System{patient.sourceCount !== 1 ? 's' : ''}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-gray-500">
                    <span className="flex items-center gap-1.5">
                      <Activity className="w-4 h-4 text-gray-400" />
                      {patient.eventCount || 0} records
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <button
                      onClick={() => navigate(`/patients/${patient.id}`)}
                      className="px-4 py-1.5 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 hover:text-blue-600 hover:border-blue-300 transition-colors shadow-sm"
                    >
                      View Profile
                    </button>
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
          Showing <span className="font-medium text-gray-900">{data?.content?.length || 0}</span> results
        </span>
        <div className="flex gap-2">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0 || isLoading}
            className="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
          <button
            onClick={() => setPage(page + 1)}
            disabled={!data || data.last || isLoading}
            className="p-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default PatientSearch;
