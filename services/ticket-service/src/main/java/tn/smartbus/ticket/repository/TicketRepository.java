package tn.smartbus.ticket.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import tn.smartbus.ticket.entity.Ticket;

public interface TicketRepository
        extends MongoRepository<Ticket, String> {
            
    Page<Ticket> findByCitizenIdOrderByPurchasedAtDesc(
            String citizenId,
            Pageable pageable
    );

    Optional<Ticket> findByIdAndCitizenId(
            String id,
            String citizenId
    );

    Optional<Ticket> findByCitizenIdAndPurchaseRequestId(
            String citizenId,
            String purchaseRequestId
    );

    Optional<Ticket> findByQrTokenHash(
            String qrTokenHash
    );
    boolean existsByTripId(String tripId);
    List<Ticket> findByTripId(String tripId);
}