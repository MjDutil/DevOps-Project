# Decisões para a PoC da Avaliação Final

| Desafio | Levar para a PoC | Limite e evolução condicionada |
|---|---|---|
| D1 | Monólito modular incremental e fronteira pública da conclusão | Modularização parcial; ranking ainda futuro |
| D2 | Domínio puro, porta de persistência e contrato fake/JPA | Não estender a migração global sem necessidade |
| D3 | Serviço Python independente, timeout e fallback | RF03/RF04 demonstrativos; modelo real exige nova validação |
| D4 | Notificação opcional somente após commit, fila limitada | Melhor esforço; outbox/broker somente com requisito de recuperação durável |
| D5 | MQTT 3.1.1/QoS 1, gateway, ACL, validação e deduplicação | ACK condicionado ao HTTP; janela em memória; sem exatamente uma vez |
| D6 | CI Jenkins, artefato identificado e métricas do fallback | Implantação local controlada; sem CD/HA/segurança de produção |

Preservar RF → RNF → até três ASRs → RPC → alternativas → ADR → C4 → evidência. Equipe B1 confirmada: Felipe, Isadora e Maria Julia, conforme o [histórico AC1](../ac1/README.md#integrantes). Antes da AF, confirmar escopo funcional, escolher cenários realmente implementáveis e executar novamente os ensaios com o artefato final. [Registro cumulativo](README.md), [ADRs](../adr/) e [resultados D6](../evidencias/desafio-6/README.md).
