"""Comprova identidade entre archive Jenkins, contexto de deploy e JAR no Core."""
import hashlib,json,subprocess
from lab import *
def main():
    m=json.loads((OUT/'artefato-aprovado.json').read_text());expected=m['jar_sha256']
    assert hashlib.sha256((RUN/'deploy/app.jar').read_bytes()).hexdigest()==expected
    cid=compose('ps','-q','core',capture_output=True,text=True).stdout.strip()
    digest=subprocess.check_output(['docker','exec',cid,'sha256sum','/app/app.jar'],text=True).split()[0]
    assert digest==expected
    info=json.loads(subprocess.check_output(['docker','inspect',cid]))[0]
    save('implantacao-verificada.json',{'jar_jenkins_sha256':expected,'jar_contexto_sha256':expected,'jar_implantado_sha256':digest,'iguais':True,'image_id':info['Image'],'container_id':cid,'base_commit':m['base_commit'],'snapshot_sha256':m['snapshot_sha256'],'jenkins_build':m['jenkins_build'],'tipo':'implantação local controlada; sem recompilação'})
    print('PASSOU: mesmo JAR aprovado e implantado:',digest)
if __name__=='__main__':main()
