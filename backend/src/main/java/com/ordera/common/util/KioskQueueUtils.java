package com.ordera.common.util;

import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tiện ích tạo số thứ tự phục vụ tại quầy Kiosk (dạng #A-08).
 * Quản lý bộ đếm nguyên tử hàng ngày theo từng chi nhánh thông qua Redis / in-memory fallback.
 */
@Slf4j
public final class KioskQueueUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong LOCAL_COUNTER = new AtomicLong(1);
    private static final long MAX_DAILY_QUEUE_NUMBER = 99L;

    private KioskQueueUtils() {
        // Prevent instantiation
    }

    /**
     * Sinh số thứ tự phục vụ nguyên tử theo từng Store và từng ngày.
     * Ví dụ: #A-01, #A-02, ..., #A-08
     *
     * @param redissonClient client Redis để thực hiện atomic increment (có thể null nếu dev/test)
     * @param storeId        định danh chi nhánh cửa hàng
     * @return chuỗi định dạng số thứ tự (ví dụ: #A-08)
     */
    public static String generateQueueNumber(RedissonClient redissonClient, UUID storeId) {
        long sequence = nextSequence(redissonClient, storeId);
        return formatQueueNumber(sequence);
    }

    /**
     * Định dạng số thứ tự từ một số nguyên cho trước.
     *
     * @param sequence giá trị số nguyên
     * @return chuỗi định dạng #A-%02d
     */
    public static String formatQueueNumber(long sequence) {
        long normalized = (sequence <= 0) ? 1 : ((sequence - 1) % MAX_DAILY_QUEUE_NUMBER) + 1;
        return String.format(KioskConstants.QUEUE_NUMBER_FORMAT, normalized);
    }

    /**
     * Chuyển đổi số thứ tự (#A-08) thành mã rút gọn hiển thị (A-08).
     *
     * @param queueNumber chuỗi số thứ tự
     * @return mã rút gọn không có dấu thăng (#)
     */
    public static String toOrderCode(String queueNumber) {
        if (queueNumber == null) {
            return "A-01";
        }
        return queueNumber.replace("#", "").trim();
    }

    private static long nextSequence(RedissonClient redissonClient, UUID storeId) {
        if (redissonClient == null || storeId == null) {
            return LOCAL_COUNTER.getAndIncrement();
        }

        try {
            String todayStr = LocalDate.now().format(DATE_FORMATTER);
            String key = String.format(KioskConstants.QUEUE_COUNTER_KEY_PATTERN, storeId, todayStr);
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long nextVal = atomicLong.incrementAndGet();
            // Đặt thời gian hết hạn 2 ngày cho key counter
            if (nextVal == 1) {
                atomicLong.expire(java.time.Duration.ofDays(2));
            }
            return nextVal;
        } catch (Exception e) {
            log.warn("Failed to generate queue sequence from Redis, using local counter fallback: {}", e.getMessage());
            return LOCAL_COUNTER.getAndIncrement();
        }
    }
}

