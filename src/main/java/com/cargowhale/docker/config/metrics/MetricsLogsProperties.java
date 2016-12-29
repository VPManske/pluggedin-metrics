package com.cargowhale.docker.config.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cargowhale.metrics.logs", ignoreUnknownFields = false)
public class MetricsLogsProperties {

    private Boolean enabled;

    private Long period;

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getPeriod() {
        return this.period;
    }

    public void setPeriod(final Long period) {
        this.period = period;
    }
}
