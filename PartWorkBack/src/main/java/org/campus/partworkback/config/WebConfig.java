package org.campus.partworkback.config;

import org.campus.partworkback.Interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Value("${app.upload.base-dir:./uploads/}")
    private String uploadBaseDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/auth/captcha/image");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射：将 /uploads/** 映射到本地文件目录（用于头像等文件访问）
        // 注意：路径前缀 file: 必须保留；确保目录存在
        String location = "file:" + new File(uploadBaseDir).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 开发阶段允许本地前端端口跨域访问
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @PostConstruct
    public void ensureUploadDir() {
        File dir = new File(uploadBaseDir);
        if (!dir.exists()) {
            // 尝试创建目录
            dir.mkdirs();
        }
    }
}
