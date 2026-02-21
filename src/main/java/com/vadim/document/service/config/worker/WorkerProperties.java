package com.vadim.document.service.config.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "worker")
public class WorkerProperties {
    private int batchSize = 100;  // размер пачки по умолчанию
    private long submitIntervalMs = 60000;  // интервал проверки для SUBMIT (1 минута)
    private long approveIntervalMs = 60000;  // интервал проверки для APPROVE (1 минута)
    private boolean enabled = true;  // включить/выключить воркеры
}