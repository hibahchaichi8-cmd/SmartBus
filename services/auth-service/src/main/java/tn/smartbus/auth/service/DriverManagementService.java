package tn.smartbus.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tn.smartbus.auth.entity.User;
import tn.smartbus.auth.entity.UserRole;
import tn.smartbus.auth.exception.ApiException;
import tn.smartbus.auth.repository.UserRepository;

@Service
public class DriverManagementService {

    private final UserRepository users;
    private final ServiceClient client;
    private final String tripUrl;

    public DriverManagementService(
            UserRepository users,
            ServiceClient client,
            @Value("${app.trip-url}") String tripUrl) {

        this.users = users;
        this.client = client;
        this.tripUrl = tripUrl;
    }

    public void delete(String driverId) {

        User driver = users.findById(driverId)
                .orElseThrow(() -> ApiException.missing("Driver"));

        if (driver.role() != UserRole.DRIVER) {
            throw ApiException.missing("Driver");
        }

        Boolean hasTrips;

        try {
            hasTrips = client.get(
                    tripUrl + "/internal/drivers/"
                            + driverId + "/has-trips",
                    Boolean.class
            );

        } catch (ApiException exception) {

            if (exception.status == 404) {
                throw new ApiException(
                        503,
                        "Trip reference check is unavailable"
                );
            }

            throw exception;
        }

        if (hasTrips == null) {
            throw new ApiException(
                    503,
                    "Unable to check driver assignments"
            );
        }

        if (hasTrips) {
            throw ApiException.conflict(
                    "This driver is linked to trips and cannot be deleted"
            );
        }

        users.delete(driver);
    }
}