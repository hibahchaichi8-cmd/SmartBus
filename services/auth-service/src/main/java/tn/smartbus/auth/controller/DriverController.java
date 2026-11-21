package tn.smartbus.auth.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.auth.dto.RegisterRequest;
import tn.smartbus.auth.dto.UserResponse;
import tn.smartbus.auth.entity.UserRole;
import tn.smartbus.auth.service.AuthService;
import tn.smartbus.auth.service.DriverManagementService;

@RestController
public class DriverController {

    private final AuthService service;
    private final DriverManagementService management;

    public DriverController(
            AuthService service,
            DriverManagementService management) {

        this.service = service;
        this.management = management;
    }

    @GetMapping("/api/v1/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return service.drivers();
    }

    @PostMapping("/api/v1/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse driver = service.create(
                request,
                UserRole.DRIVER
        );

        return ResponseEntity.status(201).body(driver);
    }

    @DeleteMapping("/api/v1/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        management.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/drivers/{id}")
    public UserResponse driver(@PathVariable String id) {
        return service.driver(id);
    }
}