package tn.smartbus.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.notification.dto.NotificationPageResponse;
import tn.smartbus.notification.dto.NotificationResponse;
import tn.smartbus.notification.dto.UnreadCountResponse;
import tn.smartbus.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<NotificationPageResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> notifications =
                service.getMine(jwt.getSubject(), page, size);

        NotificationPageResponse response =
                new NotificationPageResponse(
                        notifications.getContent(),
                        notifications.getNumber(),
                        notifications.getSize(),
                        notifications.getTotalElements(),
                        notifications.getTotalPages()
                );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            @AuthenticationPrincipal Jwt jwt) {

        long count = service.countUnread(jwt.getSubject());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new UnreadCountResponse(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        NotificationResponse notification =
                service.markAsRead(jwt.getSubject(), id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(notification);
    }
}