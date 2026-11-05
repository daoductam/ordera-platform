package com.ordera.modules.auth.infrastructure.mapper;

import com.ordera.modules.auth.domain.model.OtpAuditLog;
import com.ordera.modules.auth.domain.model.User;
import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    User toDomain(UserJpaEntity entity);
    UserJpaEntity toEntity(User domain);
    List<User> toUserDomainList(List<UserJpaEntity> entities);

    OtpAuditLog toDomain(OtpAuditLogJpaEntity entity);
    OtpAuditLogJpaEntity toEntity(OtpAuditLog domain);
    List<OtpAuditLog> toOtpDomainList(List<OtpAuditLogJpaEntity> entities);
}

