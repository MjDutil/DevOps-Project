// Captura real dos builds já concluídos; sem credenciais em argumentos/logs.
import fs from 'node:fs';import path from 'node:path';import {createRequire} from 'node:module';
const require=createRequire(path.resolve(process.argv[2],'package.json'));const puppeteer=require('puppeteer');
const dir='labs/operacao/.runtime/';const c=JSON.parse(fs.readFileSync(dir+'config.json'));const out='docs/evidencias/desafio-6/';
const browser=await puppeteer.launch({headless:"shell",timeout:120000,args:['--no-sandbox','--disable-dev-shm-usage']});
try{
 const p=await browser.newPage();p.setDefaultNavigationTimeout(120000);p.setDefaultTimeout(60000);await p.setViewport({width:1450,height:1050});
 const base=`http://127.0.0.1:${c.JENKINS_PORT}`;await p.goto(base+'/login',{waitUntil:'networkidle2'});
 await p.type('input[name="j_username"]','b1admin');await p.type('input[name="j_password"]',fs.readFileSync(dir+'jenkins_password','utf8').trim());
 await Promise.all([p.waitForNavigation({waitUntil:'networkidle2'}),p.click('[name="Submit"]')]);
 for(const estado of ['aprovado','bloqueado']){
  const r=JSON.parse(fs.readFileSync(out+`jenkins-${estado}.json`));await p.goto(base+`/job/b1-ci/${r.number}/console`,{waitUntil:'networkidle2'});
  await p.evaluate(()=>window.scrollTo(0,document.body.scrollHeight));await p.screenshot({path:out+`jenkins-${estado}.png`});
 }
 console.log('Capturas reais dos dois builds concluídas.');
}finally{await browser.close()}
