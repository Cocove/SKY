package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class myTask {

    //@Scheduled(cron = "0/5 * * * * ? ")
    public void executeTask(){
        System.out.println("执行定时任务" + new Date());
    }
}
