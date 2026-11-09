package com.ordera.modules.kiosk.domain.constant;

public final class KioskConstants {

    private KioskConstants() {
        // Prevent instantiation of utility constant class
    }

    // Kiosk Watchdog & Countdown Timeouts (Seconds)
    public static final int WATCHDOG_IDLE_TIMEOUT_SECONDS = 60;
    public static final int POST_PAYMENT_REDIRECT_SECONDS = 5;

    // Queue Number & Phone Formats
    public static final String QUEUE_NUMBER_FORMAT = "#A-%02d";
    public static final String PHONE_REGEX = "^(\\+47)?[49]\\d{7}$";

    // Redis Locks & Cache Timeouts
    public static final String LOCK_KEY_KIOSK_PATTERN = "lock:kiosk:device:%s";
    public static final long SESSION_CACHE_TTL_MINUTES = 30L;
    public static final String SESSION_CACHE_KEY_PATTERN = "kiosk:session:%s:%s";
    public static final String QUEUE_COUNTER_KEY_PATTERN = "kiosk:queue:%s:%s";
    public static final long REDIS_LOCK_WAIT_SECONDS = 5L;
    public static final long REDIS_LOCK_LEASE_SECONDS = 10L;

    // Default Fallback Values
    public static final String DEFAULT_CURRENCY = "NOK";
    public static final String DEFAULT_GUEST_NAME = "Khách tích điểm mới";
    public static final String DEFAULT_TIER_NAME = "New Member";
    public static final double DEFAULT_EARN_RATE_PERCENT = 1.0;
    public static final int DEFAULT_AVAILABLE_VOUCHERS = 0;
    public static final int ZERO_POINTS = 0;
    public static final double MIN_BILL_AMOUNT = 1.00;

    // Status Constants
    public static final String KIOSK_STATUS_STANDBY = "STANDBY";
    public static final String PRINTER_STATUS_OK = "OK";
    public static final String CARD_READER_STATUS_ONLINE = "ONLINE";

    // Reset Reasons
    public static final String REASON_IDLE_TIMEOUT = "IDLE_TIMEOUT";
    public static final String REASON_USER_MANUAL_RESET = "USER_MANUAL_RESET";
    public static final String REASON_TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";

    // Fulfillment & Payment Methods
    public static final String FULFILLMENT_TYPE_DINE_IN = "DINE_IN";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String REFERENCE_TYPE_EXTERNAL_RECEIPT = "EXTERNAL_RECEIPT";
    public static final String REFERENCE_TYPE_KIOSK_ORDER = "KIOSK_ORDER";
}

