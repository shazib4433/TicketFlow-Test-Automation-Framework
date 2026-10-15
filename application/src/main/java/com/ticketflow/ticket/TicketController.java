package com.ticketflow.ticket;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketRequests.CreateTicketRequest request) {
        return ticketService.create(request);
    }

    @GetMapping("/{number}")
    public TicketResponse get(@PathVariable("number") String number) {
        return ticketService.get(number);
    }

    @GetMapping
    public List<TicketResponse> search(@RequestParam(required = false) String query) {
        return ticketService.search(query);
    }

    @PatchMapping("/{number}")
    public TicketResponse update(@PathVariable String number, @Valid @RequestBody TicketRequests.UpdateTicketRequest request) {
        return ticketService.update(number, request);
    }

    @PostMapping("/{number}/assign")
    public TicketResponse assign(@PathVariable String number, @Valid @RequestBody TicketRequests.AssignmentRequest request) {
        return ticketService.assign(number, request);
    }

    @PostMapping("/{number}/resolve")
    public TicketResponse resolve(@PathVariable String number, @Valid @RequestBody TicketRequests.ResolveRequest request) {
        return ticketService.resolve(number, request);
    }

    @PostMapping("/{number}/close")
    public TicketResponse close(@PathVariable String number) {
        return ticketService.close(number);
    }
}
