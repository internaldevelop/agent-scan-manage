package com.toolkit.scantaskmng.dao.mybatis;

import com.toolkit.scantaskmng.bean.po.AssetPerfDataPo;
import com.toolkit.scantaskmng.bean.po.PolicyPo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AssetNetworkMapper {
    /**
     * 新建一条策略记录
     * @param policy 策略的所有参数
     * @return >=1：成功；<=0：失败；
     */
    @Insert("INSERT INTO policies( \n" +
            "uuid, name, code, \n" +
            "group_uuid, type, risk_level, \n" +
            "solutions, create_user_uuid, status, \n" +
            "os_type, baseline, run_mode, \n" +
            "run_contents, consume_time, asset_uuid, \n" +
            "create_time) \n" +
            "VALUES ( \n" +
            "#{uuid}, #{name}, #{code}, \n" +
            "#{group_uuid}, #{type}, #{risk_level}, \n" +
            "#{solutions}, #{create_user_uuid}, #{status}, \n" +
            "#{os_type}, #{baseline}, #{run_mode}, \n" +
            "#{run_contents}, #{consume_time}, #{asset_uuid}, \n" +
            "#{create_time, jdbcType=TIMESTAMP}) ")
    int addPolicy(PolicyPo policy);

    /**
     * 根据UUID，获取指定的策略记录
     * @param policyUuid 指定的策略 UUID
     * @return PolicyProps 策略记录的全部数据
     */
    @Select("SELECT * FROM policies p WHERE p.uuid=#{uuid} AND p.status>0 ")
    PolicyPo getPolicyByUuid(@Param("uuid") String policyUuid);

    /**
     *  根据group uuid获取所在组所有的策略
     * @param policyGroupUuid
     * @return
     */
    @Select("SELECT * FROM policies p WHERE p.group_uuid=#{group_uuid} AND p.status>0 ")
    List<PolicyPo> getPoliciesByGroup(@Param("group_uuid") String policyGroupUuid);

    /**
     * 更新指定策略记录的状态
     * @param policyUuid 策略的 UUID
     * @param status 新的状态
     * @return >=1：成功；<=0：失败；
     */
    @Update("UPDATE policies p SET " +
            "p.status=#{status} " +
            "WHERE " +
            "p.uuid=#{uuid} ")
    int  updateStatus(@Param("uuid") String policyUuid, @Param("status") int status);

    /**
     * 资产使用率添加
     * @param assetPerfDataPo
     * @return
     */
    @Insert(" INSERT INTO asset_perf_data ( `uuid`, `asset_uuid`, `cpu_used_percent`, `memory_used_percent`, `disk_used_percent`, `create_time` )\n" +
            " VALUES\n" +
            " (#{uuid}, #{asset_uuid}, #{cpu_used_percent}, #{memory_used_percent}, #{disk_used_percent}, #{create_time, jdbcType=TIMESTAMP})")
    int addAssetPerfData(AssetPerfDataPo assetPerfDataPo);

    /**
     * 根据ip查询资产UUID
     * @param asset_ips
     * @return
     */
    @Select("<script> " +
            "SELECT GROUP_CONCAT(uuid) FROM assets where ip IN " +
            "<foreach item='item' index='index' collection = 'asset_ips' open='(' separator=',' close=')'>\n" +
            "\t#{item}\n" +
            "</foreach> " +
            "</script>")
    String getAssetUUid(@Param("asset_ips") List<String> asset_ips);

}
