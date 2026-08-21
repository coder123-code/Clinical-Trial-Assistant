import React from 'react';
export default function Avatar({ name='Research user', size='md', className='' }) {
  const sizes={sm:'h-9 w-9',md:'h-12 w-12',lg:'h-20 w-20'};
  const seed=encodeURIComponent(name.toLowerCase().replace(/\s+/g,'-'));
  return <img src={`https://api.dicebear.com/9.x/notionists/svg?seed=${seed}&backgroundColor=e0f2fe,dbeafe,ede9fe&radius=50`} alt={`${name} profile`} className={`${sizes[size]||sizes.md} rounded-full border-2 border-white bg-sky-100 object-cover shadow-sm ${className}`} loading="lazy" />;
}
