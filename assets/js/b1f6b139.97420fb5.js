"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[1],{3905:function(e,t,n){n.d(t,{Zo:function(){return c},kt:function(){return m}});var s=n(7294);function l(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var s=Object.getOwnPropertySymbols(e);t&&(s=s.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,s)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){l(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,s,l=function(e,t){if(null==e)return{};var n,s,l={},r=Object.keys(e);for(s=0;s<r.length;s++)n=r[s],t.indexOf(n)>=0||(l[n]=e[n]);return l}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(s=0;s<r.length;s++)n=r[s],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(l[n]=e[n])}return l}var u=s.createContext({}),o=function(e){var t=s.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},c=function(e){var t=o(e.components);return s.createElement(u.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return s.createElement(s.Fragment,{},t)}},d=s.forwardRef((function(e,t){var n=e.components,l=e.mdxType,r=e.originalType,u=e.parentName,c=i(e,["components","mdxType","originalType","parentName"]),d=o(n),m=l,y=d["".concat(u,".").concat(m)]||d[m]||p[m]||r;return n?s.createElement(y,a(a({ref:t},c),{},{components:n})):s.createElement(y,a({ref:t},c))}));function m(e,t){var n=arguments,l=t&&t.mdxType;if("string"==typeof e||l){var r=n.length,a=new Array(r);a[0]=d;var i={};for(var u in t)hasOwnProperty.call(t,u)&&(i[u]=t[u]);i.originalType=e,i.mdxType="string"==typeof e?e:l,a[1]=i;for(var o=2;o<r;o++)a[o]=n[o];return s.createElement.apply(null,a)}return s.createElement.apply(null,n)}d.displayName="MDXCreateElement"},2969:function(e,t,n){var s=n(7294);t.Z=function(e){var t=e.children,n=e.color;return s.createElement("span",{style:{backgroundColor:n,borderRadius:"10px",color:"#fff",padding:"5px"}},t)}},2431:function(e,t,n){n.r(t),n.d(t,{assets:function(){return c},contentTitle:function(){return u},default:function(){return m},frontMatter:function(){return i},metadata:function(){return o},toc:function(){return p}});var s=n(7462),l=n(3366),r=(n(7294),n(3905)),a=(n(2969),["components"]),i={sidebar_position:4},u="Sync",o={unversionedId:"whatsub/sync",id:"whatsub/sync",title:"Sync",description:"Solve Out of Sync",source:"@site/docs/whatsub/sync.mdx",sourceDirName:"whatsub",slug:"/whatsub/sync",permalink:"/docs/whatsub/sync",draft:!1,tags:[],version:"current",lastUpdatedAt:1676199294,formattedLastUpdatedAt:"2/12/2023",sidebarPosition:4,frontMatter:{sidebar_position:4},sidebar:"docsSidebar",previous:{title:"Convert Subtitles",permalink:"/docs/whatsub/convert-sub"}},c={},p=[{value:"Solve Out of Sync",id:"solve-out-of-sync",level:2},{value:"Sync",id:"sync-1",level:2},{value:"TIME",id:"time",level:3},{value:"Examples",id:"examples",level:3},{value:"Sync SMI",id:"sync-smi",level:2},{value:"Forwards",id:"forwards",level:3},{value:"Backwards",id:"backwards",level:3},{value:"Sync SRT",id:"sync-srt",level:2},{value:"Forwards",id:"forwards-1",level:3},{value:"Backwards",id:"backwards-1",level:3},{value:"Help",id:"help",level:2}],d={toc:p};function m(e){var t=e.components,n=(0,l.Z)(e,a);return(0,r.kt)("wrapper",(0,s.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"sync"},"Sync"),(0,r.kt)("h2",{id:"solve-out-of-sync"},"Solve Out of Sync"),(0,r.kt)("p",null,"Is your subtitle file our of sync? Whatsub can solve it."),(0,r.kt)("h2",{id:"sync-1"},"Sync"),(0,r.kt)("p",null,"Syncing subtitles is easy just run ",(0,r.kt)("inlineCode",{parentName:"p"},"whatsub sync")," with ",(0,r.kt)("inlineCode",{parentName:"p"},"--sync")," (or ",(0,r.kt)("inlineCode",{parentName:"p"},"-m")," for short) option."),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"--sync +TIME")," for moving forwards and ",(0,r.kt)("inlineCode",{parentName:"p"},"--sync -TIME")," for moving backwards"),(0,r.kt)("h3",{id:"time"},"TIME"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"[NUMBER]h"),": e.g.) ",(0,r.kt)("inlineCode",{parentName:"li"},"2h")," for 2 hours"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"[NUMBER]m"),": e.g.) ",(0,r.kt)("inlineCode",{parentName:"li"},"15m")," for 15 minutes"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"[NUMBER]s"),": e.g.) ",(0,r.kt)("inlineCode",{parentName:"li"},"30s")," for 30 seconds"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"[NUMBER]"),": e.g.) ",(0,r.kt)("inlineCode",{parentName:"li"},"250")," for 250 milliseconds")),(0,r.kt)("h3",{id:"examples"},"Examples"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"1h2m45s200"),": 1 hour 2 minutes 35 seconds and 200 milliseconds"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"3m7s500"),": 3 minutes 7 seconds and 500 milliseconds"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"1m200"),": 1 minute 200 milliseconds"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"800"),": 800 milliseconds")),(0,r.kt)("h2",{id:"sync-smi"},"Sync SMI"),(0,r.kt)("h3",{id:"forwards"},"Forwards"),(0,r.kt)("p",null,"Sync SMI subtitles 5 seconds forwards."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sync +5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -m +5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or if you want to specify the subtitle type explicitly, you can do so like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sub-type smi --sync +5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -t smi -m +5s sub.smi sub-synced.smi\n")),(0,r.kt)("h3",{id:"backwards"},"Backwards"),(0,r.kt)("p",null,"Sync SMI subtitles 5 seconds backwards."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sync -5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -m -5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or if you want to specify the subtitle type explicitly, you can do so like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sub-type smi --sync -5s sub.smi sub-synced.smi\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -t smi -m -5s sub.smi sub-synced.smi\n")),(0,r.kt)("h2",{id:"sync-srt"},"Sync SRT"),(0,r.kt)("h3",{id:"forwards-1"},"Forwards"),(0,r.kt)("p",null,"Sync SRT subtitles 5 seconds forwards."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sync +5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -m +5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or if you want to specify the subtitle type explicitly, you can do so like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sub-type srt --sync +5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -t srt -m +5s sub.srt sub-synced.srt\n")),(0,r.kt)("h3",{id:"backwards-1"},"Backwards"),(0,r.kt)("p",null,"Sync SRT subtitles 5 seconds backwards."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sync -5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -m -5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or if you want to specify the subtitle type explicitly, you can do so like"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --sub-type srt --sync -5s sub.srt sub-synced.srt\n")),(0,r.kt)("p",null,"Or"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync -t srt -m -5s sub.srt sub-synced.srt\n")),(0,r.kt)("h2",{id:"help"},"Help"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"whatsub sync --help\n")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-shell"},"Usage:\n  Whatsub sync [-t|--sub-type <sub-type>] -m|--sync <sync> <src> [<out>] [-h|--help HELP]\n\nsync subtitles\n\nAvailable options:\n  -t|--sub-type <sub-type> A type of subtitle. Either smi or srt. Optional. If\n                          missing, it gets the sub-type from the extension of the src file.\n  -m|--sync <sync>        resync playtime (e.g. shift 1 hour 12 minutes 3\n                          seconds 100 milliseconds forward: +1h12m3s100\n  -h|--help HELP          Prints the synopsis and a list of options and arguments.\n\nPositional arguments:\n  <src>                   The source subtitle file\n  <out>                   An optional output subtitle file. If missing, the result is printed\n                          out.\n\n")))}m.isMDXComponent=!0}}]);