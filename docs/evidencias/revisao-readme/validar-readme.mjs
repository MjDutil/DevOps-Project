// Uso: node docs/evidencias/revisao-readme/validar-readme.mjs /tmp/b1-readme-tools
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
const files = [
  'README.md', 'docs/ac1/README.md', 'docs/arquitetura/README.md',
  'docs/adr/ADR-001.md', 'docs/adr/ADR-002.md',
  'docs/evidencias/desafio-1/README.md', 'docs/evidencias/desafio-2/README.md',
  'docs/evidencias/revisao-readme/README.md',
  'docs/evidencias/revisao-readme/integracao.md',
];
const tracked = new Set(execFileSync('git', ['ls-files', '-z'], { encoding: 'utf8' }).split('\0'));
let count = 0;
let fragments = 0;
let diagrams = 0;
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
    if (token.type === 'code' && token.lang === 'mermaid' && file === 'README.md') diagrams++;
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
    if (fs.statSync(resolved).isFile()) assert(tracked.has(relative), `Destino fora do índice Git: ${relative}`);
    if (fragment) {
      assert(anchors(fs.readFileSync(resolved, 'utf8')).has(decodeURIComponent(fragment)), `Âncora ausente: ${file}: ${href}`);
      fragments++;
    }
    count++;
  });
}
assert(diagrams === 2, `Esperados dois diagramas no README, encontrados ${diagrams}`);
for (const name of ['c4-container', 'c4-component']) {
  const source = fs.readFileSync(`docs/evidencias/revisao-readme/${name}.mmd`, 'utf8').trim();
  assert(fs.readFileSync('README.md', 'utf8').includes('```mermaid\n' + source + '\n```'), `Mermaid/SVG fora de sincronismo: ${name}`);
  const svg = fs.readFileSync(`docs/evidencias/revisao-readme/${name}.svg`, 'utf8');
  assert(svg.includes('<svg') && !svg.includes('Syntax error'), `SVG inválido: ${name}`);
}
console.log(`PASSOU: ${files.length} documentos, ${count} links/imagens relativos e ${fragments} âncoras.`);
console.log('PASSOU: destinos existentes, capitalização exata e arquivos incluídos no índice Git.');
console.log('PASSOU: dois blocos Mermaid sincronizados com os fontes renderizados em SVG.');
console.log('URLs externas encontradas: ' + [...external].join(', '));
console.log('Limite: validação dos caminhos do commit local; os novos destinos em main só estarão públicos após aprovação/publicação.');
