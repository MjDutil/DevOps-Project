"""Auditoria local: preservação, segredos conhecidos e lista exata para revisão."""
import hashlib,json,subprocess
from lab import *
def valores_secretos(obj):
    if isinstance(obj,dict):
        for k,v in obj.items():
            if isinstance(v,str) and len(v)>=12 and any(t in k.lower() for t in ['senha','password','token']):yield v.encode()
            else:yield from valores_secretos(v)
    elif isinstance(obj,list):
        for v in obj:yield from valores_secretos(v)
def main():
    before=json.loads((OUT/'git-antes.json').read_text());changed=[]
    assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()==before['head']
    for name,digest in before['hashes'].items():
        if hashlib.sha256((ROOT/name).read_bytes()).hexdigest()!=digest:changed.append(name)
    autorizados={'src/main/java/org/example/tdd/ia/IaClient.java','src/main/resources/application.properties'}
    assert set(changed)<=autorizados,changed
    names=subprocess.check_output(['git','ls-files','--cached','--others','--exclude-standard','-z'],cwd=ROOT).decode().split('\0')
    secrets=[(RUN/(name+'_password')).read_bytes().strip() for name in ['jenkins','grafana']]
    cred=ROOT/'labs/iot/.runtime/credenciais.json'
    if cred.exists():secrets+=list(valores_secretos(json.loads(cred.read_text())))
    leaks=[]
    for name in set(names):
        p=ROOT/name
        if not name or not p.is_file():continue
        raw=p.read_bytes()
        if any(secret in raw for secret in secrets):leaks.append(name)
        if b'\n-----BEGIN PRIVATE KEY-----\n' in b'\n'+raw:leaks.append(name)
    assert not leaks,leaks
    status=subprocess.check_output(['git','status','--short'],cwd=ROOT,text=True)
    (OUT/'git-status-final.txt').write_text(status)
    modified=subprocess.check_output(['git','diff','--name-only','HEAD'],cwd=ROOT,text=True).splitlines()
    new=subprocess.check_output(['git','ls-files','--others','--exclude-standard'],cwd=ROOT,text=True).splitlines()
    (OUT/'arquivos-d6.txt').write_text('\n'.join(sorted(set(modified+new)))+'\n')
    diff=subprocess.run(['git','diff','--check'],cwd=ROOT,capture_output=True,text=True)
    (OUT/'git-diff-check.txt').write_text('Comando: git diff --check\nExit code: '+str(diff.returncode)+'\n'+diff.stdout+diff.stderr)
    assert diff.returncode==0
    save('auditoria-preservacao.json',{'arquivos_historicos_conferidos':len(before['hashes']),'historicos_modificados_com_autorizacao_D6':changed,'historicos_preservados':len(before['hashes'])-len(changed),'segredos_gerados_encontrados_em_arquivos_publicaveis':leaks,'diff_check':True,'commit_push_merge_realizados_no_D6':False})
    print('PASSOU:',len(before['hashes']),'arquivos históricos conferidos; somente instrumentação/configuração D6 alteradas; segredos conhecidos ausentes; diff --check limpo.')
if __name__=='__main__':main()
