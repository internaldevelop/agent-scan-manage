package com.toolkit.scantaskmng.service.auto;

import com.toolkit.scantaskmng.service.AuthenticateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTask {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Value("${main.service.name}")
    private String MAIN_SERVICE_NAME;  //主服务名 embed-terminal

//    每隔5秒执行一次：*/5 * * * * ?
//    每隔1分钟执行一次：0 */1 * * * ?
//    每天23点执行一次：0 0 23 * * ?
//    每天凌晨1点执行一次：0 0 1 * * ?
//    每月1号凌晨1点执行一次：0 0 1 1 * ?
//    每月最后一天23点执行一次：0 0 23 L * ?
//    每周星期天凌晨1点实行一次：0 0 1 ? * L
//    在26分、29分、33分执行一次：0 26,29,33 * * * ?
//    每天的0点、13点、18点、21点都执行一次：0 0 0,13,18,21 * * ?

    @Scheduled(cron = "0 0 1 * * ?")
    public void authenticate(){
        logger.info("自动认证start ip:" + mainServiceIp + " service_name:" + MAIN_SERVICE_NAME);
        AuthenticateService authenticateService = new AuthenticateService();
        boolean authenticateFlag = authenticateService.autoAuthenticate(mainServiceIp, MAIN_SERVICE_NAME);
        logger.info("认证结果:" + authenticateFlag);

    }

}
