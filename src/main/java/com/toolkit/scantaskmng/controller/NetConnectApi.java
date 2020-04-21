package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.global.bean.ResponseBean;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import com.toolkit.scantaskmng.service.AssetInfoService;
import com.toolkit.scantaskmng.service.NetConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value="/netconnect")
public class NetConnectApi {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ResponseHelper responseHelper;
    @Autowired
    private NetConnectService netconnectService;

    /**
     *1.1 网络路径连通性测试
     * @param ip 连通测试 IP
     * @return payload
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public @ResponseBody
    Object netConnect(@RequestParam("ip") String ip) {
        ResponseBean response = netconnectService.ping(ip);
        return response;
//        return netconnectService.ping(ip);
    }

    /**
     * 测试指定url访问时长
     * @param url
     * @return
     */
    @RequestMapping(value = "/url-resp", method = RequestMethod.GET)
    public @ResponseBody Object urlResp(@RequestParam("url") String url) {
        ResponseBean response = netconnectService.urlResp(url);
        return response;
    }

}
