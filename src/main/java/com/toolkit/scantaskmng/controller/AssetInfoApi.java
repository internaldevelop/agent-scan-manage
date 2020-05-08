package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import com.toolkit.scantaskmng.service.AssetInfoDataService;
import com.toolkit.scantaskmng.service.AssetInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value="/asset-info")
public class AssetInfoApi {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResponseHelper responseHelper;
    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired
    AssetInfoDataService assetInfoDataService;

    /**
     * 1.1 Get asset's all infos or some designated infos
     * @param types comma split string, example: "CPU,Memory,Disks"
     * @return
     */
    @RequestMapping(value="/acquire", method = RequestMethod.GET)
    @ResponseBody
    public Object getAssetInfo(@RequestParam("types") String types) {
        return assetInfoService.fetchAssetInfo(types);
    }

    /**
     * 1.2 Get system property
     * @param propName
     * @return
     */
    @RequestMapping(value = "/system-prop")
    @ResponseBody
    public Object getSystemProps(@RequestParam("prop") String propName) {
//        return MyUtils.getWorkingPath();
        return SystemUtils.getProp(propName);
    }

    /**
     * 获取资源
     * @param types
     * @return
     */
    @GetMapping(value = "/get-resources")
    @ResponseBody
    public Object getResources(@RequestParam("types") String types) {
        return assetInfoDataService.getAssetInfo(types);
    }

    /**
     * 启动任务获取资源
     * @param types
     * @param secondTime
     * @return
     */
    @GetMapping(value = "/start-task-acquire")
    @ResponseBody
    public Object startTaskAcquire(@RequestParam("asset_uuid") String assetUuid,
                                   @RequestParam("types") String types,
                                   @RequestParam("second_time") String secondTime) {
        return assetInfoDataService.startTask(assetUuid, types, secondTime);
    }

    /**
     * 停止任务获取资源
     * @param assetUuid
     * @return
     */
    @GetMapping(value="/stop-task-acquire")
    @ResponseBody
    public Object stopGetNetwork(@RequestParam("asset_uuid") String assetUuid) {
        return assetInfoDataService.stopTask(assetUuid);
    }

    @GetMapping(value="/verify-network")
    @ResponseBody
    public Object verifyNetwork() {
        return responseHelper.success();
    }

}
