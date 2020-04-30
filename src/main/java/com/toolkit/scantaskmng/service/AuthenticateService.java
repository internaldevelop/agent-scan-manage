package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
//import java.util.Base64;
import com.toolkit.scantaskmng.global.algorithm.AESEncrypt;
import com.toolkit.scantaskmng.global.algorithm.Base64Coding;
import com.toolkit.scantaskmng.global.algorithm.RSAEncrypt;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.util.Map;

@Component
public class AuthenticateService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AssetInfoDataService assetInfoDataService;

    private static final String CHARSET_UDT8 = "UTF-8";  // 编码格式

    @Autowired
    RestTemplate restTemplate;

    @Value("${main.service.ip}")
    public String mainServiceIp;

    @Autowired
    ResponseHelper responseHelper;

    private String KEYSTORE_URL = "./sign_file/";  // 秘钥路径
    private String KEYSTORE_NAME = "keystore";  // 秘钥文件名称
    private String SYM_KEY = "symkey";  // 秘钥文件名称

    /**
     * 设备指纹信息获取
     * @return
     */
    public Object getFingerprint() {
        JSONObject retObj = new JSONObject();

        String types = "CPU";
        try {
            Object responseObj = assetInfoDataService.fetchAssetInfo(types);

            JSONObject jsonMsg = (JSONObject) JSONObject.toJSON(responseObj);
            if (jsonMsg != null) {
                JSONObject assembleObj = new JSONObject();

                assembleObj.put("os", jsonMsg.get("os"));

                JSONObject computerSystem = (JSONObject) JSONObject.toJSON(jsonMsg.get("ComputerSystem"));
                if (computerSystem != null)
                    assembleObj.put("ComputerSystem_baseboard", computerSystem.get("baseboard"));

                JSONObject CPU = (JSONObject) JSONObject.toJSON(jsonMsg.get("CPU"));
                if (CPU != null) {
                    assembleObj.put("CPU_processorID", CPU.get("processorID"));
                    assembleObj.put("CPU_name", CPU.get("name"));
                    assembleObj.put("CPU_logicalProcessorCount", CPU.get("logicalProcessorCount"));
                }
                retObj.put("device_fingerprint", assembleObj.toJSONString());
                retObj.put("sys_type", System.getProperty("os.name"));
                retObj.put("sys_version", System.getProperty("os.version"));
                retObj.put("sys_name", System.getProperty("user.name"));

                return responseHelper.success(retObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseHelper.error(ErrorCodeEnum.ERROR_INTERNAL_ERROR);

    }

    /**
     * 保存symKey并获取公钥
     * @return
     */
    public Object getPublicKey(String symKey) {
        JSONObject jsonObj = new JSONObject();
        try {
            this.createWrite(KEYSTORE_URL, SYM_KEY, symKey);  // 保存symKey  写入文件

            Map<String,Object> keyMap = RSAEncrypt.initKey();
            //公钥
            byte[] publicKey = RSAEncrypt.getPublicKey(keyMap);
            //私钥
            byte[] privateKey = RSAEncrypt.getPrivateKey(keyMap);
//            jsonObj.put("public_key", Base64.encode(publicKey));
            jsonObj.put("public_key", Base64Coding.encode(publicKey));


            this.createWrite(KEYSTORE_URL, KEYSTORE_NAME, Base64Coding.encode(privateKey));  // 写入文件

        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseHelper.success(jsonObj);
    }

    /**
     * 生成并写入文件
     * @param fileUrl
     * @param fileName
     * @param content
     * @return
     * @throws IOException
     */
    public boolean createWrite(String fileUrl, String fileName, String content) throws IOException {
        boolean retFlag = false;  // 返回值

        File dir = new File(fileUrl);
        if (!dir.exists()) {
            dir.mkdirs();  // mkdirs创建多级目录
        }
        File checkFile = new File(fileUrl + fileName);
        FileWriter writer = null;
        try {
            if (!checkFile.exists()) {
                checkFile.createNewFile(); // 创建目标文件
            }
            writer = new FileWriter(checkFile, false);  // true时为追加模式，false或缺省则为覆盖模式
            writer.append(content);  // 内容
            writer.flush();

            retFlag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer)
                writer.close();
        }
        return retFlag;
    }

    /**
     * 读取文件
     * @param fileUrlName
     * @return
     */
    public String readerCode(String fileUrlName) {
        String retStr = "";
        StringBuffer retStrbuff = new StringBuffer();  // 读取文件内容
        try {
            File file = new File(fileUrlName);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), CHARSET_UDT8);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    retStrbuff.append("\n").append(lineTxt);
                }
                read.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        retStr = retStrbuff.toString().replaceFirst("\n", "");

        return retStr;
    }

    /**
     * 认证
     * @return
     */
    public Object authenticate() {
        JSONObject retObj = new JSONObject();
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("authenticate", "success");

            String privateKey = this.readerCode(KEYSTORE_URL + KEYSTORE_NAME);
            String symKey = this.readerCode(KEYSTORE_URL + SYM_KEY);
            String encrypt = AESEncrypt.encrypt(jsonObj.toJSONString(), symKey);  // 加密

            String sign = RSAEncrypt.sign(encrypt, new BASE64Decoder().decodeBuffer(privateKey));  // 签名

            retObj.put("org_data", encrypt);
            retObj.put("sign", sign);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseHelper.success(retObj);
    }

    /**
     * 保存 sym_key
     * @param symKey
     * @return
     */
    public Object saveSymKey(String symKey){
        try {
            this.createWrite(KEYSTORE_URL, SYM_KEY, symKey);  // 写入文件
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseHelper.success();
    }
}
