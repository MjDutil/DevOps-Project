"""Utilitários locais. Credenciais não são passadas como argumentos de processos."""
import base64,http.cookiejar,json,os,subprocess,time,urllib.error,urllib.request
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2];LAB=ROOT/'labs/operacao';RUN=LAB/'.runtime';OUT=ROOT/'docs/evidencias/desafio-6'
def config():return json.loads((RUN/'config.json').read_text())
def compose(*args,**kwargs):
    c=config();env={**os.environ,**c}
    return subprocess.run(['docker','compose','-p',c['PROJECT'],'-f',str(LAB/'compose.yml'),*args],env=env,check=True,**kwargs)
def request(url,data=None,auth=None,method=None,headers=None):
    h=dict(headers or {})
    if auth:h['Authorization']='Basic '+base64.b64encode(('b1admin:'+auth).encode()).decode()
    if isinstance(data,dict):data=json.dumps(data).encode();h['Content-Type']='application/json'
    req=urllib.request.Request(url,data=data,headers=h,method=method)
    try:
        with urllib.request.urlopen(req,timeout=20) as r:return r.status,r.read()
    except urllib.error.HTTPError as e:return e.code,e.read()
def wait(fn,timeout=240):
    until=time.monotonic()+timeout;last=None
    while time.monotonic()<until:
        try:
            result=fn()
            if result:return result
        except Exception as e:last=e
        time.sleep(2)
    raise TimeoutError(str(last))
def save(name,obj):
    OUT.mkdir(parents=True,exist_ok=True);(OUT/name).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
if __name__=='__main__':
    import sys
    compose(*sys.argv[1:])
