"""Registra versões e isolamento do Jenkins sem consultar segredos."""
import xml.etree.ElementTree as ET
from lab import *
def main():
    cid=compose('ps','-q','jenkins',capture_output=True,text=True).stdout.strip()
    inspect=json.loads(subprocess.check_output(['docker','inspect',cid]))[0]
    assert not any(m['Destination']=='/var/run/docker.sock' for m in inspect['Mounts'])
    cmd=['docker','exec',cid,'sh','-c','java -version 2>&1; /opt/java/jdk17/bin/java -version 2>&1; /usr/share/maven/bin/mvn -version; python3 --version; java -jar /usr/share/jenkins/jenkins.war --version']
    text=subprocess.check_output(cmd,text=True);(OUT/'versoes-jenkins.txt').write_text(text)
    plugins=subprocess.check_output(['docker','exec',cid,'python3','-c',"import glob,zipfile,json;print(json.dumps({p.rsplit('/',1)[-1]:zipfile.ZipFile(p).read('META-INF/MANIFEST.MF').decode() for p in glob.glob('/var/jenkins_home/plugins/*.jpi')}))"],text=True)
    manifests=json.loads(plugins);save('plugins-resolvidos.json',manifests)
    versions={}
    for p,m in manifests.items():
        fields=dict(line.split(': ',1) for line in m.splitlines() if ': ' in line)
        versions[fields.get('Short-Name',p.removesuffix('.jpi'))]=fields.get('Plugin-Version')
    save('plugins-versoes.json',versions)
    (LAB/'jenkins/plugins.lock.txt').write_text('\n'.join(k+':'+v for k,v in sorted(versions.items()))+'\n')
    save('jenkins-isolamento.json',{'socket_docker_montado':False,'portas':inspect['HostConfig']['PortBindings'],'mounts':[{'tipo':m['Type'],'destino':m['Destination']} for m in inspect['Mounts']]})
    print(text)
if __name__=='__main__':main()
