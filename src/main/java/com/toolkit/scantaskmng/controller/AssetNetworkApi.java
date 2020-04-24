package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.service.AssetInfoDataService;
import com.toolkit.scantaskmng.service.AssetNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value="/asset-network-info")
public class AssetNetworkApi {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResponseHelper responseHelper;
    @Autowired
    private AssetNetworkService assetNetworkService;
    @Autowired
    AssetInfoDataService assetInfoDataService;


    /**
     * 1.1 Get asset's all infos or some designated infos
     * @param ip
     * @return
     */
    @RequestMapping(value="/delay", method = RequestMethod.GET)
    @ResponseBody
    public Object getDelayInfo(@RequestParam("type") String type, @RequestParam("ip") String ip) {
        return assetNetworkService.getDelayInfo(type,ip);
    }

    /**
     * 获取流量数据
     * @param assetUuid
     * @param types
     * @param secondTime
     * @return
     */
    @GetMapping(value="/get-network")
    @ResponseBody
    public Object getNetwork(@RequestParam("asset_uuid") String assetUuid,
                             @RequestParam("types") String types,
                             @RequestParam("second_time") String secondTime) {
        return assetInfoDataService.startNetworkTask(assetUuid, types, secondTime);
    }

    /**
     * 停止获取Network数据
     * @param assetUuid
     * @return
     */
    @GetMapping(value="/stop-get-network")
    @ResponseBody
    public Object stopGetNetwork(@RequestParam("asset_uuid") String assetUuid) {
        return assetInfoDataService.stopNetWorkTask(assetUuid);
    }

}
