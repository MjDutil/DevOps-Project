"""Gera segredos locais; usa mosquitto_passwd da mesma imagem do broker.
Não imprime senhas nem as coloca em argumentos de processo. Não sobrescreve estado.
"""
import json
import os
from pathlib import Path
import secrets
import subprocess

ROOT = Path(__file__).resolve().parent
RUNTIME = ROOT / ".runtime"
IMAGE = "eclipse-mosquitto:2.0.22"

def main():
    subprocess.run(["docker", "info"], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=15)
    RUNTIME.mkdir(mode=0o700, exist_ok=True)
    if (RUNTIME/"credenciais.json").exists() or (RUNTIME/"passwords").exists():
        raise SystemExit("Credenciais locais já existem; preservadas. Consulte README para reset explícito.")
    creds = {"device_username":"dispositivo-01", "device_password":secrets.token_urlsafe(24),
             "gateway_username":"gateway-b1", "gateway_password":secrets.token_urlsafe(24),
             "core_token":secrets.token_urlsafe(32)}
    for i,(user,password) in enumerate([(creds["device_username"],creds["device_password"]),(creds["gateway_username"],creds["gateway_password"])]):
        nome="b1-d5-credenciais-"+str(os.getpid())+"-"+str(i)
        cmd=["docker","run","--rm","--name",nome,"-i","-v",str(RUNTIME)+":/runtime",IMAGE,"mosquitto_passwd"]
        if i==0:cmd.append("-c")
        cmd += ["/runtime/passwords",user]
        try:
            subprocess.run(cmd,input=(password+"\n"+password+"\n").encode(),check=True,
                           stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,timeout=60)
        except (subprocess.TimeoutExpired, subprocess.CalledProcessError):
            # Remove somente o container temporário identificado desta execução.
            try:
                subprocess.run(["docker","rm","-f",nome],stdout=subprocess.DEVNULL,
                               stderr=subprocess.DEVNULL,timeout=15)
            except subprocess.TimeoutExpired:
                pass
            (RUNTIME/"passwords").unlink(missing_ok=True)
            raise SystemExit("Preparação de credenciais não terminou; verifique se o Docker inicia containers.")
    (RUNTIME/"passwords").chmod(0o644) # hashes precisam ser lidos pelo usuário mosquitto no container
    fd=os.open(RUNTIME/"credenciais.json",os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    with os.fdopen(fd,"w") as f:json.dump(creds,f,indent=2)
    print("Credenciais geradas somente em labs/iot/.runtime (ignorado pelo Git).")

if __name__=="__main__":main()
