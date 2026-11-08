package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.AvatarUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadAvatarUseCase {

    AvatarUploadResponse execute(MultipartFile file);
}

