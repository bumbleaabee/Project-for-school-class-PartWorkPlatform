package org.campus.partworkback.utils;

import jakarta.annotation.PostConstruct;
import org.campus.partworkback.pojo.CaptchaInfo;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CaptchaUtil {
    // 验证码缓存，key为captchaId，value为CaptchaInfo
    private final ConcurrentHashMap<String, CaptchaInfo> captchaMap = new ConcurrentHashMap<>();
    // 定时清理过期验证码
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }

    // 生成随机验证码字符串
    public String generateRandomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 生成验证码图片并转为 base64
    public String generateImageBase64(String code) {
        int w = 120, h = 40;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.BLACK);
        g.drawString(code, 20, 30);
        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            return "";
        }
    }

    // 生成并存储验证码，返回 CaptchaInfo（包含id、code、图片、过期时间）
    public CaptchaInfo generateAndStoreCaptcha(int codeLen, long expireMillis) {
        String captchaId = UUID.randomUUID().toString();
        String code = generateRandomCode(codeLen);
        String imageBase64 = generateImageBase64(code);
        long expireAt = System.currentTimeMillis() + expireMillis;
        CaptchaInfo info = new CaptchaInfo(code.toLowerCase(), expireAt, imageBase64, captchaId);
        captchaMap.put(captchaId, new CaptchaInfo(code.toLowerCase(), expireAt, null, null)); // 缓存只存校验用字段
        return info;
    }

    // 校验验证码
    public boolean validateCaptcha(String captchaId, String userInput) {
        CaptchaInfo info = captchaMap.get(captchaId);
        if (info == null || info.getExpireAt() < System.currentTimeMillis()) {
            captchaMap.remove(captchaId);
            return false;
        }
        boolean ok = info.getCode().equalsIgnoreCase(userInput);
        captchaMap.remove(captchaId); // 一次性
        return ok;
    }

    // 定时清理过期验证码
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        captchaMap.entrySet().removeIf(e -> e.getValue().getExpireAt() < now);
    }
}
