package com.ordera.modules.auth.infrastructure.adapter;

import com.ordera.modules.auth.application.port.SmsSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter gửi tin nhắn SMS viễn thông qua Telnyx.
 * Trong môi trường Local/Dev, ghi nhận thông điệp vào log hệ thống.
 */
@Slf4j
@Component
public class TelnyxSmsAdapter implements SmsSenderPort {

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[Telnyx SMS Gateway] Sending SMS to recipient [{}]: \"{}\"", phoneNumber, message);
    }
}

