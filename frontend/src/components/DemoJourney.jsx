import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  ArrowRight, BrainCircuit, Building2, CheckCircle2, Database,
  FileSearch, FileText, GitMerge, ListChecks, ScanSearch, ShieldCheck,
  Sparkles, Target, UserCheck
} from 'lucide-react';
import { getPatients, getTrials, getReviewQueue, getAuditLogs } from '../api/client';

const steps = [
  { n: '01', title: 'One patient, two hospitals', text: 'Open John Smith to reveal four source identities linked to one canonical patient.', icon: Building2, to: '/patients', cta: 'Find John Smith' },
  { n: '02', title: 'Resolve identity safely', text: 'Show the Master Patient Index and confirmed source-to-patient mappings.', icon: GitMerge, to: '/patients', cta: 'Open identity map' },
  { n: '03', title: 'Unify the clinical timeline', text: 'HbA1c, diagnosis, eGFR, medications and labs arrive from independent systems.', icon: Database, to: '/patients', cta: 'Inspect evidence' },
  { n: '04', title: 'Upload a trial protocol', text: 'PDFBox converts a real-looking protocol PDF into reviewable eligibility text.', icon: FileText, to: '/trials', cta: 'Open trial workspace' },
  { n: '05', title: 'AI structures the criteria', text: 'The AI service extracts inclusion and exclusion rules; regex provides a resilient fallback.', icon: BrainCircuit, to: '/trials', cta: 'View extracted rules' },
  { n: '06', title: 'Run deterministic matching', text: 'Clinical facts are evaluated by a rules engine—not by an opaque AI verdict.', icon: Target, to: '/trials', cta: 'Run a match' },
  { n: '07', title: 'Explain every decision', text: 'Each criterion shows required value, patient value, outcome and plain-language reasoning.', icon: ListChecks, to: '/trials', cta: 'Open explanation' },
  { n: '08', title: 'Verify the original source', text: 'Drill into the exact FHIR event, source system, record ID and timestamp.', icon: FileSearch, to: '/patients', cta: 'Trace provenance' },
  { n: '09', title: 'Escalate uncertainty', text: 'Missing eGFR and ambiguous evidence go to a clinician instead of becoming a false answer.', icon: ScanSearch, to: '/review-queue', cta: 'Open review queue' },
  { n: '10', title: 'Human decision, full audit', text: 'Approve or reject with notes, then prove who did what and when.', icon: UserCheck, to: '/audit', cta: 'View audit trail' }
];

export default function DemoJourney() {
  const { data: patients } = useQuery({ queryKey: ['patients', 'workspace'], queryFn: () => getPatients('', 0, 50) });
  const { data: trials } = useQuery({ queryKey: ['trials'], queryFn: getTrials });
  const { data: review } = useQuery({ queryKey: ['review-queue'], queryFn: getReviewQueue });
  const { data: audit } = useQuery({ queryKey: ['audit-logs', 0], queryFn: () => getAuditLogs(0, 20) });

  const metrics = [
    ['Patient profiles', patients?.totalElements ?? '—'],
    ['Source records', patients?.content?.reduce((n, p) => n + (p.eventCount || 0), 0) ?? '—'],
    ['Active trials', trials?.length ?? '—'],
    ['Awaiting review', review?.length ?? '—'],
    ['Audited actions', audit?.totalElements ?? '—']
  ];

  return (
    <div className="space-y-8 pb-12">
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-950 via-blue-950 to-indigo-900 p-8 lg:p-12 text-white shadow-2xl">
        <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-cyan-400/20 blur-3xl" />
        <div className="absolute bottom-0 left-1/3 h-48 w-48 rounded-full bg-violet-500/20 blur-3xl" />
        <div className="relative max-w-4xl">
          <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-cyan-300/25 bg-cyan-300/10 px-4 py-2 text-xs font-bold tracking-[0.2em] text-cyan-200">
            <Sparkles className="h-4 w-4" /> CLINICAL INTELLIGENCE WORKSPACE
          </div>
          <h1 className="text-4xl font-black leading-tight lg:text-6xl">From fragmented records to an explainable trial match.</h1>
          <p className="mt-5 max-w-3xl text-lg leading-relaxed text-blue-100">ClinicalTrialIQ unifies patient identities, understands protocol documents, evaluates clinical evidence, and keeps a human accountable for uncertain decisions.</p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/patients" className="inline-flex items-center gap-2 rounded-xl bg-cyan-400 px-5 py-3 font-bold text-slate-950 shadow-lg shadow-cyan-500/20 hover:bg-cyan-300">Start with John Smith <ArrowRight className="h-4 w-4" /></Link>
            <Link to="/trials" className="inline-flex items-center gap-2 rounded-xl border border-white/20 bg-white/10 px-5 py-3 font-bold text-white hover:bg-white/15">Open matching lab</Link>
          </div>
        </div>
      </section>

      <section className="grid grid-cols-2 gap-3 lg:grid-cols-5">
        {metrics.map(([label, value]) => <div key={label} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"><div className="text-3xl font-black text-slate-950">{value}</div><div className="mt-1 text-xs font-bold uppercase tracking-wider text-slate-500">{label}</div></div>)}
      </section>

      <section>
        <div className="mb-5 flex items-end justify-between"><div><p className="text-xs font-bold tracking-[0.2em] text-blue-600">END-TO-END WORKFLOW</p><h2 className="mt-1 text-2xl font-black text-slate-950">Ten connected intelligence capabilities</h2></div><div className="hidden items-center gap-2 text-sm font-semibold text-emerald-700 md:flex"><CheckCircle2 className="h-4 w-4" /> Governed data, deterministic decisions</div></div>
        <div className="grid gap-4 md:grid-cols-2">
          {steps.map(({ n, title, text, icon: Icon, to, cta }) => (
            <article key={n} className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-lg">
              <div className="flex gap-4">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-slate-950 text-white"><Icon className="h-5 w-5" /></div>
                <div className="min-w-0 flex-1"><div className="text-xs font-black tracking-[0.18em] text-blue-600">STEP {n}</div><h3 className="mt-1 text-lg font-black text-slate-900">{title}</h3><p className="mt-1 text-sm leading-relaxed text-slate-600">{text}</p><Link to={to} className="mt-3 inline-flex items-center gap-1 text-sm font-bold text-blue-700 hover:text-blue-900">{cta} <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" /></Link></div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="grid gap-4 lg:grid-cols-3">
        {[['AI assists', 'Protocol language becomes structured, reviewable criteria.', BrainCircuit], ['Rules decide', 'Deterministic logic evaluates facts consistently.', Target], ['Humans govern', 'Uncertainty is escalated and every action is audited.', ShieldCheck]].map(([title, text, Icon]) => <div key={title} className="rounded-2xl bg-slate-900 p-6 text-white"><Icon className="h-6 w-6 text-cyan-300" /><h3 className="mt-4 text-lg font-black">{title}</h3><p className="mt-2 text-sm leading-relaxed text-slate-300">{text}</p></div>)}
      </section>
    </div>
  );
}
