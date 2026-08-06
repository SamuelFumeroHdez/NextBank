package com.nextbank.account.adapter.in.rest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter  extends OncePerRequestFilter{

    public static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String incoming = request.getHeader(HEADER);
        if (incoming != null && !incoming.isBlank()) {
            CorrelationContext.set(incoming);
        }
        String correlationId = CorrelationContext.getOrGenerate();
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        try{
            filterChain.doFilter(request, response);
        }finally {
            MDC.remove(MDC_KEY);
            CorrelationContext.clear();
        }



    }
}
