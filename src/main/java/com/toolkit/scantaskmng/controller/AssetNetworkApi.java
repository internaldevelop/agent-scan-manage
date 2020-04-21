package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.global.response.ResponseHelper;
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

}
