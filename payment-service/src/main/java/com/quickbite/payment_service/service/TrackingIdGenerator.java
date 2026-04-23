package com.quickbite.payment_service.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrackingIdGenerator {

    private final AtomicLong sequence = new AtomicLong(0);
    private static final String PREFIX = "QB";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateTrackingId() {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        long seq = sequence.incrementAndGet();
        return String.format("%s-%s-%05d", PREFIX, date, seq);
    }
}
