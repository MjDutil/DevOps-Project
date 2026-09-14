<template>
  <main class="page">
    <header class="hero">
      <p class="kicker">Painel ATDD</p>
      <h1>Educacao Continuada Gamificada</h1>
      <p class="subtitle">Execute os cenarios de teste e valide o comportamento esperado de cada US.</p>
    </header>

    <section class="card">
      <h2>US1 - Desempenho Academico</h2>
      <p class="desc">Teste: aluno com media acima de 7 deve ganhar 3 cursos ao concluir disciplina.</p>
      <div class="row">
        <input v-model.number="mediaUs1" type="number" step="0.1" placeholder="Media para testar" />
        <button @click="executarUs1">Executar cenario US1</button>
      </div>
      <p class="expected">Esperado: media > 7 = 3 cursos, media <= 7 = 0 cursos.</p>
      <p v-if="statusUs1" :class="statusClass(statusUs1.ok)">{{ statusUs1.texto }}</p>
    </section>

    <section class="card">
      <h2>US2 - Recompensa por Forum</h2>
      <p class="desc">
        Teste: ganha 1 curso quando cria mais topicos que os demais e faz pelo menos 1 comentario de ajuda.
      </p>
      <div class="grid">
        <input v-model.number="forum.topicosCriados" type="number" placeholder="Topicos criados" />
        <input
          v-model.number="forum.maiorQuantidadeTopicosDosOutros"
          type="number"
          placeholder="Maior qtd dos outros"
        />
        <input v-model.number="forum.comentariosDeAjuda" type="number" placeholder="Comentarios de ajuda" />
      </div>
      <div class="row">
        <button @click="aplicarCenarioUs2(10, 8, 3)">Cenario valido</button>
        <button class="ghost" @click="aplicarCenarioUs2(8, 10, 3)">Nao lidera topicos</button>
        <button class="ghost" @click="aplicarCenarioUs2(10, 8, 0)">Sem comentarios</button>
        <button @click="executarUs2">Executar cenario US2</button>
      </div>
      <p class="expected">Esperado: apenas o cenario valido libera 1 curso.</p>
      <p v-if="statusUs2" :class="statusClass(statusUs2.ok)">{{ statusUs2.texto }}</p>
    </section>

    <section class="card">
      <h2>US3 - Plano Premium</h2>
      <p class="desc">Teste: ao atingir 12 cursos liberados, aluno vira Premium, recebe voucher e 3 moedas.</p>
      <div class="row">
        <button @click="executarUs3">Executar cenario US3 completo</button>
      </div>
      <p class="expected">Fluxo automatico: cria aluno, conclui 4 cursos (4 x 3 = 12) e verifica Premium.</p>
      <p v-if="statusUs3" :class="statusClass(statusUs3.ok)">{{ statusUs3.texto }}</p>
    </section>

    <section class="card">
      <h2>Resposta da API</h2>
      <pre>{{ resultado }}</pre>
    </section>
  </main>
</template>

<script setup>
import axios from "axios";
import { ref } from "vue";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
});

const mediaUs1 = ref(8.0);
const forum = ref({
  topicosCriados: 10,
  maiorQuantidadeTopicosDosOutros: 8,
  comentariosDeAjuda: 3
});

const resultado = ref("Sem execucao ainda.");
const statusUs1 = ref(null);
const statusUs2 = ref(null);
const statusUs3 = ref(null);

function formatar(data) {
  return JSON.stringify(data, null, 2);
}

function statusClass(ok) {
  return ok ? "status ok" : "status fail";
}

function aplicarCenarioUs2(topicosCriados, maiorQuantidadeTopicosDosOutros, comentariosDeAjuda) {
  forum.value = { topicosCriados, maiorQuantidadeTopicosDosOutros, comentariosDeAjuda };
}

async function criarAluno(media) {
  const { data } = await api.post("/alunos", { media });
  return data;
}

async function concluirCurso(id) {
  const { data } = await api.post(`/alunos/${id}/concluir-curso`);
  return data;
}

async function recompensarForum(id, payload) {
  const { data } = await api.post(`/alunos/${id}/recompensar-forum`, payload);
  return data;
}

async function verificarPremium(id) {
  const { data } = await api.post(`/alunos/${id}/verificar-premium`);
  return data;
}

