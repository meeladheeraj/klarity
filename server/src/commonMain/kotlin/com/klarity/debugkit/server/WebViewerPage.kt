package com.klarity.debugkit.server

/**
 * The browser page served at `/`. It's a "dumb terminal": opens a WebSocket to `/events`,
 * receives the full event list as JSON on every change, and re-renders. No state of its own.
 *
 * (Plain string concatenation in the JS — no backticks/`${}` — to avoid clashing with
 * Kotlin's own string templating inside this raw string.)
 */
internal val WEB_VIEWER_HTML: String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<title>Klarity Debug Toolkit — Web Viewer</title>
<style>
 body { font-family: -apple-system, system-ui, sans-serif; margin: 16px; background:#fafafa; color:#222; }
 h2 { font-weight:600; margin-bottom:4px; }
 #status { font-size:12px; color:#999; margin-bottom:12px; }
 table { width:100%; border-collapse:collapse; }
 th,td { text-align:left; padding:6px 10px; border-bottom:1px solid #eee; font-size:14px; }
 th { color:#888; font-weight:600; }
 td.status { font-weight:700; width:48px; }
 .mono { font-family: ui-monospace, Menlo, monospace; }
 .dur { color:#999; }
 tr.row { cursor:pointer; }
 tr.detail td { background:#f4f4f4; white-space:pre-wrap; font-family:ui-monospace,Menlo,monospace; font-size:12px; }
 .s2{color:#2E7D32}.s3{color:#1565C0}.s4{color:#E65100}.s5{color:#C62828}
</style>
</head>
<body>
<h2>Network <span id="count">0</span></h2>
<div id="status">connecting…</div>
<input id="filter" oninput="render(lastEvents)" placeholder="filter by method / url / status / message"
       style="width:100%;box-sizing:border-box;padding:6px 10px;margin:8px 0;border:1px solid #ddd;border-radius:6px;font-size:14px;"/>
<table>
 <thead><tr><th>Status</th><th>Method</th><th>URL</th><th>Time</th></tr></thead>
 <tbody id="rows"></tbody>
</table>
<script>
 var expanded = {};
 var lastEvents = [];
 function cls(code){ if(code==null) return ''; if(code<300) return 's2'; if(code<400) return 's3'; if(code<500) return 's4'; return 's5'; }
 function esc(s){ return (s==null?'':String(s)).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
 function headersText(h){ if(!h) return ''; return Object.keys(h).map(function(k){ return k+': '+h[k]; }).join('\n'); }
 function matches(ev, q){ if(!q) return true; return ((ev.url||'')+' '+(ev.method||'')+' '+(ev.statusCode||'')+' '+(ev.message||'')).toLowerCase().indexOf(q) >= 0; }
 function render(events){
   lastEvents = events;
   var q = (document.getElementById('filter').value || '').toLowerCase();
   var shown = events.filter(function(ev){ return matches(ev, q); });
   document.getElementById('count').textContent = q ? (shown.length + ' / ' + events.length) : events.length;
   var rows = document.getElementById('rows');
   rows.innerHTML = '';
   for (var i=0;i<shown.length;i++){
     var ev = shown[i];
     if (ev.type === 'http'){
       var tr = document.createElement('tr');
       tr.className = 'row';
       tr.innerHTML = '<td class="status '+cls(ev.statusCode)+'">'+esc(ev.statusCode)+'</td>'
         + '<td>'+esc(ev.method)+'</td>'
         + '<td class="mono">'+esc(ev.url)+'</td>'
         + '<td class="dur">'+esc(ev.durationMs)+'ms</td>';
       (function(id){ tr.onclick = function(){ expanded[id] = !expanded[id]; render(lastEvents); }; })(ev.id);
       rows.appendChild(tr);
       if (expanded[ev.id]){
         var d = document.createElement('tr'); d.className = 'detail';
         d.innerHTML = '<td colspan="4">'
           + 'Request headers\n'  + esc(headersText(ev.requestHeaders))
           + '\n\nResponse headers\n' + esc(headersText(ev.responseHeaders))
           + '\n\nResponse body\n' + esc(ev.responseBody || '(none)')
           + '</td>';
         rows.appendChild(d);
       }
     } else if (ev.type === 'log'){
       var tr2 = document.createElement('tr');
       tr2.innerHTML = '<td>LOG</td><td colspan="3" class="mono">'+esc(ev.message)+'</td>';
       rows.appendChild(tr2);
     }
   }
 }
 var ws = new WebSocket('ws://' + location.host + '/events');
 ws.onopen    = function(){ document.getElementById('status').textContent = 'connected — live'; };
 ws.onclose   = function(){ document.getElementById('status').textContent = 'disconnected'; };
 ws.onmessage = function(e){ render(JSON.parse(e.data)); };
</script>
</body>
</html>
""".trimIndent()
