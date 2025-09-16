package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaInfo {
    private String code;         // 验证码内容（小写）
    private long expireAt;       // 过期时间戳（毫秒）
    // 可选：如果需要返回图片，可以加上
    private String imageBase64;  // 验证码图片的 base64 字符串
    // 可选：如果需要存储 id，也可加上
    private String captchaId;
}
