package com.clinicaltrial.assistant.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardDTO {
    private long totalPatients;
    private long totalClinicalEvents;
    private long totalTrials;
    private long potentialMatches;
    private long needsReview;
    private long newPatientsToday;
    private long newEventsToday;
    private List<TimelineEventDTO> recentEvents;
}
