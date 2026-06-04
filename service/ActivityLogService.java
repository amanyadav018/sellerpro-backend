package com.sellerpro.service;

import com.sellerpro.model.ActivityLog;
import com.sellerpro.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(String email, String action, String detail, String result) {
        ActivityLog log = ActivityLog.builder()
            .userEmail(email)
            .action(action)
            .detail(detail)
            .result(result)
            .build();
        activityLogRepository.save(log);
    }

    public void log(String email, String action, String detail,
                    String result, HttpServletRequest request) {
        ActivityLog log = ActivityLog.builder()
            .userEmail(email)
            .action(action)
            .detail(detail)
            .result(result)
            .ipAddress(getClientIp(request))
            .userAgent(request.getHeader("User-Agent"))
            .build();
        activityLogRepository.save(log);
    }

    public Page<ActivityLog> getAllLogs(int page, int size) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public Page<ActivityLog> getUserLogs(String email, int page, int size) {
        return activityLogRepository.findByUserEmailOrderByCreatedAtDesc(
            email, PageRequest.of(page, size));
    }

    public Page<ActivityLog> getLogsByAction(String action, int page, int size) {
        return activityLogRepository.findByActionOrderByCreatedAtDesc(
            action, PageRequest.of(page, size));
    }

    public List<Map<String, Object>> getActionSummary() {
        return activityLogRepository.findActionSummaryLast30Days().stream()
            .map(row -> Map.of("action", row[0], "count", row[1]))
            .toList();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
