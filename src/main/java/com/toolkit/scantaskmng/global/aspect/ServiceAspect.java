package com.toolkit.scantaskmng.global.aspect;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.algorithm.AESEncrypt;
import com.toolkit.scantaskmng.global.algorithm.RSAEncrypt;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Aspect
public class ServiceAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${encry.switch}")
    private String encrySwitch;  // 加密开关 on/off

//    private final String ExpGetResultDataPonit = "execution(* com.toolkit.scantaskmng.global.response..*.*(..))";  // 拦截这个包下面所有方法
    private final String ExpGetResultDataPonit = "execution(* com.toolkit.scantaskmng.global.response.ResponseHelper.success(..))";  // 拦截这个包下面的该类下面的该方法

    /**
     * 定义切入点,拦截指定的方法
     */
    @Pointcut(ExpGetResultDataPonit)
    public void excuteService() {

    }

    /**
     * 执行方法前的拦截方法
     * @param joinPoint
     */
    @Before("excuteService()")
    public void doBeforeMethod(JoinPoint joinPoint) {
        logger.info("前置拦截通知，我将要执行一个方法了doBeforeMethod()");

        try {
            if ("ON".equals(encrySwitch)) {  // 加密开关 on/off
                Object[] args = joinPoint.getArgs();
                Map<String,Object> keyMap = RSAEncrypt.initKey();
                //私钥
                byte[] privateKey = RSAEncrypt.getPrivateKey(keyMap);

                //数据加密
                for (Object arg : args) {
                    String orgVal = arg.toString();

                    JSONObject jsonObj = (JSONObject) arg;
                    jsonObj.clear();

                    orgVal = AESEncrypt.encrypt(orgVal, AESEncrypt.SYM_KEY);

                    jsonObj.put("org_val", orgVal);
                    jsonObj.put("sign", RSAEncrypt.sign(orgVal, privateKey));

                    arg = jsonObj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 后置最终通知（目标方法只要执行完了就会执行后置通知方法）
     */
    @AfterReturning("excuteService()")
    public void doAfterAdvice(JoinPoint joinPoint) {
        logger.info("执行后置方法doAfterAdvice()");
    }

}
