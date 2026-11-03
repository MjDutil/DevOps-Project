// Uso: node docs/evidencias/revisao-readme/validar-readme.mjs /tmp/b1-readme-tools [--working-tree]
// Sem --working-tree, exige destinos já incluídos no índice Git.
// Ferramentas editoriais isoladas do projeto: marked e github-slugger.
import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { pathToFileURL } from 'node:url';
import { execFileSync } from 'node:child_process';

const toolsDir = path.resolve(process.argv[2]);
const require = createRequire(path.join(toolsDir, 'package.json'));
const { marked } = await import(pathToFileURL(require.resolve('marked')));
const { default: GithubSlugger } = await import(pathToFileURL(require.resolve('github-slugger')));
const root = process.cwd();
function markdownFiles(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => {
    const file = path.join(directory, entry.name);
    return entry.isDirectory() ? markdownFiles(file) : (entry.name.endsWith('.md') ? [file] : []);
  });
}
const files = ['README.md', 'README-B1-START.md', ...markdownFiles('docs'), 'labs/ia/README.md'];
const tracked = new Set(execFileSync('git', ['ls-files', '-z'], { encoding: 'utf8' }).split('\0'));
const workingTree = process.argv.includes('--working-tree');
const untracked = new Set(execFileSync('git', ['ls-files', '--others', '--exclude-standard', '-z'], { encoding: 'utf8' }).split('\0'));
let count = 0;
let fragments = 0;
const external = new Set();
function assert(condition, message) { if (!condition) throw new Error(message); }
function anchors(markdown) {
  const result = new Set([...markdown.matchAll(/<a\s+(?:id|name)="([^"]+)"\s*><\/a>/g)].map(x => x[1]));
  const slugger = new GithubSlugger();
  marked.walkTokens(marked.lexer(markdown), token => {
    if (token.type === 'heading') result.add(slugger.slug(token.text.replace(/[`*_]/g, '')));
  });
  return result;
}
function exactCase(relative) {
  let directory = root;
  for (const component of relative.split(path.sep)) {
    assert(fs.readdirSync(directory).includes(component), `Nome/capitalização incorretos: ${relative}`);
    directory = path.join(directory, component);
  }
}
for (const file of files) {
  const markdown = fs.readFileSync(file, 'utf8');
  marked.walkTokens(marked.lexer(markdown), token => {
    if (!['link', 'image'].includes(token.type)) return;
    const href = token.href;
    if (/^https?:\/\//.test(href)) { external.add(href); return; }
    assert(!/^(?:file:|\/|[A-Za-z]:)/.test(href), `Link não portável: ${file}: ${href}`);
    const [destination, fragment] = href.split('#');
    const resolved = destination ? path.resolve(path.dirname(file), decodeURIComponent(destination)) : path.resolve(file);
    const relative = path.relative(root, resolved);
    assert(!relative.startsWith('..'), `Link fora do repositório: ${href}`);
    assert(fs.existsSync(resolved), `Destino ausente: ${file}: ${href}`);
    exactCase(relative);
    if (fs.statSync(resolved).isFile()) assert(tracked.has(relative) || (workingTree && untracked.has(relative)), `Destino fora do índice Git${workingTree ? ' ou ignorado' : ''}: ${relative}`);
    if (fragment) {
      assert(anchors(fs.readFileSync(resolved, 'utf8')).has(decodeURIComponent(fragment)), `Âncora ausente: ${file}: ${href}`);
      fragments++;
    }
    count++;
  });
}
const expected = [
  'docs/evidencias/revisao-readme/c4-container',
  'docs/evidencias/revisao-readme/c4-component',
  'docs/evidencias/desafio-3/c4-container',
];
const diagramPage = fs.readFileSync('docs/arquitetura/diagramas.md', 'utf8');
function visibleText(text) {
  return text.replace(/<[^>]*>/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/\s+/g, ' ').trim();
}
for (const name of expected) {
  const source = fs.readFileSync(`${name}.mmd`, 'utf8').trim();
  assert(/^flowchart\s+(TB|LR)\b/.test(source), `Fonte C4 ausente ou inesperado: ${name}`);
  const svg = fs.readFileSync(`${name}.svg`, 'utf8');
  assert(svg.includes('<svg') && !svg.includes('Syntax error'), `SVG inválido: ${name}`);
  for (const [, label] of source.matchAll(/"([^"]+)"/g)) {
    assert(visibleText(svg).includes(visibleText(label)), `Rótulo do fonte ausente no SVG: ${name}: ${label}`);
  }
}
const d3Source = fs.readFileSync(`${expected[2]}.mmd`, 'utf8').trim();
assert(diagramPage.includes('```mermaid\n' + d3Source + '\n```'), 'Container D3 fora de sincronismo no registro arquitetural');
assert(fs.readFileSync('README.md', 'utf8').includes('```mermaid\n' + d3Source + '\n```'), 'Container D3 fora de sincronismo no README');
console.log(`PASSOU: ${files.length} documentos, ${count} links/imagens relativos e ${fragments} âncoras.`);
console.log(`PASSOU: destinos existentes, capitalização exata e ${workingTree ? 'arquivos rastreados ou novos não ignorados (modo working-tree)' : 'arquivos incluídos no índice Git'}.`);
console.log('PASSOU: fontes Mermaid e rótulos dos SVGs D1/D2/D3 conferidos; Container D3 sincronizado com o registro arquitetural e apresentado no README.');
console.log('URLs externas encontradas: ' + [...external].join(', '));
console.log('Limite: verifica caminhos locais e sincronismo dos SVGs; não substitui parser Mermaid. Arquivos novos em working-tree ainda precisam de git add/commit. Publicação exige aprovação.');
