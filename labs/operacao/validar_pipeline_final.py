"""Uma execução normal final, sem sobrescrever o histórico #1–#4 nem implantar JAR."""
import base64,hashlib,http.cookiejar,json,sys,urllib.parse,urllib.request
import xml.etree.ElementTree as ET
from lab import ROOT,RUN,config,wait
OUT=ROOT/'docs/evidencias/desafio-6/validacao-final'
def save(name,obj):
    (OUT/name).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def main():
    OUT.mkdir(exist_ok=True)
    if (OUT/'acionamento.json').exists():raise SystemExit('Tentativa final já acionada; preservar resultado, não repetir automaticamente.')
    c=config();base='http://127.0.0.1:'+c['JENKINS_PORT']
    auth=base64.b64encode(('b1admin:'+(RUN/'jenkins_password').read_text().strip()).encode()).decode()
    opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    def api(path,data=None,headers=None):
        req=urllib.request.Request(base+path,data=data,headers={'Authorization':'Basic '+auth,**(headers or {})})
        with opener.open(req,timeout=60) as r:return r.read(),dict(r.headers)
    wait(lambda:api('/api/json'),600)
    source=(RUN/'snapshot/Jenkinsfile').read_text()
    crumb=json.loads(api('/crumbIssuer/api/json')[0]);h={crumb['crumbRequestField']:crumb['crumb'],'Content-Type':'application/xml; charset=UTF-8'}
    job=ET.fromstring(api('/job/b1-ci/config.xml')[0]);job.find('definition/script').text=source
    api('/job/b1-ci/config.xml',ET.tostring(job,encoding='utf-8',xml_declaration=True),h)
    assert ET.fromstring(api('/job/b1-ci/config.xml')[0]).find('definition/script').text==source
    crumb=json.loads(api('/crumbIssuer/api/json')[0]);h={crumb['crumbRequestField']:crumb['crumb']}
    _,headers=api('/job/b1-ci/buildWithParameters?SIMULAR_FALHA_TESTE=false',b'',h)
    queue=urllib.parse.urlparse(headers['Location']).path
    save('acionamento.json',{'fila':queue,'simular_falha':False,'jenkinsfile_sha256':hashlib.sha256(source.encode()).hexdigest(),'tentativas':1})
    run=wait(lambda:json.loads(api(queue+'api/json')[0]).get('executable'),300);number=run['number'];path='/job/b1-ci/'+str(number)+'/'
    print('Build final acionado:',number,flush=True)
    try:
        result=wait(lambda:(lambda x:x if not x['building'] else None)(json.loads(api(path+'api/json')[0])),1500)
        save('build.json',result)
        (OUT/'console.txt').write_bytes(api(path+'consoleText')[0])
        report=json.loads(api(path+'testReport/api/json')[0]);save('junit.json',report)
        snapshot=json.loads(api(path+'artifact/source-manifest.json')[0]);save('snapshot.json',snapshot)
        assert snapshot['files']['Jenkinsfile']==hashlib.sha256(source.encode()).hexdigest()
        assert result['result']=='SUCCESS',result['result']
        assert report['failCount']==0 and report['passCount']==115 and report['skipCount']==0,report
        manifest=json.loads(api(path+'artifact/aprovado/manifest.json')[0]);artifact=api(path+'artifact/aprovado/app.jar')[0]
        assert hashlib.sha256(artifact).hexdigest()==manifest['jar_sha256']
        (RUN/'app-final-aprovado.jar').write_bytes(artifact);save('artefato.json',manifest)
        save('resultado.json',{'build':number,'resultado':'SUCCESS','maven':101,'python':14,'jar_sha256':manifest['jar_sha256'],'implantado':False,'jenkinsfile_final_conferido':True})
        print('SUCCESS: configuração final, 115 testes e JAR aprovado; não implantado.',flush=True)
    except Exception as error:
        for name,url in [('build.json',path+'api/json'),('console.txt',path+'consoleText')]:
            try:(OUT/name).write_bytes(api(url)[0])
            except Exception:pass
        save('resultado.json',{'build':number,'resultado':'inconclusivo ou falhou; consultar build.json','erro':str(error),'tentativas':1})
        raise
if __name__=='__main__':main()
