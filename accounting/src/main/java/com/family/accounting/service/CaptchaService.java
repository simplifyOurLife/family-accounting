package com.family.accounting.service;

import com.family.accounting.dto.CaptchaVO;
import com.family.accounting.entity.Captcha;
import com.family.accounting.mapper.CaptchaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * 图片验证码服务类
 */
@Service
public class CaptchaService {

    /**
     * 验证码字符集（排除易混淆字符：0/O、1/I/L）
     */
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 图片宽度
     */
    private static final int IMAGE_WIDTH = 120;

    /**
     * 图片高度
     */
    private static final int IMAGE_HEIGHT = 40;

    /**
     * 验证码有效期（分钟）
     */
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;

    /**
     * 干扰线数量
     */
    private static final int NOISE_LINE_COUNT = 5;

    /**
     * 噪点数量
     */
    private static final int NOISE_DOT_COUNT = 50;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private CaptchaMapper captchaMapper;

    /**
     * 生成图片验证码
     *
     * @return 验证码VO（包含captchaKey和base64编码的图片）
     */
    @Transactional
    public CaptchaVO generateCaptcha() {
        // 生成验证码内容
        String code = generateRandomCode();

        // 生成唯一标识
        String captchaKey = UUID.randomUUID().toString();

        // 计算过期时间
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRE_MINUTES);

        // 保存到数据库
        Captcha captcha = new Captcha();
        captcha.setCaptchaKey(captchaKey);
        captcha.setCode(code);
        captcha.setExpiredAt(expiredAt);
        captchaMapper.insert(captcha);

        // 生成图片
        String imageBase64 = generateCaptchaImage(code);

        // 返回结果
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaKey(captchaKey);
        vo.setCaptchaImage(imageBase64);
        return vo;
    }


    /**
     * 生成随机验证码字符串
     *
     * @return 4位随机字符串
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = secureRandom.nextInt(CAPTCHA_CHARS.length());
            sb.append(CAPTCHA_CHARS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片
     *
     * @param code 验证码内容
     * @return base64编码的PNG图片
     */
    private String generateCaptchaImage(String code) {
        // 创建图片缓冲区
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 填充背景色（浅色）
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 绘制干扰线
        drawNoiseLines(g2d);

        // 绘制噪点
        drawNoiseDots(g2d);

        // 绘制验证码字符
        drawCaptchaText(g2d, code);

        g2d.dispose();

        // 转换为base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 绘制干扰线
     */
    private void drawNoiseLines(Graphics2D g2d) {
        for (int i = 0; i < NOISE_LINE_COUNT; i++) {
            g2d.setColor(getRandomColor(100, 180));
            g2d.setStroke(new BasicStroke(1.5f));
            int x1 = secureRandom.nextInt(IMAGE_WIDTH);
            int y1 = secureRandom.nextInt(IMAGE_HEIGHT);
            int x2 = secureRandom.nextInt(IMAGE_WIDTH);
            int y2 = secureRandom.nextInt(IMAGE_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制噪点
     */
    private void drawNoiseDots(Graphics2D g2d) {
        for (int i = 0; i < NOISE_DOT_COUNT; i++) {
            g2d.setColor(getRandomColor(100, 200));
            int x = secureRandom.nextInt(IMAGE_WIDTH);
            int y = secureRandom.nextInt(IMAGE_HEIGHT);
            g2d.fillOval(x, y, 2, 2);
        }
    }


    /**
     * 绘制验证码文字
     */
    private void drawCaptchaText(Graphics2D g2d, String code) {
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 28);
        g2d.setFont(font);

        // 计算每个字符的宽度
        int charWidth = IMAGE_WIDTH / (CAPTCHA_LENGTH + 1);
        int startX = charWidth / 2;

        for (int i = 0; i < code.length(); i++) {
            // 随机颜色（深色）
            g2d.setColor(getRandomColor(20, 100));

            // 随机旋转角度（-15到15度）
            double angle = (secureRandom.nextDouble() - 0.5) * Math.PI / 6;

            // 计算字符位置
            int x = startX + i * charWidth;
            int y = IMAGE_HEIGHT / 2 + 10;

            // 应用旋转变换
            AffineTransform originalTransform = g2d.getTransform();
            g2d.rotate(angle, x, y - 5);

            // 绘制字符
            g2d.drawString(String.valueOf(code.charAt(i)), x, y);

            // 恢复原始变换
            g2d.setTransform(originalTransform);
        }
    }

    /**
     * 获取随机颜色
     *
     * @param min 颜色分量最小值
     * @param max 颜色分量最大值
     * @return 随机颜色
     */
    private Color getRandomColor(int min, int max) {
        int r = min + secureRandom.nextInt(max - min);
        int g = min + secureRandom.nextInt(max - min);
        int b = min + secureRandom.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 验证图片验证码
     * 验证后立即删除验证码记录（无论成功失败）
     * 验证时大小写不敏感
     *
     * @param captchaKey  验证码唯一标识
     * @param captchaCode 用户输入的验证码
     * @return 验证是否成功
     */
    @Transactional
    public boolean verifyCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.isEmpty() || 
            captchaCode == null || captchaCode.isEmpty()) {
            return false;
        }

        // 查询验证码
        Captcha captcha = captchaMapper.findByCaptchaKey(captchaKey);

        // 无论验证成功与否，都删除验证码记录（一次性使用）
        captchaMapper.deleteByCaptchaKey(captchaKey);

        // 验证码不存在
        if (captcha == null) {
            return false;
        }

        // 验证码已过期
        if (captcha.getExpiredAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 大小写不敏感比较
        return captcha.getCode().equalsIgnoreCase(captchaCode);
    }

    /**
     * 定时清理过期验证码（每10分钟执行一次）
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void cleanExpiredCaptchas() {
        captchaMapper.deleteExpired(LocalDateTime.now());
    }
}
