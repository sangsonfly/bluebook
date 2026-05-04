package com.example.springboot.task;

import com.example.springboot.service.IRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 推荐任务调度
 * 每小时执行一次推荐结果预计算
 */
@Slf4j
@Component
@EnableScheduling
public class RecommendationTask {
    
    @Resource
    private IRecommendationService recommendationService;
    
    /**
     * 每30秒执行一次推荐预计算（测试模式）
     * Cron表达式：30 * * * * ? 表示每30秒执行一次
     * 注意：测试完成后请改为每小时执行：0 0 * * * ?
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void precomputeRecommendations() {
        log.info("========== 开始执行推荐结果预计算任务 ==========");
        long startTime = System.currentTimeMillis();
        
        try {
            recommendationService.precomputeRecommendationsForAllActiveUsers();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("========== 推荐结果预计算任务完成，耗时：{}ms ==========", duration);
            
        } catch (Exception e) {
            log.error("========== 推荐结果预计算任务执行失败 ==========", e);
        }
    }
}

