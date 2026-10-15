package com.ticketflow.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private TicketRepository ticketRepository;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsNewTicketWithTimestampAndPriority() {
        Ticket ticket = new Ticket("VPN unavailable", "Remote access is down", "Network", TicketPriority.P2_HIGH);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket saved = invocation.getArgument(0);
            return saved;
        });

        TicketResponse response = ticketService.create(new TicketRequests.CreateTicketRequest(
                "VPN unavailable", "Remote access is down", "Network", TicketPriority.P2_HIGH));

        assertThat(response.status()).isEqualTo(TicketStatus.NEW);
        assertThat(response.priority()).isEqualTo(TicketPriority.P2_HIGH);
        assertThat(response.createdDate()).isEqualTo(NOW);
    }

    @Test
    void allowsTheSupportedTransitionChain() {
        Ticket ticket = ticketWithId(7L, TicketStatus.NEW);
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.update("INC000007", new TicketRequests.UpdateTicketRequest(null, null, null, null, TicketStatus.IN_PROGRESS, null));
        ticketService.update("INC000007", new TicketRequests.UpdateTicketRequest(null, null, null, null, TicketStatus.ON_HOLD, null));
        ticketService.update("INC000007", new TicketRequests.UpdateTicketRequest(null, null, null, null, TicketStatus.IN_PROGRESS, null));
        ticketService.resolve("INC000007", new TicketRequests.ResolveRequest("Restored access"));
        TicketResponse response = ticketService.close("INC000007");

        assertThat(response.status()).isEqualTo(TicketStatus.CLOSED);
        assertThat(response.resolutionNotes()).isEqualTo("Restored access");
    }

    @Test
    void rejectsInvalidTransition() {
        Ticket ticket = ticketWithId(8L, TicketStatus.NEW);
        when(ticketRepository.findById(8L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.resolve("INC000008", new TicketRequests.ResolveRequest("Not ready")))
                .isInstanceOf(InvalidTicketTransitionException.class);
    }

    @Test
    void rejectsUnknownIncidentNumber() {
        assertThatThrownBy(() -> ticketService.get("INC999999"))
                .isInstanceOf(TicketNotFoundException.class);
    }

    private Ticket ticketWithId(Long id, TicketStatus status) {
        Ticket ticket = new Ticket("Short description", "Description", "Category", TicketPriority.P3_MEDIUM);
        try {
            var field = Ticket.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ticket, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        ticket.setStatus(status);
        ticket.markCreated(NOW);
        return ticket;
    }
}
