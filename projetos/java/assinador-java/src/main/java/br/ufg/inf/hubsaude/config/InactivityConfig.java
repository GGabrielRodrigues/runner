package br.ufg.inf.hubsaude.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class InactivityConfig implements WebMvcConfigurer {

    private final InactivityInterceptor interceptor;

    public InactivityConfig(InactivityInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}

@Component
class InactivityInterceptor implements HandlerInterceptor {
    private final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        lastActivity.set(System.currentTimeMillis());
        return true;
    }

    public long getLastActivity() {
        return lastActivity.get();
    }
}

@Component
class InactivityMonitor {
    private final ApplicationContext context;
    private final InactivityInterceptor interceptor;
    
    @Value("${server.inactivity-timeout:0}")
    private int timeoutMinutes;

    public InactivityMonitor(ApplicationContext context, InactivityInterceptor interceptor) {
        this.context = context;
        this.interceptor = interceptor;
    }

    @Scheduled(fixedRate = 60000) // Verifica a cada minuto
    public void checkInactivity() {
        if (timeoutMinutes <= 0) return;

        long idleTime = System.currentTimeMillis() - interceptor.getLastActivity();
        if (idleTime > (long) timeoutMinutes * 60 * 1000) {
            System.out.println("Inatividade detectada por " + timeoutMinutes + " minutos. Encerrando servidor.");
            SpringApplication.exit(context, () -> 0);
        }
    }
}
