package com.toolkit.scantaskmng.dao.mybatis;

import com.toolkit.scantaskmng.bean.po.PolicyPo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PoliciesMapper {
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
     * 获得所有有效的策略记录
     * @return PolicyProps 的集合
     */
    @Select("SELECT * FROM policies p WHERE p.status>0 ")
    List<PolicyPo> allPolicies();

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
     * 更新指定的策略记录
     * @param policy 策略的数据
     * @return >=1：成功；<=0：失败；
     */
    @Update("UPDATE policies p SET " +
            "name=#{name}, code=#{code}, group_uuid=#{group_uuid}, \n" +
            "type=#{type}, risk_level=#{risk_level}, solutions=#{solutions}, \n" +
            "create_user_uuid=#{create_user_uuid}, status=#{status}, os_type=#{os_type}, \n" +
            "baseline=#{baseline}, run_mode=#{run_mode}, run_contents=#{run_contents}, \n" +
            "consume_time=#{consume_time}, asset_uuid=#{asset_uuid}, \n" +
            "create_user_uuid=#{create_user_uuid} \n" +
            "WHERE \n" +
            "p.uuid=#{uuid} AND p.status>0  ")
    int updatePolicy(PolicyPo policy);

    /**
     * 永久删除一条策略记录
     * @param policyUuid 策略的 UUID
     * @return >=1：成功；<=0：失败；
     */
    @Delete("DELETE FROM policies WHERE uuid=#{uuid} ")
    int deletePolicy(@Param("uuid") String policyUuid);

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
}
