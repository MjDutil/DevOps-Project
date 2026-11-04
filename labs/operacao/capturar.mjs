// Uso: node labs/operacao/capturar.mjs /tmp/b1-readme-tools [fase]
// Captura telas reais. Segredos lidos do diretório ignorado, nunca enviados aos logs.
import {createRequire} from 'node:module';
import fs from 'node:fs';
import path from 'node:path';
const require=createRequire(path.resolve(process.argv[2],'package.json'));
const puppeteer=require('puppeteer');
const dir='labs/operacao/.runtime/';const c=JSON.parse(fs.readFileSync(dir+'config.json'));
const fase=process.argv[3]||'recuperada';const out='docs/evidencias/desafio-6/';
const browser=await puppeteer.launch({headless:true,timeout:120000,args:['--no-sandbox','--disable-dev-shm-usage']});
try {
const page=await browser.newPage();page.setDefaultNavigationTimeout(120000);page.setDefaultTimeout(60000);const consultas=[];const pendentes=[];
page.on('response',r=>{if(r.url().includes('/api/ds/query'))pendentes.push((async()=>{try{consultas.push({status:r.status(),consulta:JSON.parse(r.request().postData()),resultado:await r.json()})}catch{}})())});
await page.setViewport({width:1500,height:1100,deviceScaleFactor:1});
await page.goto(`http://127.0.0.1:${c.GRAFANA_PORT}/login`,{waitUntil:'networkidle2'});
await page.waitForSelector('input[name="user"]');await page.type('input[name="user"]','b1admin');await page.type('input[name="password"]',fs.readFileSync(dir+'grafana_password','utf8').trim());
await Promise.all([page.waitForNavigation({waitUntil:'networkidle2'}),page.click('button[type="submit"]')]);
await page.goto(`http://127.0.0.1:${c.GRAFANA_PORT}/d/b1-d6/architecture-lab-b1-d6?from=now-15m&to=now&refresh=1m`,{waitUntil:'networkidle2'});
await page.waitForFunction(valor=>{const t=document.body.innerText;return t.includes('\n'+valor+'\n')&&t.includes('assistente')&&t.includes('recomendacao')},{timeout:120000},fase==='inicial'?'0':'5');
await new Promise(r=>setTimeout(r,5000));
await page.bringToFront();
if(await page.$('input[name="password"]'))throw Error('Dashboard não autenticado');
await page.screenshot({path:out+`grafana-${fase}.png`,fullPage:false});
await page.goto(`http://127.0.0.1:${c.PROMETHEUS_PORT}/query?g0.expr=ia_fallback_total&g0.tab=1`,{waitUntil:'networkidle2'});
await new Promise(r=>setTimeout(r,5000));await page.screenshot({path:out+`prometheus-${fase}.png`,fullPage:false});
fs.writeFileSync(out+`captura-${fase}.json`,JSON.stringify({grafana_text:await (async()=>{await page.goto(`http://127.0.0.1:${c.GRAFANA_PORT}/d/b1-d6/architecture-lab-b1-d6`,{waitUntil:'networkidle2'});return await page.evaluate(()=>document.body.innerText)})(),capturado_em:new Date().toISOString()},null,2)+'\n');
await Promise.allSettled(pendentes);fs.writeFileSync(out+`grafana-consultas-${fase}.json`,JSON.stringify(consultas,null,2)+'\n');
if(!consultas.some(q=>q.status===200))throw Error('Nenhuma consulta real bem-sucedida do dashboard');
console.log('Telas reais capturadas:',fase,'consultas:',consultas.length);
}finally{await browser.close()}
