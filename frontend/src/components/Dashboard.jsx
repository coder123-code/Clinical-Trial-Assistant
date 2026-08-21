import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Users, Activity, FlaskConical, Target, AlertTriangle, CheckCircle, Database, Server } from 'lucide-react';
import { getDashboard } from '../api/client';
import { format } from 'date-fns';

const StatCard = ({ title, value, icon: Icon, colorClass, bgColorClass, iconColorClass }) => (
  <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex items-center gap-4 hover:shadow-md transition-shadow">
    <div className={`p-4 rounded-xl ${bgColorClass}`}>
      <Icon className={`w-8 h-8 ${iconColorClass}`} />
    </div>
    <div>
      <p className="text-sm font-medium text-gray-500 mb-1">{title}</p>
      <h3 className="text-2xl font-bold text-gray-900">{value !== undefined ? value : '...'}</h3>
    </div>
  </div>
);

const Dashboard = () => {
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboard,
  });

  const stats = data?.stats || {};
  const recentEvents = data?.recentEvents || [];
  const systemStatus = data?.systemStatus || {
    ingestion: 'ONLINE',
    identity: 'ACTIVE',
    aiService: 'STANDBY',
    kafka: 'STANDBY'
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      
      {/* Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
        <StatCard 
          title="Total Patients" 
          value={stats.totalPatients} 
          icon={Users} 
          bgColorClass="bg-blue-50"
          iconColorClass="text-blue-600"
        />
        <StatCard 
          title="Clinical Events" 
          value={stats.clinicalEvents} 
          icon={Activity} 
          bgColorClass="bg-green-50"
          iconColorClass="text-green-600"
        />
        <StatCard 
          title="Clinical Trials" 
          value={stats.clinicalTrials} 
          icon={FlaskConical} 
          bgColorClass="bg-purple-50"
          iconColorClass="text-purple-600"
        />
        <StatCard 
          title="Potential Matches" 
          value={stats.potentialMatches} 
          icon={Target} 
          bgColorClass="bg-amber-50"
          iconColorClass="text-amber-600"
        />
        <StatCard 
          title="Needs Review" 
          value={stats.needsReview} 
          icon={AlertTriangle} 
          bgColorClass="bg-red-50"
          iconColorClass="text-red-600"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Recent Activity Table */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="p-6 border-b border-gray-100 flex justify-between items-center">
            <h3 className="text-lg font-bold text-gray-900">Recent Clinical Events</h3>
            <span className="text-sm text-blue-600 font-medium cursor-pointer hover:underline">View All</span>
          </div>
          
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left text-gray-600">
              <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b border-gray-100">
                <tr>
                  <th className="px-6 py-4">Time</th>
                  <th className="px-6 py-4">Patient</th>
                  <th className="px-6 py-4">Event Type</th>
                  <th className="px-6 py-4">Name</th>
                  <th className="px-6 py-4">Source System</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  [...Array(5)].map((_, i) => (
                    <tr key={i} className="border-b border-gray-50">
                      <td className="px-6 py-4"><div className="h-4 bg-gray-200 rounded animate-pulse w-24"></div></td>
                      <td className="px-6 py-4"><div className="h-4 bg-gray-200 rounded animate-pulse w-32"></div></td>
                      <td className="px-6 py-4"><div className="h-4 bg-gray-200 rounded animate-pulse w-20"></div></td>
                      <td className="px-6 py-4"><div className="h-4 bg-gray-200 rounded animate-pulse w-40"></div></td>
                      <td className="px-6 py-4"><div className="h-4 bg-gray-200 rounded animate-pulse w-24"></div></td>
                    </tr>
                  ))
                ) : recentEvents.length === 0 ? (
                   <tr>
                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                      No recent clinical events found.
                    </td>
                  </tr>
                ) : (
                  recentEvents.map((event, i) => (
                    <tr key={i} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">{format(new Date(event.timestamp || Date.now()), 'MMM d, HH:mm')}</td>
                      <td className="px-6 py-4 font-medium text-gray-900">{event.patientName}</td>
                      <td className="px-6 py-4">
                        <span className="bg-gray-100 text-gray-800 text-xs px-2 py-1 rounded-md font-medium">
                          {event.resourceType}
                        </span>
                      </td>
                      <td className="px-6 py-4 truncate max-w-xs">{event.name}</td>
                      <td className="px-6 py-4">
                        <span className="flex items-center gap-1.5 text-xs font-medium text-indigo-700 bg-indigo-50 px-2 py-1 rounded-md w-fit">
                          <Database className="w-3 h-3" />
                          {event.sourceSystem}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* System Status */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100">
          <div className="p-6 border-b border-gray-100">
            <h3 className="text-lg font-bold text-gray-900">System Status</h3>
          </div>
          
          <div className="p-6 space-y-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
                  <Database className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">Data Ingestion</p>
                  <p className="text-xs text-gray-500">FHIR R4 Endpoint</p>
                </div>
              </div>
              <span className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                systemStatus.ingestion === 'ONLINE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
              }`}>
                {systemStatus.ingestion}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                  <Users className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">Identity Resolution</p>
                  <p className="text-xs text-gray-500">Master Patient Index</p>
                </div>
              </div>
              <span className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                systemStatus.identity === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
              }`}>
                {systemStatus.identity}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-purple-50 text-purple-600 rounded-lg">
                  <Server className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">AI Service</p>
                  <p className="text-xs text-gray-500">Criteria Extraction</p>
                </div>
              </div>
              <span className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                systemStatus.aiService === 'ACTIVE' || systemStatus.aiService === 'ONLINE' ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
              }`}>
                {systemStatus.aiService}
              </span>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-gray-100 text-gray-600 rounded-lg">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">Event Stream (Kafka)</p>
                  <p className="text-xs text-gray-500">Real-time processing</p>
                </div>
              </div>
              <span className="px-2.5 py-1 text-xs font-bold rounded-full bg-amber-100 text-amber-700">
                {systemStatus.kafka}
              </span>
            </div>

          </div>
        </div>

      </div>
    </div>
  );
};

export default Dashboard;
