import React from 'react';
import { Sparkles } from 'lucide-react';
export default function AiOrb({compact=false}) { return <div className={`ai-orb ${compact?'ai-orb--compact':''}`} aria-label="AI intelligence active"><span className="ai-orb__ring"/><span className="ai-orb__ring ai-orb__ring--two"/><span className="ai-orb__core"><Sparkles className={compact?'h-4 w-4':'h-6 w-6'}/></span></div>; }
