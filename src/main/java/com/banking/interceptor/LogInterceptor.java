package com.banking.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This LogInterceptor is to record each request and print the consumed time.
 */
@Component
public class LogInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        logger.info("Received request: method {}, URI {}", method, uri);
        request.setAttribute("startTimeStamp", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        Long startTime = (Long) request.getAttribute("startTimeStamp");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        if (ex != null) {
            logger.error("Request handling error: method {}, URI {}, status {}, duration {} ms, error: {}", method, uri, status, duration, ex.getMessage());
        } else {
            logger.info("Response success: method {}, URI {}, status {}, duration {} ms",  method, uri, status, duration);
        }
    }
} 