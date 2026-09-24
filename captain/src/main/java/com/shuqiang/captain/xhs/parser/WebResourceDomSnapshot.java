package com.shuqiang.captain.xhs.parser;

/** 只读取已有 DOM 元数据；不得发请求、触发图片解码或修改网页加载策略。 */
final class WebResourceDomSnapshot {
    private WebResourceDomSnapshot() {}

    private static final String TEMPLATE =
            "(function(){var __videosOnly=__CAPTAIN_VIDEO_ONLY__;"
            + "var out=[],seen={},playing=[];"
            + "function absolute(u){if(!u||typeof u!=='string'){return '';}try{return new URL(u.replace(/&amp;/g,'&'),document.baseURI).href;}catch(e){return '';}}"
            + "function add(u,k,m,w,h,sw,sh,visible,ready,hints){u=absolute(u);if(!/^https?:\\/\\//i.test(u)||seen[u]){return;}seen[u]=true;out.push({url:u,kind:k,mime:m||'',width:w||0,height:h||0,sourceWidth:sw||0,sourceHeight:sh||0,visible:!!visible,ready:!!ready,hints:hints||''});}"
            + "function box(e){var r=e.getBoundingClientRect?e.getBoundingClientRect():null;return {width:Math.round(r?r.width:(e.width||0)),height:Math.round(r?r.height:(e.height||0))};}"
            + "var imgs=__videosOnly?[]:document.querySelectorAll('img');"
            + "for(var i=0;i<imgs.length&&i<240;i++){"
            + "var e=imgs[i],b=box(e),s=window.getComputedStyle?getComputedStyle(e):null;"
            + "var visible=(!s||(s.display!=='none'&&s.visibility!=='hidden'&&s.opacity!=='0'))&&b.width>0&&b.height>0;"
            + "var ok=!!e.complete&&(e.naturalWidth||0)>0,loaded=ok?absolute(e.currentSrc||e.src):'';"
            + "var hints=(e.className||'')+' '+(e.alt||'')+' '+(s?s.filter:'');"
            + "var urls=[e.currentSrc,e.src,e.getAttribute('data-src'),e.getAttribute('data-original'),e.getAttribute('data-lazy-src')];"
            + "var sets=[e.getAttribute('srcset'),e.getAttribute('data-srcset')];"
            + "for(var z=0;z<sets.length;z++){if(sets[z]){var parts=sets[z].split(',');for(var n=0;n<parts.length;n++){urls.push(parts[n].trim().split(/\\s+/)[0]);}}}"
            + "for(var j=0;j<urls.length;j++){var ready=ok&&absolute(urls[j])===loaded;add(urls[j],'image','',b.width,b.height,ready?e.naturalWidth:0,ready?e.naturalHeight:0,visible,ready,hints);}"
            + "}"
            + "var videos=document.querySelectorAll('video,video source');"
            + "for(var x=0;x<videos.length&&x<80;x++){var m=videos[x],host=m.tagName&&m.tagName.toLowerCase()==='source'?m.parentElement:m;if(!host){continue;}if(!host.paused&&host.readyState>=2&&host.currentSrc){playing.push(absolute(host.currentSrc));}var vb=box(host);var urls=[m.currentSrc,m.src,m.getAttribute('src'),m.getAttribute('data-src')];for(var q=0;q<urls.length;q++){add(urls[q],'video',m.getAttribute('type'),vb.width,vb.height,host.videoWidth,host.videoHeight,true,host.readyState>=2,m.className);}}"
            + "var links=__videosOnly?[]:document.querySelectorAll('a[href$=\".pdf\"],embed[type=\"application/pdf\"],object[type=\"application/pdf\"]');"
            + "for(var y=0;y<links.length&&y<40;y++){var p=links[y];add(p.href||p.src||p.data,'pdf','application/pdf',0,0,0,0,true,false,p.className);}"
            + "return JSON.stringify({title:document.title||'',items:out,playingUrls:playing});"
            + "})()";
    static final String SCRIPT = TEMPLATE.replace("__CAPTAIN_VIDEO_ONLY__", "false");
    static final String VIDEO_SCRIPT = TEMPLATE.replace("__CAPTAIN_VIDEO_ONLY__", "true");
}
