package com.ordera.modules.auth.infrastructure.mapper;

import com.ordera.modules.auth.domain.model.User;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.presentation.dto.AuthUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthDtoMapper {

    AuthUserResponse toUserResponse(User user);

    AuthUserResponse toUserResponse(UserJpaEntity entity);
}

