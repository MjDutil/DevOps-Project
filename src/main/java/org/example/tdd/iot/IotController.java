package org.example.tdd.iot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/iot/eventos")
@ConditionalOnProperty(name = "iot.habilitado", havingValue = "true")
public class IotController {
    private static final Logger log = LoggerFactory.getLogger(IotController.class);
    private final RegistroEventosIot registro;
    private final byte[] autorizacao;

    public IotController(RegistroEventosIot registro, @Value("${iot.token:}") String token) {
        if (token.isBlank()) throw new IllegalArgumentException("IOT_TOKEN é obrigatório ao habilitar IoT");
        this.registro = registro;
        this.autorizacao = ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    public RegistroEventosIot.Recepcao receber(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody EventoIot evento) {
        if (authorization == null || !MessageDigest.isEqual(autorizacao,
                authorization.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gateway não autorizado");
        }
        try {
            var resultado = registro.receber(evento);
            log.info("D5 RECEPCAO status={} evento={} dispositivo={}", resultado.status(),
                    evento.eventId(), evento.dispositivoId());
            return resultado;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }
}
