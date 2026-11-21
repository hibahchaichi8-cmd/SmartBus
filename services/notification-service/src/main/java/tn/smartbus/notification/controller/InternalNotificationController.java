package tn.smartbus.notification.controller;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.notification.dto.CreateNotificationRequest;
import tn.smartbus.notification.dto.NotificationResponse;
import tn.smartbus.notification.service.NotificationService;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService service;

    public InternalNotificationController(
            NotificationService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request) {

        NotificationResponse notification =
                service.create(request);

        return ResponseEntity.status(201)
                .cacheControl(CacheControl.noStore())
                .body(notification);
    }
}