"""Aciona Jenkins real via API autenticada e recupera somente o JAR aprovado."""
import hashlib,http.cookiejar,json,time,urllib.parse,urllib.request,sys,xml.etree.ElementTree as ET
from lab import *
def main():
    c=config();base='http://127.0.0.1:'+c['JENKINS_PORT'];password=(RUN/'jenkins_password').read_text().strip()
    jar=http.cookiejar.CookieJar();opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
    token=base64.b64encode(('b1admin:'+password).encode()).decode()
    def api(path,data=None,headers=None):
        req=urllib.request.Request(base+path,data=data,headers={'Authorization':'Basic '+token,**(headers or {})})
        with opener.open(req,timeout=30) as r:return r.status,r.read(),dict(r.headers)
    wait(lambda:api('/api/json')[0]==200,600)
    save('jenkins-versao.json',{'headers':{k:v for k,v in api('/api/json')[2].items() if k.lower() in ['x-jenkins']}})
    crumb=json.loads(api('/crumbIssuer/api/json')[1]);headers={crumb['crumbRequestField']:crumb['crumb'],'Content-Type':'application/xml; charset=UTF-8'}
    job=ET.fromstring(api('/job/b1-ci/config.xml')[1]);job.find('definition/script').text=(RUN/'snapshot/Jenkinsfile').read_text()
    api('/job/b1-ci/config.xml',ET.tostring(job,encoding='utf-8',xml_declaration=True),headers)
    results=[]
    if '--somente-bloqueio' in sys.argv:
        approved=json.loads((OUT/'jenkins-aprovado.json').read_text());assert approved['result']=='SUCCESS'
        results.append({'build':approved['number'],'result':'SUCCESS','falha_controlada':False})
    for fail in ([True] if '--somente-bloqueio' in sys.argv else [False,True]):
        crumb=json.loads(api('/crumbIssuer/api/json')[1]);headers={crumb['crumbRequestField']:crumb['crumb']}
        path='/job/b1-ci/buildWithParameters?SIMULAR_FALHA_TESTE='+str(fail).lower()
        try:_,_,h=api(path,b'',headers)
        except urllib.error.HTTPError as error:
            if not fail and error.code in (400,404):_,_,h=api('/job/b1-ci/build',b'',headers)
            else:raise
        q=urllib.parse.urlparse(h['Location']).path
        executable=wait(lambda:json.loads(api(q+'api/json')[1]).get('executable'),300)
        n=executable['number'];build='/job/b1-ci/'+str(n)+'/'
        r=wait(lambda:(lambda x:x if not x['building'] else None)(json.loads(api(build+'api/json')[1])),1200)
        name='bloqueado' if fail else 'aprovado'
        (OUT/('jenkins-'+name+'-console.txt')).write_bytes(api(build+'consoleText')[1])
        save('jenkins-'+name+'.json',r)
        save('snapshot-'+name+'.json',json.loads(api(build+'artifact/source-manifest.json')[1]))
        save('jenkins-'+name+'-junit.json',json.loads(api(build+'testReport/api/json')[1]))
        assert r['result']==('FAILURE' if fail else 'SUCCESS'),r['result']
        if not fail:
            manifest=json.loads(api(build+'artifact/aprovado/manifest.json')[1]);artifact=api(build+'artifact/aprovado/app.jar')[1]
            assert hashlib.sha256(artifact).hexdigest()==manifest['jar_sha256']
            (RUN/'deploy/app.jar').write_bytes(artifact);save('artefato-aprovado.json',manifest)
        else:
            assert not any(a['relativePath'].startswith('aprovado/') for a in r['artifacts'])
        results.append({'build':n,'result':r['result'],'falha_controlada':fail});print(name,r['result'],n,flush=True)
    save('pipeline-resultados.json',results)
if __name__=='__main__':main()
