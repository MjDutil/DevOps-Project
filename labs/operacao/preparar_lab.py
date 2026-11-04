"""Prepara laboratório isolado; não inicia serviços e não publica segredos."""
import hashlib,json,os,secrets,shutil,socket,subprocess,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
LAB=ROOT/'labs/operacao'; RUN=LAB/'.runtime'
def main():
    RUN.mkdir(exist_ok=True,mode=0o700); os.chmod(RUN,0o700)
    if '--refresh-snapshot' in sys.argv:
        atualizar_snapshot();return
    if (RUN/'config.json').exists():
        raise SystemExit('Ambiente já preparado. Reutilize config.json; não sobrescreva volumes ou credenciais.')
    lock=json.loads((LAB/'images.lock.json').read_text())
    conf={'LAB_ID':secrets.token_hex(4),'JENKINS_BASE':lock['jenkins']['image'],'MAVEN_BASE':lock['maven']['image'],'JAVA_BASE':lock['java_runtime']['image'],'PYTHON_BASE':lock['python']['image'],'PROMETHEUS_IMAGE':lock['prometheus']['image'],'GRAFANA_IMAGE':lock['grafana']['image']}
    sockets=[]
    for key in ['JENKINS_PORT','CORE_PORT','PROMETHEUS_PORT','GRAFANA_PORT']:
        s=socket.socket();s.bind(('127.0.0.1',0));sockets.append(s);conf[key]=str(s.getsockname()[1])
    conf['PROJECT']='b1-d6-'+conf['LAB_ID']
    for name in ['jenkins','grafana']:
        p=RUN/(name+'_password');p.write_text(secrets.token_urlsafe(32));os.chmod(p,0o444)
    (RUN/'config.json').write_text(json.dumps(conf,indent=2)+'\n');os.chmod(RUN/'config.json',0o600)
    files=atualizar_snapshot()
    (RUN/'deploy').mkdir()
    for s in sockets:s.close()
    print('Preparado',conf['PROJECT'],'com',len(files),'arquivos; credenciais somente em .runtime ignorado.')
def atualizar_snapshot():
    dest=RUN/'snapshot'
    dest.mkdir(exist_ok=True)
    for child in dest.iterdir():
        if child.is_dir():shutil.rmtree(child)
        else:child.unlink()
    names=subprocess.check_output(['git','ls-files','-z','--cached','--others','--exclude-standard'],cwd=ROOT).decode().split('\0')
    files={}
    for name in sorted(set(names)):
        p=ROOT/name
        if not name or not p.is_file() or p.is_symlink() or name.startswith('docs/evidencias/') or name.startswith('labs/operacao/.runtime/'):
            continue
        q=dest/name;q.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,q)
        files[name]=hashlib.sha256(q.read_bytes()).hexdigest()
    manifest={'base_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'files':files,'snapshot_sha256':hashlib.sha256(json.dumps(files,sort_keys=True,separators=(',',':')).encode()).hexdigest()}
    (dest/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    return files
if __name__=='__main__':main()
