package com.sellerpro.service;

import com.sellerpro.entity.ActivityLog;
import com.sellerpro.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async("notificationExecutor")
    public void log(Long userId, String action, String entityType, Long entityId,
                    Map<String, Object> details, String ipAddress) {
        ActivityLog log = ActivityLog.builder()
            .userId(userId)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .ipAddress(ipAddress)
            .build();
        activityLogRepository.save(log);
    }

    @Async("notificationExecutor")
    public void log(Long userId, String action) {
        log(userId, action, null, null, null, null);
    }
}
