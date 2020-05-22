package com.toolkit.scantaskmng.service.auto;

import com.toolkit.scantaskmng.global.utils.StringUtils;
import com.toolkit.scantaskmng.service.AssetInfoDataService;
import com.toolkit.scantaskmng.service.AssetNetworkService;
import com.toolkit.scantaskmng.service.AuthenticateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthenticateService authenticateService;
    @Autowired
    private AssetNetworkService assetNetworkService;
    @Autowired
    private AssetInfoDataService assetInfoDataService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String assetUuid = authenticateService.getUuid();

        if (StringUtils.isValid(assetUuid)) {
            logger.info("启动抓取数据包....");
            assetNetworkService.getPacket(assetUuid);

            logger.info("启动获取资源信息CPU,Memory,Disks,Network....");  // CPU,Memory,Disks,Network
            String types = "CPU,Memory,Disks,Network";
            assetInfoDataService.startTask(assetUuid, types, "3", "0");
        }

    }
}
