package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.service.AuthenticateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*",maxAge = 3600)
@RestController
@RequestMapping(value="/authenticate")
public class AuthenticateApi {
    @Autowired
    AuthenticateService authenticateService;

    /**
     * 1.3 设备指纹信息获取
     * @return
     */
    @GetMapping(value="/get-fingerprint")
    @ResponseBody
    public Object getFingerprint(@RequestParam(required = false, value = "types") String types) {
        return authenticateService.getFingerprint(types);
    }

    /**
     * 1.4 设备公钥信息获取
     * @return
     */
    @GetMapping(value="/get-public-key")
    @ResponseBody
    public Object getPublicKey(@RequestParam("sym_key") String symKey) {
        return authenticateService.getPublicKey(symKey);
    }

    /**
     * 认证
     * @return
     */
    @GetMapping(value="/authenticate")
    @ResponseBody
    public Object authenticate() {
        return authenticateService.authenticate();
    }

    /**
     * 保存sym_key
     * @param symKey
     * @return
     */
    @GetMapping(value="/save-sym-key")
    @ResponseBody
    public Object saveSymKey(@RequestParam("sym_key") String symKey){
        return authenticateService.saveSymKey(symKey);
    }
}
