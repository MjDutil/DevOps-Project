package org.example.tdd.iot;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/** Deduplicação FIFO limitada ao processo e aos últimos N eventos, sem persistência. */
public class RegistroEventosIot {
    public record Recepcao(String status, EventoIot evento) {}
    private static final Set<String> TIPOS = Set.of("PRESENCA", "INICIO_ATIVIDADE", "CONCLUSAO_ATIVIDADE");
    private final LinkedHashMap<UUID, EventoIot> eventos = new LinkedHashMap<>();
    private final int capacidade;

    public RegistroEventosIot(int capacidade) {
        if (capacidade < 1) throw new IllegalArgumentException("Capacidade IoT deve ser positiva");
        this.capacidade = capacidade;
    }

    public synchronized Recepcao receber(EventoIot evento) {
        if (evento == null || evento.eventId() == null || evento.dispositivoId() == null
                || !evento.dispositivoId().matches("[A-Za-z0-9_-]{1,64}")
                || !TIPOS.contains(evento.tipo() == null ? "" : evento.tipo())
                || evento.ocorridoEm() == null || !Integer.valueOf(1).equals(evento.versao())) {
            throw new IllegalArgumentException("Evento IoT inválido");
        }
        var anterior = eventos.get(evento.eventId());
        if (anterior != null) {
            if (!anterior.equals(evento)) throw new IllegalStateException("eventId reutilizado com conteúdo diferente");
            return new Recepcao("duplicado", anterior);
        }
        if (eventos.size() == capacidade) eventos.remove(eventos.keySet().iterator().next());
        eventos.put(evento.eventId(), evento);
        return new Recepcao("recebido", evento);
    }
}
