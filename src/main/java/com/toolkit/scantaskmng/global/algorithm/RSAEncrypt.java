package com.toolkit.scantaskmng.global.algorithm;

//import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAEncrypt {

    //非对称密钥算法
    private static final String KEY_ALGORITHM = "RSA";
    /**
     * 密钥长度，DH算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     * */
    private static final int KEY_SIZE = 512;

    //公钥
    private static final String PUBLIC_KEY = "PUBLIC_KEY";

    //私钥
    private static final String PRIVATE_KEY = "PRIVATE_KEY";

    //算法
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    //编码
    private static final String CHARSETTING = "UTF-8";

    /**
     * 初始化密钥对
     * @return Map 甲方密钥的Map
     * */
    public static Map<String,Object> initKey() throws Exception{
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        System.out.println("系数："+publicKey.getModulus()+"  加密指数："+publicKey.getPublicExponent());
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//        System.out.println("系数："+privateKey.getModulus()+"解密指数："+privateKey.getPrivateExponent());
        //将密钥存储在map中
        Map<String,Object> keyMap = new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;

    }

    /**
     * 取得私钥
     * @param keyMap 密钥map
     * @return byte[] 私钥
     * */
    public static byte[] getPrivateKey(Map<String,Object> keyMap){
        Key key=(Key)keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取得公钥
     * @param keyMap 密钥map
     * @return byte[] 公钥
     * */
    public static byte[] getPublicKey(Map<String,Object> keyMap) throws Exception{
        Key key=(Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }

    /**
     * RSA签名
     * @param content 待签名数据
     * @param rsaPrivateKey 私钥
     * @return 签名值
     */
    public static String sign(String content, byte[] rsaPrivateKey) {
        String result = null;// 签名结果
        try {
            // 2.执行签名
            PKCS8EncodedKeySpec pkcS8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcS8EncodedKeySpec);

            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(privateKey);
            signature.update(content.getBytes(CHARSETTING));

//            result = Base64.encode(signature.sign());
            result = Base64Coding.encode(signature.sign());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
