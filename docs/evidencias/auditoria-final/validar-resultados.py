"""Executar após mvn clean verify: python3 docs/evidencias/auditoria-final/validar-resultados.py."""
from pathlib import Path
import hashlib
import json
import subprocess
import xml.etree.ElementTree as E

raiz = Path(__file__).resolve().parents[3]
saida = Path(__file__).resolve().parent
relatorios = raiz / 'target/surefire-reports'
log = (saida / 'mvn-clean-verify.txt').read_text()
assert 'BUILD SUCCESS' in log
suites = [E.parse(p).getroot() for p in sorted(relatorios.glob('TEST-*.xml'))]
assert sum(int(s.attrib['tests']) for s in suites) == 75
assert all(int(s.attrib[k]) == 0 for s in suites for k in ['failures', 'errors', 'skipped'])
xml = E.parse(raiz / 'target/site/jacoco/jacoco.xml').getroot()
contadores = {c.attrib['type']: {k: int(c.attrib[k]) for k in ['missed', 'covered']}
              for c in xml.findall('counter')}
assert all(c['missed'] == 0 for c in contadores.values())
assert [contadores[k]['covered'] for k in ['INSTRUCTION', 'BRANCH', 'LINE']] == [669, 16, 160]
antes = E.parse(raiz / 'docs/evidencias/cobertura/antes-jacoco.xml').getroot()
assert {c.attrib['name'] for c in antes.findall('./package/class')} == {
    c.attrib['name'] for c in xml.findall('./package/class')}

def casos(nome):
    r = E.parse(relatorios / ('TEST-org.example.tdd.gamificacao.' + nome + '.xml')).getroot()
    return {c.attrib['name']: not any(c.find(t) is not None for t in ['failure', 'error', 'skipped'])
            for c in r.findall('testcase')}

fake = casos('FakeAlunoStoreContractTest')
jpa = casos('JpaAlunoStoreContractTest')
assert len(fake) == 11 and len(jpa) == 14
assert set(fake).issubset(jpa)
assert all(fake.values()) and all(jpa.values())
rows = ['# Substituição dos adapters — evidência da auditoria final', '',
        'Os mesmos métodos herdados de `AlunoStoreContract` são executados pelos dois adapters.',
        'O caso de uso e `domain.Aluno` são os mesmos. Somente a composição/fixture de persistência muda.', '',
        '| Cenário comum (nome real no Surefire) | Fake | JPA/H2 |', '|---|---|---|']
for nome in sorted(fake):
    rows.append(f'| `{nome}` | PASSOU | PASSOU |')
extras = sorted(set(jpa) - set(fake))
rows += ['', 'Os três cenários adicionais do adapter JPA também passaram:']
rows += ['- `' + n + '`.' for n in extras]
rows += ['', 'Fonte: relatórios XML de `target/surefire-reports` da execução limpa cujo log acompanha esta pasta.',
         'Os parâmetros do método de média correspondem a `8.0 → 5`, `7.0 → 2`, `6.5 → 2`, com saldo inicial 2.',
         'Os números por adapter não são novos testes além dos 75; são um recorte dessa mesma execução.']
(saida / 'substituicao-adapters.md').write_text('\n'.join(rows) + '\n')
dominio = 'src/main/java/org/example/tdd/domain/Aluno.java'
original = subprocess.check_output(['git', 'show', '8bf8785:' + dominio], cwd=raiz)
atual = (raiz / dominio).read_bytes()
assert original == atual
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
pom = E.parse(raiz / 'pom.xml').getroot()
for plugin in pom.findall('./m:build/m:plugins/m:plugin', ns):
    if plugin.findtext('m:artifactId', namespaces=ns) == 'jacoco-maven-plugin':
        assert not plugin.findall('.//m:excludes', ns)
resumo = {'data': '2026-10-27', 'comando': 'mvn clean verify', 'resultado': 'BUILD SUCCESS',
          'testes': [{'classe': s.attrib['name'], 'testes': int(s.attrib['tests']),
                      'falhas': int(s.attrib['failures']), 'erros': int(s.attrib['errors']),
                      'ignorados': int(s.attrib['skipped'])} for s in suites],
          'total_testes': 75, 'jacoco_global': contadores,
          'casos_comuns_adapters': len(fake), 'extras_jpa': extras,
          'sha256_dominio_preservado': hashlib.sha256(atual).hexdigest()}
(saida / 'resultados.json').write_text(json.dumps(resumo, ensure_ascii=False, indent=2) + '\n')
print('PASSOU: 75 testes; 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS.')
print('PASSOU: JaCoCo 669/669 instruções, 16/16 branches, 160/160 linhas; mesmas classes antes/depois.')
print('PASSOU: 11 cenários comuns em fake e JPA/H2; mais 3 transacionais JPA.')
print('PASSOU: regra central preservada em relação ao D1; nenhuma exclusão JaCoCo.')