async function executarUs1() {
  try {
    const aluno = await criarAluno(mediaUs1.value);
    const resposta = await concluirCurso(aluno.id);
    const esperado = mediaUs1.value > 7 ? 3 : 0;
    const ok = resposta.cursosLiberados === esperado;
    statusUs1.value = {
      ok,
      texto: ok
        ? `US1 aprovado: cursosLiberados=${resposta.cursosLiberados}`
        : `US1 falhou: esperado ${esperado}, retornou ${resposta.cursosLiberados}`
    };
    resultado.value = formatar(resposta);
  } catch (error) {
    statusUs1.value = { ok: false, texto: "US1 falhou por erro de chamada da API." };
    resultado.value = formatar(error.response?.data || error.message);
  }
}

async function executarUs2() {
  try {
    const aluno = await criarAluno(0);
    const resposta = await recompensarForum(aluno.id, forum.value);
    const elegivel =
      forum.value.topicosCriados > forum.value.maiorQuantidadeTopicosDosOutros &&
      forum.value.comentariosDeAjuda > 0;
    const esperado = elegivel ? 1 : 0;
    const ok = resposta.cursosLiberados === esperado;
    statusUs2.value = {
      ok,
      texto: ok
        ? `US2 aprovado: cursosLiberados=${resposta.cursosLiberados}`
        : `US2 falhou: esperado ${esperado}, retornou ${resposta.cursosLiberados}`
    };
    resultado.value = formatar(resposta);
  } catch (error) {
    statusUs2.value = { ok: false, texto: "US2 falhou por erro de chamada da API." };
    resultado.value = formatar(error.response?.data || error.message);
  }
}

async function executarUs3() {
  try {
    const aluno = await criarAluno(8.0);
    for (let i = 0; i < 4; i += 1) {
      await concluirCurso(aluno.id);
    }
    const resposta = await verificarPremium(aluno.id);
    const ok =
      resposta.plano === "Premium" &&
      resposta.voucherProjetosReais === true &&
      resposta.moedas === 3 &&
      resposta.cursosLiberados >= 12;
    statusUs3.value = {
      ok,
      texto: ok
        ? "US3 aprovado: aluno Premium com voucher e moedas."
        : "US3 falhou: retorno nao condiz com os criterios de Premium."
    };
    resultado.value = formatar(resposta);
  } catch (error) {
    statusUs3.value = { ok: false, texto: "US3 falhou por erro de chamada da API." };
    resultado.value = formatar(error.response?.data || error.message);
  }
}
</script>

<style scoped>
:global(body) {
  --bg: #f4f6fb;
  --ink: #12203a;
  --sub: #415476;
  --card: #ffffff;
  --line: #cfd8e6;
  --brand: #0c5aa6;
  --brand-2: #1986cf;
  --ok: #127a49;
  --ok-bg: #ddf4e8;
  --fail: #8e1e1e;
  --fail-bg: #fde6e6;
  margin: 0;
  font-family: "Trebuchet MS", "Gill Sans", "Segoe UI", sans-serif;
  background: radial-gradient(circle at 10% 10%, #dde7fa, var(--bg) 60%);
}

.page {
  max-width: 980px;
  margin: 0 auto;
  padding: 24px 16px 40px;
  color: var(--ink);
}

.hero h1 {
  margin: 8px 0 4px;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
}

.kicker {
  margin: 0;
  font-weight: 700;
  color: var(--brand);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.76rem;
}

.subtitle {
  color: var(--sub);
  margin-top: 0;
}

.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 16px;
  margin-top: 14px;
  box-shadow: 0 10px 22px -20px #0e274b;
}

.card h2 {
  margin-top: 0;
}

.desc,
.expected {
  color: var(--sub);
}

.row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

input {
  border: 1px solid #a9bbd8;
  border-radius: 8px;
  padding: 10px;
  background: #fcfdff;
}

button {
  border: none;
  background: linear-gradient(90deg, var(--brand), var(--brand-2));
  color: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

button.ghost {
  background: #dce8f9;
  color: #1a3f72;
}

button:hover {
  filter: brightness(0.96);
}

.status {
  margin: 10px 0 2px;
  border-radius: 8px;
  padding: 10px;
  font-weight: 600;
}

.status.ok {
  color: var(--ok);
  background: var(--ok-bg);
}

.status.fail {
  color: var(--fail);
  background: var(--fail-bg);
}

pre {
  background: #0f1b2e;
  color: #d7e7ff;
  border-radius: 8px;
  padding: 12px;
  overflow: auto;
  margin-bottom: 0;
}
</style>
