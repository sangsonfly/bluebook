package com.example.springboot.controller;

import com.example.springboot.common.Constants;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.entity.dto.AiOptimizeRequest;
import com.example.springboot.entity.dto.AiOptimizeResponse;
import com.example.springboot.service.AiCopyService;
import com.example.springboot.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private AiCopyService aiCopyService;

    @PostMapping("/copy/optimize")
    public Result optimizeCopy(@RequestBody AiOptimizeRequest request) {
        try {
            Account current = TokenUtils.getCurrentUser();
            Integer userId = current == null ? null : current.getId();
            AiOptimizeResponse response = aiCopyService.optimize(request, userId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(Constants.CODE_400, e.getMessage());
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, "AI 服务暂时不可用，请稍后重试");
        }
    }
}
