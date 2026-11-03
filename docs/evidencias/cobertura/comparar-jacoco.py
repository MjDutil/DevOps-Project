"""Compara contadores JaCoCo reais. Uso: python3 docs/evidencias/cobertura/comparar-jacoco.py."""
from pathlib import Path
import xml.etree.ElementTree as E

pasta = Path(__file__).resolve().parent
antes = E.parse(pasta / 'antes-jacoco.xml').getroot()
depois = E.parse(pasta / 'depois-jacoco.xml').getroot()

def contadores(elemento):
    return {c.attrib['type']: (int(c.attrib['covered']), int(c.attrib['missed']))
            for c in elemento.findall('counter')}

def taxa(contador):
    if contador is None:
        return '—'
    coberto, ausente = contador
    total = coberto + ausente
    return f'{100 * coberto / total:.2f}% ({coberto}/{total})' if total else '—'

linhas = ['# Comparação JaCoCo — antes/depois', '',
          'Percentuais calculados diretamente dos XMLs, sem arredondamento para inteiro.', '',
          '| Métrica global | Antes | Depois |', '|---|---|---|']
a, d = contadores(antes), contadores(depois)
for metrica in ['INSTRUCTION', 'BRANCH', 'LINE', 'METHOD', 'CLASS']:
    linhas.append(f'| {metrica} | {taxa(a.get(metrica))} | {taxa(d.get(metrica))} |')
linhas += ['', '| Classe | Instruções antes | Instruções depois | Branches antes → depois | Linhas antes → depois |',
           '|---|---|---|---|---|']
classes_antes = {c.attrib['name']: contadores(c) for c in antes.findall('./package/class')}
classes_depois = {c.attrib['name']: contadores(c) for c in depois.findall('./package/class')}
assert classes_antes.keys() == classes_depois.keys(), 'Conjunto de classes de produção mudou'
for nome in sorted(classes_depois):
    a, d = classes_antes[nome], classes_depois[nome]
    if not d:
        continue  # Interfaces sem bytecode executável não têm percentual de cobertura.
    linhas.append(f'| `{nome.replace("/", ".")}` | {taxa(a.get("INSTRUCTION"))} | '
                  f'{taxa(d.get("INSTRUCTION"))} | {taxa(a.get("BRANCH"))} → {taxa(d.get("BRANCH"))} | '
                  f'{taxa(a.get("LINE"))} → {taxa(d.get("LINE"))} |')
linhas += ['', '## Trechos inicialmente descobertos', '']
for pacote in antes.findall('package'):
    for fonte in pacote.findall('sourcefile'):
        for linha in fonte.findall('line'):
            if int(linha.attrib['mi']) or int(linha.attrib['mb']):
                linhas.append(f'- `{pacote.attrib["name"]}/{fonte.attrib["name"]}:{linha.attrib["nr"]}`: '
                              f'{linha.attrib["mi"]} instruções e {linha.attrib["mb"]} branches descobertos.')
linhas += ['', '## Trechos ainda descobertos', '']
pendentes = []
for pacote in depois.findall('package'):
    for fonte in pacote.findall('sourcefile'):
        for linha in fonte.findall('line'):
            if int(linha.attrib['mi']) or int(linha.attrib['mb']):
                pendentes.append(f'- `{pacote.attrib["name"]}/{fonte.attrib["name"]}:{linha.attrib["nr"]}`: {linha.attrib}')
linhas += pendentes or ['Nenhuma instrução, linha ou branch descoberto no relatório final.']
linhas += ['', '“—” indica ausência de branches/código executável, não um percentual presumido.',
           'Cobertura não mede a completude funcional, a validade de ML/RAG, rede real, carga ou PostgreSQL.']
(pasta / 'comparacao.md').write_text('\n'.join(linhas) + '\n')
print('\n'.join(linhas))
