<template>
  <main class="page">
    <header class="hero">
      <p class="kicker">Painel ATDD</p>
      <h1>Educação Continuada Gamificada</h1>
      <p class="subtitle">
        Execute os cenários de teste e valide o comportamento esperado de cada User Story.
      </p>
    </header>

    <!-- US1 -->
    <section class="card">
      <div class="us-header">
        <span class="us-badge">US1</span>
        <div>
          <h2>Desempenho Acadêmico</h2>
          <p class="desc">
            Valida a recompensa concedida ao aluno após a conclusão de um curso.
          </p>
        </div>
      </div>

      <div class="rule-box">
        <span class="section-label">Regra testada</span>
        <p>
          Se o aluno concluir um curso com <strong>média acima de 7</strong>,
          deve receber <strong>+3 cursos</strong>.
        </p>
      </div>

      <div class="form-section">
        <span class="section-label">Dados do teste</span>

        <div class="field single-field">
          <label for="media-us1">Média final do aluno</label>
          <input
              id="media-us1"
              v-model.number="mediaUs1"
              type="number"
              step="0.1"
              placeholder="Ex.: 8.0"
          />
          <small>Teste valores como 8.0, 7.0 ou 6.5.</small>
        </div>
      </div>

      <button class="primary-action" @click="executarUs1">
        Executar cenário US1
      </button>

      <div class="expected-box">
        <strong>Resultado esperado</strong>
        <span>Média &gt; 7 → 3 cursos | Média ≤ 7 → 0 cursos</span>
      </div>

      <p v-if="statusUs1" :class="statusClass(statusUs1.ok)">
        {{ statusUs1.texto }}
      </p>
    </section>

    <!-- US2 -->
    <section class="card">
      <div class="us-header">
        <span class="us-badge">US2</span>
        <div>
          <h2>Recompensa por Fórum</h2>
          <p class="desc">
            Valida a recompensa do aluno por participação e ajuda no fórum.
          </p>
        </div>
      </div>

      <div class="rule-box">
        <span class="section-label">Regra testada</span>
        <p>
          O aluno recebe <strong>+1 curso</strong> quando cria mais tópicos
          que os demais <strong>e</strong> realiza pelo menos um comentário de ajuda.
        </p>
      </div>

      <div class="form-section">
        <span class="section-label">Dados do teste</span>

        <div class="grid">
          <div class="field">
            <label>Tópicos criados pelo aluno</label>
            <input
                v-model.number="forum.topicosCriados"
                type="number"
                placeholder="Ex.: 10"
            />
          </div>

          <div class="field">
            <label>Maior quantidade de tópicos dos outros</label>
            <input
                v-model.number="forum.maiorQuantidadeTopicosDosOutros"
                type="number"
                placeholder="Ex.: 8"
            />
          </div>

          <div class="field">
            <label>Comentários de ajuda realizados</label>
            <input
                v-model.number="forum.comentariosDeAjuda"
                type="number"
                placeholder="Ex.: 3"
            />
          </div>
        </div>
      </div>

      <div class="scenario-section">
        <span class="section-label">Preencher cenário de exemplo</span>

        <div class="scenario-buttons">
          <button @click="aplicarCenarioUs2(10, 8, 3)">
            Cenário válido
          </button>

          <button
              class="ghost"
              @click="aplicarCenarioUs2(8, 10, 3)"
          >
            Não lidera tópicos
          </button>

          <button
              class="ghost"
              @click="aplicarCenarioUs2(10, 8, 0)"
          >
            Sem comentários
          </button>
        </div>
      </div>

      <button class="primary-action" @click="executarUs2">
        Executar cenário US2
      </button>

      <div class="expected-box">
        <strong>Resultado esperado</strong>
        <span>Somente o cenário válido deve liberar +1 curso.</span>
      </div>

      <p v-if="statusUs2" :class="statusClass(statusUs2.ok)">
        {{ statusUs2.texto }}
      </p>
    </section>

    <!-- US3 -->
    <section class="card">
      <div class="us-header">
        <span class="us-badge">US3</span>
        <div>
          <h2>Plano Premium</h2>
          <p class="desc">
            Valida a mudança automática do plano quando o aluno alcança 12 cursos.
          </p>
        </div>
      </div>

      <div class="rule-box">
        <span class="section-label">Regra testada</span>
        <p>
          Ao conquistar <strong>12 cursos</strong>, o aluno deve se tornar
          <strong>Premium</strong>, receber um <strong>voucher</strong>
          e ganhar <strong>3 moedas</strong>.
        </p>
      </div>

      <div class="flow-box">
        <span class="section-label">Fluxo executado automaticamente</span>

        <div class="flow">
          <span>Aluno com média 8</span>
          <span class="arrow">→</span>
          <span>4 conclusões</span>
          <span class="arrow">→</span>
          <span>12 cursos</span>
          <span class="arrow">→</span>
          <span>Premium</span>
        </div>
      </div>

      <button class="primary-action" @click="executarUs3">
        Executar cenário US3 completo
      </button>

      <div class="expected-box">
        <strong>Resultado esperado</strong>
        <span>Premium + voucher liberado + 3 moedas + 12 cursos.</span>
      </div>

      <p v-if="statusUs3" :class="statusClass(statusUs3.ok)">
        {{ statusUs3.texto }}
      </p>
    </section>

    <!-- API -->
    <section class="card api-card">
      <span class="section-label">Retorno técnico</span>
      <h2>Resposta da API</h2>
      <p class="desc">
        JSON retornado pelo backend após a última execução realizada.
      </p>

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
          ? `✓ Teste aprovado — esperado: ${esperado} curso(s) | obtido: ${resposta.cursosLiberados} curso(s)`
          : `✕ Teste falhou — esperado: ${esperado} | obtido: ${resposta.cursosLiberados}`
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
          ? `✓ Teste aprovado — esperado: ${esperado} curso(s) | obtido: ${resposta.cursosLiberados} curso(s)`
          : `✕ Teste falhou — esperado: ${esperado} | obtido: ${resposta.cursosLiberados}`
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
          ? "✓ Teste aprovado — aluno tornou-se Premium, recebeu voucher e 3 moedas."
          : "✕ Teste falhou — o retorno não corresponde aos critérios do plano Premium."
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

