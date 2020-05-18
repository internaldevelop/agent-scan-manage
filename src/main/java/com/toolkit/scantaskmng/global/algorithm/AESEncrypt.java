package com.toolkit.scantaskmng.global.algorithm;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AESEncrypt {

    private static Logger logger = Logger.getLogger(AESEncrypt.class.getName());

    private static final String KEY_AES = "AES";  //非对称密钥算法
    private static final String CHARSET_TING = "UTF-8";  // 编码格式
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";  // 默认的加密算法

    public static final String SYM_KEY = "f52da1cfdf5c8b0c1376c7aaf1ebf4a94a9a0b94092c7d5fc844d0f7db879ef3";  // 秘钥

    /**
     * AES 加密操作
     * @param content 待加密内容
     * @param symKey 加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String content, String symKey) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器
            byte[] byteContent = content.getBytes(CHARSET_TING);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(symKey));// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(byteContent);// 加密

            return Base64.encodeBase64String(result);//通过Base64转码返回
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
        return null;
    }


    /**
     * AES 解密操作
     * @param content
     * @param symKey
     * @return
     */
    public static String decrypt(String content, String symKey) {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(symKey));
            //执行操作
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));

            return new String(result, CHARSET_TING);
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
        return null;
    }

    /**
     * 生成加密秘钥
     * @return
     */
    private static SecretKeySpec getSecretKey(final String symKey) {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_AES);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(symKey.getBytes());
            //AES 要求密钥长度为 128
            kg.init(128, secureRandom);
            //生成一个密钥
            SecretKey secretKey = kg.generateKey();
            return new SecretKeySpec(secretKey.getEncoded(), KEY_AES);// 转换为AES专用密钥
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, null, e);
        }
        return null;
    }
}
