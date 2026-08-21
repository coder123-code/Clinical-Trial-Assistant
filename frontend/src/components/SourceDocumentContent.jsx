import React from 'react';
import { Download, FileJson, FileText, FileType2 } from 'lucide-react';

const formatJson = value => { if (!value) return null; try { return JSON.stringify(JSON.parse(value), null, 2); } catch { return value; } };

export default function SourceDocumentContent({ event }) {
  const format = event?.sourceFormat || (event?.rawSourceText ? 'DOCUMENT' : 'FHIR');
  const isDocument = format !== 'FHIR' && Boolean(event?.rawSourceText);
  const content = isDocument ? event.rawSourceText : formatJson(event?.rawJson);
  const name = event?.sourceDocumentName || `${event?.sourceRecordId || 'source-record'}.${isDocument ? 'txt' : 'json'}`;
  const pdfUrl = format === 'LAB_PDF' && event?.id ? `/api/clinical-events/${event.id}/source-document` : null;
  const download = () => { if (pdfUrl) { const a=document.createElement('a'); a.href=pdfUrl; a.download=name; a.click(); return; } const blob = new Blob([content || 'Source content unavailable'], { type: isDocument ? 'text/plain' : 'application/json' }); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = name; a.click(); URL.revokeObjectURL(url); };
  return <div className="flex min-h-0 flex-1 flex-col bg-slate-950 text-slate-100">
    <div className="flex items-center justify-between border-b border-white/10 bg-slate-900 px-5 py-3"><div className="flex items-center gap-3">{format==='LAB_PDF'?<FileType2 className="h-5 w-5 text-rose-300"/>:isDocument?<FileText className="h-5 w-5 text-cyan-300"/>:<FileJson className="h-5 w-5 text-indigo-300"/>}<div><p className="text-sm font-bold">{name}</p><p className="text-[11px] font-bold uppercase tracking-widest text-slate-400">{format.replaceAll('_',' ')}</p></div></div><button onClick={download} className="flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-xs font-bold hover:bg-white/10"><Download className="h-4 w-4"/> Download source</button></div>
    <div className="flex-1 overflow-auto">{pdfUrl ? <iframe src={pdfUrl} title={name} className="h-[560px] w-full bg-white" /> : content ? <pre className={`p-6 whitespace-pre-wrap text-sm leading-7 ${isDocument?'font-sans text-slate-200':'font-mono text-cyan-50'}`}>{content}</pre> : <div className="grid h-48 place-items-center text-center text-slate-400"><div><FileText className="mx-auto mb-3 h-8 w-8"/><p className="font-bold text-slate-200">Source content is unavailable</p><p className="mt-1 text-sm">Metadata was preserved, but the original payload was not stored.</p></div></div>}</div>
  </div>;
}
