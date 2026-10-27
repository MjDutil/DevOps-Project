# Testes direcionados e cobertura global

Data: 2026-10-27. Escopo: validar código existente. Nenhum fonte de produção, regra, endpoint ou contrato foi alterado; não foram implementadas funcionalidades dos Desafios 3–6.

## Resultado medido

`mvn clean verify`: **BUILD SUCCESS**, **75 testes**, zero falhas, erros ou testes ignorados. O `clean` removeu os dados JaCoCo anteriores antes da execução integral.

| Métrica | Antes | Depois |
|---|---|---|
| Instruções | 560/669 — 83,71% | **669/669 — 100%** |
| Branches | 12/16 — 75% | **16/16 — 100%** |
| Linhas | 141/160 — 88,12% | **160/160 — 100%** |
| Métodos | 52/62 — 83,87% | **62/62 — 100%** |

A [comparação por todas as classes](comparacao.md) é calculada diretamente dos XMLs e inclui os trechos antes descobertos. **Nenhuma instrução, linha ou branch permaneceu descoberto**. Classes/interfaces sem bytecode ou sem branches usam “—”, sem percentual fictício.

## O que faltava e como foi validado

| Classe | Lacuna identificada | Validação adicionada |
|---|---|---|
| `IaClient` | Chamadas HTTP, lambdas de URI, duas condições de corpo nulo e fallbacks | 15 cenários: sucesso, query com caracteres especiais, HTTP 204, JSON null, HTTP 400/404/500/503, conexão recusada, timeout de leitura/conexão simulado, JSON malformado, tipo incompatível, mapa sem schema esperado e nova chamada após falha |
| `IaController` | Métodos dos dois endpoints | Seis cenários MockMvc: sucesso e fallback nos dois endpoints, parâmetro ausente e ID não numérico; verificações de HTTP/JSON e interações com o cliente |
| `AlunoService` | Lambdas de aluno inexistente no fórum/Premium | Dois testes verificam erro/mensagem e ausência de qualquer escrita no repository |
| `AlunoEntity` | `setMedia` | Integração altera e persiste média de 6,5 para 8; confirma valor com JDBC e que a próxima conclusão libera três cursos |
| `TddApplication` | `main` | Teste existente fortalecido com `useMainMethod=ALWAYS`; inicializa pelo main real, verifica contexto ativo e a entrada de conclusão disponível |

Foram preservados os 51 testes anteriores e acrescentados 24 cenários: 15 cliente, seis controller, dois serviço e uma integração adicional. O teste de contexto existente passou a executar o main real, sem acrescentar um teste artificial de chamada estática.

## Estratégia de isolamento do cliente HTTP

`MockRestServiceServer` fornece o transporte, com `RestClient` e conversão JSON reais. Um spy no builder e um mock estático **limitado à construção** permitem usar o construtor público existente sem alterar a produção. A factory HTTP configurada pela produção é substituída somente no teste; nenhum servidor FastAPI ou socket é necessário.

`ConnectException` e `SocketTimeoutException` são injetadas como falhas de transporte. Isso verifica o tratamento e o fallback; **não mede os limites reais de 700 ms/1.200 ms nem prova temporização de rede**.

Um JSON válido como `{"outro":"valor"}` é repassado: o código atual não valida schema. O teste documenta essa limitação em vez de inventar um fallback que a produção não implementa. O controller mantém HTTP 200 para respostas degradadas, conforme o contrato existente. Não foi adicionado tratamento novo.

## Configuração Mockito/JaCoCo

A primeira tentativa falhou na inicialização do Mockito inline porque o ambiente impediu self-attach. O [log inicial](tentativa-sem-agente.txt) foi preservado; não é contado como execução bem-sucedida.

Foi configurado o `maven-surefire-plugin` para iniciar a JVM dos testes com o agente do Mockito já presente no starter de testes. `@{argLine}` preserva o agente JaCoCo. Não foi adicionada dependência de produção, não foi alterado o JaCoCo e não foram excluídas classes.

Após esse ajuste, os [33 testes direcionados](testes-direcionados.txt) passaram. A execução final foi limpa e completa, conforme [log integral](mvn-clean-verify.txt) e [resumo Surefire](resumo-testes.txt).

## Reprodução

```bash
mvn clean verify
python3 docs/evidencias/cobertura/comparar-jacoco.py

# Apenas os cenários direcionados, sem substituir a execução completa:
mvn -Dtest=IaClientTest,IaControllerTest,AlunoServiceTest,TddApplicationTests,AlunoHttpJpaIntegrationTest test
```

O HTML atual está em `target/site/jacoco/index.html`. Os relatórios gerados em `target` não são versionados. Os snapshots são [XML antes](antes-jacoco.xml), [XML depois](depois-jacoco.xml), [CSV antes](antes-jacoco.csv) e [CSV depois](depois-jacoco.csv). O [script de comparação](comparar-jacoco.py) reproduz a tabela e identifica quaisquer trechos ainda descobertos a partir desses snapshots.

## Cobertura global versus evidência arquitetural

O núcleo `domain` + `gamificacao.*` já tinha 100% de instruções e branches existentes antes desta etapa e permaneceu assim. A cobertura global menor vinha principalmente do cliente/controller IA, main, erros do serviço e setter da entidade.

**100% de cobertura significa execução do bytecode medido com as verificações descritas, não completude funcional.** Não prova schema correto do serviço remoto, IA real, ML/RAG, rede, limites de tempo, concorrência global, carga, PostgreSQL ou operação Docker. O mini-lab Spring/FastAPI real do D3 continua pendente; esta etapa apenas valida o código preexistente.

[Validação dos links da documentação](validacao-documentacao.txt). [Auditoria de escopo](auditoria-escopo.txt): mesmas classes e mesmos totais de código, sem alterações em `src/main`. A publicação no GitHub continua sujeita à aprovação anterior; nenhum merge ou push foi realizado.
