"""Experimento real: IA disponível, indisponível e recuperada; consultas Prometheus."""
import urllib.parse
from lab import *
def main():
    c=config();core='http://127.0.0.1:'+c['CORE_PORT'];prom='http://127.0.0.1:'+c['PROMETHEUS_PORT'];graf='http://127.0.0.1:'+c['GRAFANA_PORT'];password=(RUN/'grafana_password').read_text().strip()
    events=[]
    core_id_inicial=compose('ps','-q','core',capture_output=True,text=True).stdout.strip()
    def call(path,data=None):
        status,raw=request(core+path,data);body=json.loads(raw);events.append({'path':path,'status':status,'body':body});assert status==200,(status,body);return body
    def query(expr):
        status,raw=request(prom+'/api/v1/query?'+urllib.parse.urlencode({'query':expr}));assert status==200
        return json.loads(raw)
    def counters():
        r=query('ia_fallback_total');return {s['metric']['operacao']:float(s['value'][1]) for s in r['data']['result']}
    wait(lambda:request(core+'/ia/recomendacoes/1')[0]==200,240)
    wait(lambda:query('up{job="core"}')['data']['result'][0]['value'][1]=='1')
    # Primeiras chamadas não podem usar fallback: aguarda o FastAPI pronto.
    wait(lambda:bool(json.loads(request(core+'/ia/recomendacoes/1')[1]).get('recomendacoes')))
    phases=[]
    def record(name):
        wait(lambda:len(counters())==2)
        r={'fase':name,'timestamp':time.time(),'contadores':counters(),'prometheus_up':query('up{job="core"}'),'prometheus_fallback':query('ia_fallback_total'),'prometheus_http':query('http_server_requests_seconds_count')};phases.append(r);save('metricas-'+name+'.json',r)
        request(graf+'/api/annotations',{'dashboardUID':'b1-d6','time':int(time.time()*1000),'tags':['d6'],'text':name},auth=password)
        return r['contadores']
    # Pode haver fallback de prontidão; o valor inicial é registrado, não suposto zero.
    assert call('/ia/recomendacoes/1')['recomendacoes']
    assert call('/ia/assistente?pergunta=laboratorio').get('status')!='indisponivel'
    initial=record('inicial')
    compose('stop','ia')
    for _ in range(3):assert call('/ia/recomendacoes/1')['status']=='indisponivel'
    for _ in range(2):assert call('/ia/assistente?pergunta=laboratorio')['status']=='indisponivel'
    aluno=call('/alunos',{'media':8});concluido=call('/alunos/'+str(aluno['id'])+'/concluir-curso',{})
    assert concluido['cursosLiberados']==aluno['cursosLiberados']+3
    call('/alunos/'+str(aluno['id'])+'/verificar-premium',{})
    expected={k:v+({'recomendacao':3,'assistente':2}[k]) for k,v in initial.items()}
    wait(lambda:counters()==expected)
    record('ia-indisponivel')
    # Uma pausa opcional permite capturar a UI antes da recuperação.
    (RUN/'fase.txt').write_text('ia-indisponivel')
    if os.environ.get('D6_PAUSAR_CAPTURA')=='1':wait(lambda:(RUN/'continuar').exists(),600)
    compose('start','ia')
    # Aguarda o health diretamente na rede Docker, evitando novos fallbacks de prontidão.
    wait(lambda:compose('exec','-T','ia','python','-c','import urllib.request;urllib.request.urlopen("http://localhost:8000/health")',capture_output=True).returncode==0)
    for _ in range(3):
        assert call('/ia/recomendacoes/1')['recomendacoes'];assert call('/ia/assistente?pergunta=laboratorio').get('status')!='indisponivel'
    time.sleep(6);assert counters()==expected
    record('recuperada');core_id_final=compose('ps','-q','core',capture_output=True,text=True).stdout.strip();assert core_id_inicial==core_id_final
    save('http-experimento.json',events);save('experimento-resumo.json',{'fases':phases,'core_conclusao_preservada':True,'incremento':{'recomendacao':3,'assistente':2},'contador_estavel_apos_recuperacao':True,'core_container_id_inicial':core_id_inicial,'core_container_id_final':core_id_final})
    status,raw=request(graf+'/api/dashboards/uid/b1-d6',auth=password);assert status==200;save('grafana-dashboard-real.json',json.loads(raw))
    status,raw=request(prom+'/api/v1/targets');assert status==200;save('prometheus-targets.json',json.loads(raw))
    print('PASSOU: fallback +3/+2, Core funcional e recuperação sem novos incrementos.')
if __name__=='__main__':main()
