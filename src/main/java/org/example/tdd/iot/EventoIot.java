package org.example.tdd.iot;

import java.time.Instant;
import java.util.UUID;

/** Contrato normalizado de recepção demonstrativa; não altera gamificação. */
public record EventoIot(UUID eventId, String dispositivoId, String tipo,
                        Instant ocorridoEm, Integer versao) {}
