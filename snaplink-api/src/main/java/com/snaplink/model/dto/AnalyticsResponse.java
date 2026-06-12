package com.snaplink.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Response body for {@code GET /api/analytics/{code}}.
 * Contains aggregated click analytics for a single short link.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponse {

    private String shortCode;
    private long totalClicks;
    private long uniqueClicks;
    private Map<String, Long> clicksByCountry;   // top 5 countries
    private Map<String, Long> clicksByDevice;    // mobile / desktop / bot
    private List<DailyClicks> clicksOverTime;    // 7-day time series

    public AnalyticsResponse() {}

    // ---- Nested class for time series ----
    public static class DailyClicks {
        private String date;
        private long clicks;

        public DailyClicks() {}

        public DailyClicks(String date, long clicks) {
            this.date = date;
            this.clicks = clicks;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public long getClicks() { return clicks; }
        public void setClicks(long clicks) { this.clicks = clicks; }
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }

    public long getUniqueClicks() { return uniqueClicks; }
    public void setUniqueClicks(long uniqueClicks) { this.uniqueClicks = uniqueClicks; }

    public Map<String, Long> getClicksByCountry() { return clicksByCountry; }
    public void setClicksByCountry(Map<String, Long> clicksByCountry) { this.clicksByCountry = clicksByCountry; }

    public Map<String, Long> getClicksByDevice() { return clicksByDevice; }
    public void setClicksByDevice(Map<String, Long> clicksByDevice) { this.clicksByDevice = clicksByDevice; }

    public List<DailyClicks> getClicksOverTime() { return clicksOverTime; }
    public void setClicksOverTime(List<DailyClicks> clicksOverTime) { this.clicksOverTime = clicksOverTime; }
}
