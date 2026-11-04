"""Inicia o JAR com H2 e token local em variável de ambiente, sem imprimir segredo."""
import argparse
import json
import os
from pathlib import Path

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--port",type=int,default=8080)
    args=parser.parse_args()
    root=Path(__file__).resolve().parents[2]
    cred=json.loads((Path(__file__).parent/".runtime/credenciais.json").read_text())
    env=dict(os.environ,SPRING_PROFILES_ACTIVE="iot-lab",SERVER_PORT=str(args.port),IOT_TOKEN=cred["core_token"],
             SPRING_DATASOURCE_URL="jdbc:h2:mem:d5manual",SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver",
             SPRING_DATASOURCE_USERNAME="sa",SPRING_DATASOURCE_PASSWORD="",SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop",
             NOTIFICACOES_HABILITADAS="false")
    os.chdir(root)
    os.execvpe("java",["java","-jar","target/TDD-0.0.1-SNAPSHOT.jar"],env)
