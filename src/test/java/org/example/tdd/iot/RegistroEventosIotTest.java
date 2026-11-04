package org.example.tdd.iot;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistroEventosIotTest {
    private EventoIot evento() { return new EventoIot(UUID.randomUUID(), "dispositivo-01", "PRESENCA", Instant.parse("2026-11-04T12:00:00Z"), 1); }
    @Test void aceitaENaoRepeteMesmoEvento() {
        var registro = new RegistroEventosIot(2); var evento = evento();
        assertEquals("recebido", registro.receber(evento).status());
        assertEquals("duplicado", registro.receber(evento).status());
    }
    @Test void mesmoIdComOutroConteudoGeraConflito() {
        var registro = new RegistroEventosIot(2); var evento = evento(); registro.receber(evento);
        assertThrows(IllegalStateException.class, () -> registro.receber(new EventoIot(evento.eventId(), "outro", "PRESENCA", evento.ocorridoEm(), 1)));
    }
    @Test void janelaLimitadaPodeAceitarNovamenteAposEviccao() {
        var registro = new RegistroEventosIot(2); var primeiro = evento(); registro.receber(primeiro);
        registro.receber(evento()); registro.receber(evento());
        assertEquals("recebido", registro.receber(primeiro).status());
    }
    @Test void reinicioDoRegistroPerdeDeduplicacao() {
        var evento = evento(); new RegistroEventosIot(2).receber(evento);
        assertEquals("recebido", new RegistroEventosIot(2).receber(evento).status());
    }
    @Test void rejeitaCamposInvalidos() {
        var r = new RegistroEventosIot(2); var e = evento();
        assertThrows(IllegalArgumentException.class, () -> r.receber(null));
        assertThrows(IllegalArgumentException.class, () -> r.receber(new EventoIot(null,e.dispositivoId(),e.tipo(),e.ocorridoEm(),1)));
        assertThrows(IllegalArgumentException.class, () -> r.receber(new EventoIot(e.eventId(),"../outro",e.tipo(),e.ocorridoEm(),1)));
        assertThrows(IllegalArgumentException.class, () -> r.receber(new EventoIot(e.eventId(),e.dispositivoId(),"INVALIDO",e.ocorridoEm(),1)));
        assertThrows(IllegalArgumentException.class, () -> r.receber(new EventoIot(e.eventId(),e.dispositivoId(),e.tipo(),null,1)));
        assertThrows(IllegalArgumentException.class, () -> r.receber(new EventoIot(e.eventId(),e.dispositivoId(),e.tipo(),e.ocorridoEm(),2)));
        assertThrows(IllegalArgumentException.class, () -> new RegistroEventosIot(0));
    }
    @Test void duplicatasConcorrentesTemUmaUnicaAceitacaoNaJanela() throws Exception {
        var registro = new RegistroEventosIot(2); var evento = evento();
        var pool = Executors.newFixedThreadPool(4);
        try {
            var tarefas = new java.util.ArrayList<Future<String>>();
            for (int i=0;i<8;i++) tarefas.add(pool.submit(() -> registro.receber(evento).status()));
            int recebidos=0;
            for (var f:tarefas) if (f.get(5,TimeUnit.SECONDS).equals("recebido")) recebidos++;
            assertEquals(1,recebidos);
        } finally { pool.shutdownNow(); }
    }
}
