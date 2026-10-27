package org.example.tdd.ia;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de laboratório; integração Python não é um modelo ML/LLM de produção. */
@RestController
@RequestMapping("/ia")
public class IaController {
    private final IaClient client;

    public IaController(IaClient client) { this.client = client; }

    @GetMapping("/recomendacoes/{alunoId}")
    public Map<String, Object> recomendacoes(@PathVariable Long alunoId) {
        return client.recomendar(alunoId);
    }

    @GetMapping("/assistente")
    public Map<String, Object> assistente(@RequestParam String pergunta) {
        return client.assistente(pergunta);
    }
}
