package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.sun.org.apache.xml.internal.security.utils.Base64;
//import java.util.Base64;
import com.toolkit.scantaskmng.global.algorithm.AESEncrypt;
import com.toolkit.scantaskmng.global.algorithm.Base64Coding;
import com.toolkit.scantaskmng.global.algorithm.RSAEncrypt;
import com.toolkit.scantaskmng.global.enumeration.ErrorCodeEnum;
import com.toolkit.scantaskmng.global.response.ResponseHelper;
import com.toolkit.scantaskmng.global.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import oshi.software.os.OSFileStore;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.sql.Timestamp;
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
    public Object getFingerprint(String types) {
        JSONObject retObj = new JSONObject();

        if (!StringUtils.isValid(types)) {
            types = "CPU,Network,SoundCards,Disks,Memory";  // 硬件、OS系统、应用软件（可选）、服务、网络（不含IP）、配置
        }

        try {
            Object responseObj = assetInfoDataService.fetchAssetInfo(types, true);

            JSONObject jsonMsg = (JSONObject) JSONObject.toJSON(responseObj);
            if (jsonMsg != null) {

                retObj.put("ComputerSystem", jsonMsg.get("ComputerSystem"));

                Object soundCards = jsonMsg.get("SoundCards");
                if (soundCards != null) {
                    retObj.put("SoundCards", soundCards);
                }

                Object processorObj = jsonMsg.get("CPU");
                if (processorObj != null) {
                    JSONObject processor = (JSONObject) JSONObject.toJSON(processorObj);
                    processor.remove("systemCpuLoadTicks");
                    processor.remove("systemCpuLoadBetweenTicks");
                    processor.remove("processorCpuLoadBetweenTicks");
                    processor.remove("processorCpuLoadTicks");
                    processor.remove("systemCpuLoad");
                    processor.remove("systemUptime");
                    processor.remove("contextSwitches");
                    processor.remove("interrupts");
                    processor.remove("usedPercentTotal");
                    processor.remove("freePercentTotal");
                    retObj.put("CPU", processor);
                }

                Object networkObjs = jsonMsg.get("Network");
                if (networkObjs != null) {
                    JSONArray networkArray = (JSONArray) networkObjs;
                    for(Object obj : networkArray) {
                        JSONObject networkObj = (JSONObject) JSONObject.toJSON(obj);
                        networkObj.remove("IPv4");
                        networkObj.remove("IPv6");
                        networkObj.remove("packetsRecv");
                        networkObj.remove("bytesRecv");
                        networkObj.remove("packetsSent");
                        networkObj.remove("bytesSent");
                        networkObj.remove("timeStamp");
                    }

                    retObj.put("Network", networkArray);
                }

                Object disksObj = jsonMsg.get("Disks");
                if (disksObj != null) {
                    JSONObject disk = (JSONObject) JSONObject.toJSON(disksObj);
                    disk.remove("usedPercentTotal");
                    disk.remove("freePercentTotal");
                    disk.remove("freeTotal");
                    disk.remove("usedTotal");
                    disk.remove("");

                    Object fileStoreObjs = disk.get("fileStores");
                    if (fileStoreObjs != null) {

                        OSFileStore[] fileStores = (OSFileStore[]) fileStoreObjs;
                        disk.remove("fileStores");

                        JSONArray fileStoreArray = new JSONArray();
                        for(OSFileStore obj : fileStores) {
                            JSONObject fileStoreObj = (JSONObject) JSONObject.toJSON(obj);
                            fileStoreObj.remove("usableSpace");
                            fileStoreObj.remove("totalSpace");
                            fileStoreArray.add(fileStoreObj);
                        }

                        disk.put("fileStores", fileStoreArray);
                    }

                    retObj.put("Disks", disk);
                }

                Object memoryObj = jsonMsg.get("Memory");
                if (memoryObj != null) {
                    JSONObject memory = (JSONObject) JSONObject.toJSON(memoryObj);
                    memory.remove("usedPercentTotal");
                    memory.remove("freePercentTotal");
                    memory.remove("swapUsed");
                    memory.remove("available");
                    memory.remove("swapPagesIn");

                    retObj.put("Memory", memory);
                }

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
            Object responseObj = assetInfoDataService.fetchAssetInfo("CPU", true);

            JSONObject jsonObj = (JSONObject) JSONObject.toJSON(responseObj);
            if (jsonObj != null) {
                jsonObj.put("authenticate", "success");

                String plainData = jsonObj.toJSONString();

                String privateKey = this.readerCode(KEYSTORE_URL + KEYSTORE_NAME);
                String symKey = this.readerCode(KEYSTORE_URL + SYM_KEY);
                String encrypt = AESEncrypt.encrypt(plainData, symKey);  // 加密

                String sign = RSAEncrypt.sign(encrypt, new BASE64Decoder().decodeBuffer(privateKey));  // 签名

                retObj.put("plain_data", plainData);  // 明文
                retObj.put("cipher_data", encrypt);  // 密文
                retObj.put("sign", sign);
            }

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
