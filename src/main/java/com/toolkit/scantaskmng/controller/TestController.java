package com.toolkit.scantaskmng.controller;

import com.alibaba.fastjson.JSONObject;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.MyUtils;
import com.toolkit.scantaskmng.global.utils.SystemUtils;
import com.toolkit.scantaskmng.service.mq.TopicSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@RequestMapping(value = "/test")
public class TestController {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TopicSender topicSender;
    @Autowired
    ResponseHelper responseHelper;

    /**
     * A.1 获取操作系统信息
     * @return
     */
    @RequestMapping(value = "/os", method = RequestMethod.GET)
    @ResponseBody
    public Object getOsInfo() {
        JSONObject jsonOS = new JSONObject();
        jsonOS.put("OS Name", SystemUtils.getOsName());
        jsonOS.put("OS Arch", SystemUtils.getOsArch());
        jsonOS.put("OS Version", SystemUtils.getOsVersion());
        return responseHelper.success(jsonOS);
    }

    @RequestMapping(value = "/all-sys-props", method = RequestMethod.GET)
    @ResponseBody
    public Object getAllSystemProps() {
        return responseHelper.success(SystemUtils.sysProps);
    }

    @RequestMapping(value = "/rabbitmq", method = RequestMethod.POST)
    @ResponseBody
    public Object sendRabbitMsg(@RequestParam("channel") String channel, @RequestParam("message") String message) {
        topicSender.send(channel, message);
        JSONObject jsonResp = new JSONObject();
        jsonResp.put("timestamp", MyUtils.getCurrentSystemTimestamp());
        jsonResp.put("result", "OK");
        return responseHelper.success(jsonResp);
    }
}
