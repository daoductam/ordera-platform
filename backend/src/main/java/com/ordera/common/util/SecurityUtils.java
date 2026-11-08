package com.ordera.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

/**
 * Tiện ích xử lý ngữ cảnh danh tính, trích xuất User ID và phân tích an toàn UUID.
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Phân tích chuỗi UUID an toàn, trả về Optional.empty() nếu chuỗi không hợp lệ hoặc null.
     */
    public static Optional<UUID> parseUuid(String uuidStr) {
        if (uuidStr == null || uuidStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(uuidStr.trim()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Trích xuất User ID từ Header X-User-ID hoặc Spring Security Principal.
     *
     * @param userIdHeader Giá trị header X-User-ID từ client
     * @param principal    Ngữ cảnh Principal từ Spring Security
     * @return UUID người dùng hiện tại, hoặc null nếu chưa đăng nhập/không hợp lệ
     */
    public static UUID resolveCurrentUserId(String userIdHeader, Principal principal) {
        Optional<UUID> headerId = parseUuid(userIdHeader);
        if (headerId.isPresent()) {
            return headerId.get();
        }

        if (principal != null) {
            return parseUuid(principal.getName()).orElse(null);
        }

        return null;
    }

    /**
     * Lấy UUID của người dùng hiện tại trực tiếp từ SecurityContextHolder.
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return parseUuid(authentication.getName()).orElse(null);
        }
        return null;
    }
}
