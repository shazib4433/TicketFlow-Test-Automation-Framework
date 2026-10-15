package com.ticketflow.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class TicketExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(TicketNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(InvalidTicketTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> invalidTransition(InvalidTicketTransitionException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflictingUpdate() {
        return Map.of("error", "Ticket was updated by another user; retry with the latest version");
    }
}
