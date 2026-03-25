package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.NotificationDto;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long restauranteId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(restauranteId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(restauranteId, emitter));
        emitter.onTimeout(() -> removeEmitter(restauranteId, emitter));
        emitter.onError(ex -> removeEmitter(restauranteId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(new NotificationDto.NotificationEvent(
                            "CONNECTED",
                            "Canal de notificações ativo.",
                            null,
                            OffsetDateTime.now()
                    )));
        } catch (IOException ex) {
            removeEmitter(restauranteId, emitter);
        }

        return emitter;
    }

    public void publish(Long restauranteId, String type, String message, Long entityId) {
        NotificationDto.NotificationEvent event = new NotificationDto.NotificationEvent(
                type,
                message,
                entityId,
                OffsetDateTime.now()
        );

        emitters.getOrDefault(restauranteId, List.of()).forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(type).data(event));
            } catch (IOException ex) {
                removeEmitter(restauranteId, emitter);
            }
        });
    }

    @Scheduled(fixedRate = 25000)
    public void heartbeat() {
        emitters.forEach((restauranteId, currentEmitters) -> currentEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(OffsetDateTime.now().toString()));
            } catch (IOException ex) {
                removeEmitter(restauranteId, emitter);
            }
        }));
    }

    private void removeEmitter(Long restauranteId, SseEmitter emitter) {
        List<SseEmitter> restauranteEmitters = emitters.get(restauranteId);
        if (restauranteEmitters != null) {
            restauranteEmitters.remove(emitter);
        }
    }
}