.us-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.us-header h2 {
  margin: 0 0 4px;
}

.us-header .desc {
  margin: 0;
}

.us-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: #e5f0fd;
  color: var(--brand);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.section-label {
  display: block;
  margin-bottom: 8px;
  color: var(--brand);
  font-size: 0.75rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.07em;
}

.rule-box {
  margin: 16px 0 22px;
  padding: 14px 16px;
  background: #f6f9fe;
  border-left: 4px solid var(--brand);
  border-radius: 8px;
}

.rule-box p {
  margin: 0;
  color: var(--sub);
  line-height: 1.55;
}

.form-section {
  margin-top: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field label {
  color: var(--ink);
  font-size: 0.9rem;
  font-weight: 700;
}

.field small {
  color: #71809a;
  font-size: 0.78rem;
}

.single-field {
  max-width: 300px;
}

.grid {
  gap: 14px;
}

.grid input {
  width: 100%;
  box-sizing: border-box;
}

.scenario-section {
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px solid #e3e9f2;
}

.scenario-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-action {
  display: block;
  width: fit-content;
  min-width: 220px;
  margin-top: 24px;
  padding: 12px 18px;
}

.expected-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 20px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f5f7fb;
  color: var(--sub);
}

.expected-box strong {
  color: var(--ink);
  font-size: 0.85rem;
}

.flow-box {
  margin-top: 20px;
}

.flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.flow span:not(.arrow) {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eef4fd;
  color: #234e83;
  font-weight: 600;
  font-size: 0.86rem;
}

.arrow {
  color: var(--brand);
  font-weight: 800;
}

.status {
  margin-top: 16px;
  padding: 13px 15px;
}

.api-card {
  margin-top: 26px;
}

@media (max-width: 650px) {
  .us-header {
    flex-direction: column;
  }

  .primary-action {
    width: 100%;
  }

  .scenario-buttons button {
    flex: 1 1 100%;
  }

  .flow .arrow {
    display: none;
  }
}

</style>
