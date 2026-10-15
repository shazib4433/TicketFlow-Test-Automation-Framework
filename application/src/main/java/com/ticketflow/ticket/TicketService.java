package com.ticketflow.ticket;

import com.ticketflow.notification.NotificationPublisher;
import com.ticketflow.notification.TicketNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final Clock clock;
    private final NotificationPublisher notificationPublisher;

    public TicketService(TicketRepository ticketRepository) {
        this(ticketRepository, Clock.systemUTC(), notification -> { });
    }

    TicketService(TicketRepository ticketRepository, Clock clock) {
        this(ticketRepository, clock, notification -> { });
    }

    @Autowired
    public TicketService(TicketRepository ticketRepository, NotificationPublisher notificationPublisher) {
        this(ticketRepository, Clock.systemUTC(), notificationPublisher);
    }

    TicketService(TicketRepository ticketRepository, Clock clock, NotificationPublisher notificationPublisher) {
        this.ticketRepository = ticketRepository;
        this.clock = clock;
        this.notificationPublisher = notificationPublisher;
    }

    public TicketResponse create(TicketRequests.CreateTicketRequest request) {
        Ticket ticket = new Ticket(request.shortDescription(), request.description(), request.category(), request.priority());
        ticket.markCreated(Instant.now(clock));
        Ticket saved = ticketRepository.save(ticket);
        publish("TICKET_CREATED", saved);
        if (saved.getPriority() == TicketPriority.P1_CRITICAL) publish("P1_TICKET_CREATED", saved);
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse get(String incidentNumber) {
        return TicketResponse.from(find(incidentNumber));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> search(String query) {
        List<Ticket> tickets = query == null || query.isBlank()
                ? ticketRepository.findAll()
                : ticketRepository.findByShortDescriptionContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        return tickets.stream().map(TicketResponse::from).toList();
    }

    public TicketResponse update(String incidentNumber, TicketRequests.UpdateTicketRequest request) {
        Ticket ticket = find(incidentNumber);
        if (request.shortDescription() != null) ticket.setShortDescription(request.shortDescription());
        if (request.description() != null) ticket.setDescription(request.description());
        if (request.category() != null) ticket.setCategory(request.category());
        if (request.priority() != null) ticket.setPriority(request.priority());
        if (request.resolutionNotes() != null) ticket.setResolutionNotes(request.resolutionNotes());
        if (request.status() != null) transition(ticket, request.status());
        ticket.touch(Instant.now(clock));
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.from(saved);
    }

    public TicketResponse assign(String incidentNumber, TicketRequests.AssignmentRequest request) {
        Ticket ticket = find(incidentNumber);
        ticket.setAssignedUser(request.assignedUser());
        ticket.setAssignedGroup(request.assignedGroup());
        if (ticket.getStatus() == TicketStatus.NEW) transition(ticket, TicketStatus.IN_PROGRESS);
        ticket.touch(Instant.now(clock));
        Ticket saved = ticketRepository.save(ticket);
        publish("TICKET_ASSIGNED", saved);
        return TicketResponse.from(saved);
    }

    public TicketResponse resolve(String incidentNumber, TicketRequests.ResolveRequest request) {
        Ticket ticket = find(incidentNumber);
        transition(ticket, TicketStatus.RESOLVED);
        ticket.setResolutionNotes(request.resolutionNotes());
        ticket.touch(Instant.now(clock));
        Ticket saved = ticketRepository.save(ticket);
        publish("TICKET_RESOLVED", saved);
        return TicketResponse.from(saved);
    }

    public TicketResponse close(String incidentNumber) {
        Ticket ticket = find(incidentNumber);
        transition(ticket, TicketStatus.CLOSED);
        ticket.touch(Instant.now(clock));
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    private Ticket find(String incidentNumber) {
        if (incidentNumber == null || !incidentNumber.matches("INC\\d{6}")) {
            throw new TicketNotFoundException(incidentNumber);
        }
        long id = Long.parseLong(incidentNumber.substring(3));
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(incidentNumber));
    }

    private void transition(Ticket ticket, TicketStatus requested) {
        boolean valid = switch (ticket.getStatus()) {
            case NEW -> requested == TicketStatus.IN_PROGRESS;
            case IN_PROGRESS -> requested == TicketStatus.ON_HOLD || requested == TicketStatus.RESOLVED;
            case ON_HOLD -> requested == TicketStatus.IN_PROGRESS;
            case RESOLVED -> requested == TicketStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!valid) throw new InvalidTicketTransitionException(ticket.getStatus(), requested);
        ticket.setStatus(requested);
    }

    private void publish(String event, Ticket ticket) {
        notificationPublisher.publish(new TicketNotification(
                event, ticket.getIncidentNumber(), ticket.getPriority().name(), ticket.getStatus().name()));
    }
}
