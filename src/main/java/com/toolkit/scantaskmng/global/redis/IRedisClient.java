package com.toolkit.scantaskmng.global.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis 接口
 */
public interface IRedisClient {

    /**
     * 添加一个对象
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, Serializable value);

    /**
     * 获取一个对象
     *
     * @param key
     * @return
     */
    public Serializable get(String key);
    /**
     * 通过选择数据库,获取一个对象
     *
     * @param dbIndex 数据库编码
     * @return
     */
    public Serializable get(String key, Integer dbIndex);

    /**
     * 删除一个Key
     *
     * @param key
     * @return
     */
    public boolean del(String key);

    /**
     * @param key
     * @param list
     * @return
     */
    public Boolean addList(String key, List<Serializable> list);

    /**
     * 获取列表的长度
     *
     * @param key
     * @return
     */
    public long getListLength(String key);

    /**
     * @param key
     * @return
     */
    public List<Serializable> getList(String key);

    /**
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<Serializable> getList(String key, int start, int end);

    /**
     * @param key
     * @return
     */
    public Boolean delList(String... key);

    /**
     * @param key
     * @param values
     * @return
     */
    public Boolean addListItems(String key, Serializable[] values);

    /**
     * @param key
     * @param indexes
     * @param values
     * @return
     */
    public Boolean setListItems(String key, int[] indexes, Serializable[] values);

    /**
     * @param key
     * @param values
     * @return
     */
    public Boolean delListItems(String key, Serializable[] values);

    /**
     * @param key
     * @param map
     * @return
     */
    public Boolean addMap(String key, Map<String, Serializable> map);

    /**
     * @param key
     * @return
     */
    public Map<String, Serializable> getMap(String key);

    /**
     *
     * @param key
     * @param fields
     * @return
     */
    public Serializable[] getMapItems(String key, String[] fields);

    /**
     *
     * @param key
     * @param field
     * @return
     */
    public Serializable getMapItem(String key, String field);

    /**
     * @param key
     * @return
     */
    public Boolean delMap(String... key);

    /**
     * @param key
     * @param mapkey
     * @param value
     * @return
     */
    public Boolean addMapValue(String key, String mapkey, Serializable value);

    /**
     * @param key
     * @param mapkey
     * @return
     */
    public Boolean delMapValue(String key, String mapkey);

    /**
     * @param key
     * @param set
     * @return
     */
    public Boolean addSet(String key, Set<Serializable> set);

    /**
     * @param key
     * @return
     */
    public Set<Serializable> getSet(String key);

    /**
     * @param key
     * @return
     */
    public Boolean delSet(String... key);

    /**
     * @param key
     * @param values
     * @return
     */
    public Boolean addSetItems(String key, Serializable[] values);

    /**
     * @param key
     * @param values
     * @return
     */
    public Boolean delSetItems(String key, Serializable[] values);

    /**
     * @param pattern
     * @return
     */
    public Set<String> getKeysSet(String pattern);

    /**
     * 对key设置定时存储
     *
     * @param key
     * @param seconds
     *            秒
     * @return
     */
    public Long expire(String key, int seconds);

    /**
     * 将key由定时存储调整为永久存储
     *
     * @param key
     * @return
     */
    public Long persist(String key);

    /**
     * 判断key的缓存时间
     *
     * @param key
     * @return -1 永久保存 -2 KEY不存在
     */
    public Long ttl(String key);

    /**
     * key 减少 decr ，value限制64位singed integer
     *
     * @param key
     *            如果key不存在，默认为0，如果对应的不是stirng或者Integer，那么返回错误信息
     * @param decr
     * @return
     */
    public Long decrby(String key, long decr);

    /**
     * key 增加 incr ，value限制64位singed integer
     *
     * @param key
     *            如果key不存在，默认为0，如果对应的不是stirng或者Integer，那么返回错误信息
     * @param incr
     * @return
     */
    public Long incrby(String key, long incr);

    /**
     * key 对应的值为 integer 或者 数字内容的字符串
     *
     * @param key
     * @return
     */
    public Long getNumber(String key);

    /**
     * key对应的值为 Long 或者 数字内容的字符串
     *
     * @param key
     * @param number
     * @return
     */
    public Long setNumber(String key, Long number);

    /**
     * 返回名称为key的zset中member元素的排名(按score从小到大排序)即下标
     *
     * @param key
     * @param member
     * @param dbIndex
     * @return
     */
    public Long getZrank(String key, String member, Integer dbIndex);

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public boolean exists(String key);

    /**
     * 判断key对应的map中 field对应的value是否存在
     *
     * @param key
     * @param field
     * @return
     */
    public boolean hexists(String key, String field);
}
