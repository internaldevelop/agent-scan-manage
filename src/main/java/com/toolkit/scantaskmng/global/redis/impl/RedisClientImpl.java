package com.toolkit.scantaskmng.global.redis.impl;

import com.toolkit.scantaskmng.global.redis.IRedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.*;
import java.util.*;

@Service
public class RedisClientImpl implements IRedisClient {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected JedisPool readRedisPool;

    @Autowired
    protected JedisPool writeRedisPool;

    private Serializable getObjectFromBytes(byte[] objBytes) {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (Serializable) oi.readObject();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return null;

    }

    private byte[] getBytesFromObject(Serializable obj) {
        if (obj == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            return bo.toByteArray();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addList(String key, List<Serializable> list) {
        if (key != null && list != null && list.size() > 0) {
            logger.debug("key:{} addList  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (Serializable s : list) {
                        if (s == null) {
                            logger.warn(" Serializable is null !");
                            continue;
                        }
                        try {
                            pl.rpush(key.getBytes(), getBytesFromObject(s));
                        } catch (RuntimeException re) {
                            logger.error("rpush begin error ! key {}" , key);
                            logger.error("rpush begin error !", re);
                        }
                    }
                    pl.sync();
                    tag = true;
                } else {
                    logger.error("pl couldn't be null !");
                }
                try {
                    pl = null;
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return contention !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addList   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addListItems(String key, Serializable[] values) {
        if (key != null && values != null && values.length > 0) {
            logger.debug("key:{} addListItems  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (Serializable s : values) {
                        if (s == null) {
                            logger.warn(" Serializable is null !");
                            continue;
                        }
                        try {
                            pl.rpush(key.getBytes(), getBytesFromObject(s));
                        } catch (RuntimeException re) {
                            logger.error("rpush begin error ! key {}" + key);
                            logger.error("rpush begin error !" + re);
                        }
                    }
                    pl.sync();
                    tag = true;
                } else {
                    logger.error("pl couldn't be null !");
                }
                try {
                    pl = null;
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return contention !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addListItems   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addMap(String key, Map<String, Serializable> map) {
        if (key != null && map != null && map.size() > 0) {
            logger.debug("key:{} addMap  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Set<String> mapkey = map.keySet();
                Map<byte[], byte[]> hash = new HashMap<byte[], byte[]>();
                for (String k : mapkey) {
                    Serializable value = map.get(k);
                    if (value == null) {
                        logger.warn(" map.get(k:{}) is null !", k);
                        continue;
                    }
                    hash.put(k.getBytes(), getBytesFromObject(value));
                }
                if (hash.size() > 0) {
                    try {
                        writeRedisClient.hmset(key.getBytes(), hash);
                        tag = true;
                    } catch (RuntimeException re) {
                        logger.error("hset begin error ! key {},map{}", key, hash);
                        logger.error("hset begin error !", re);
                    }
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return contention !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addMap   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addMapValue(String key, String mapkey, Serializable value) {
        if (key != null && mapkey != null) {
            logger.debug("key:{} addMapValue  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    writeRedisClient.hset(key.getBytes(), mapkey.getBytes(), getBytesFromObject(value));
                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("hset begin error ! key {},mapkey{}", key, mapkey);
                    logger.error("hset begin error !", re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return contention !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addMapValue   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addSet(String key, Set<Serializable> set) {
        if (key != null && set != null && set.size() > 0) {
            logger.debug("key:{} addSet  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (Serializable s : set) {
                        if (s == null) {
                            logger.warn(" Serializable is null !");
                            continue;
                        }
                        try {
                            pl.sadd(key.getBytes(), getBytesFromObject(s));
                        } catch (RuntimeException re) {
                            logger.error("sadd begin error ! key {}", key);
                            logger.error("sadd begin error !", re);
                        }
                    }
                    pl.sync();
                    tag = true;
                } else {
                    logger.error("pl couldn't be null !");
                }
                try {
                    pl = null;
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addSet   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean addSetItems(String key, Serializable[] values) {
        if (key != null && values != null && values.length > 0) {
            logger.debug("key:{} addSetItems  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (Serializable s : values) {
                        if (s == null) {
                            logger.warn(" Serializable is null !");
                            continue;
                        }
                        try {
                            pl.sadd(key.getBytes(), getBytesFromObject(s));
                        } catch (RuntimeException re) {
                            logger.error("zadd begin error ! key {}", key);
                            logger.error("zadd begin error !", re);
                        }
                    }
                    pl.sync();
                    tag = true;
                } else {
                    logger.error("pl couldn't be null !");
                }
                try {
                    pl = null;
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
                logger.debug("key:{} addSetItems   end!", key);
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} addSetItems   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty !");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delList(String... key) {
        if (key != null && key.length > 0) {
            logger.debug("key:{} delList  begin !" + key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    byte[][] bytes = new byte[key.length][];

                    for (int j = 0; j < key.length; j++) {
                        byte[] b = key[j].getBytes();

                        bytes[j] = b;
                    }

                    writeRedisClient.del(bytes);

                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("del begin error ! key {}" + key);
                    logger.error("del begin error !" + re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delList   end!" + key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null or empty !");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delListItems(String key, Serializable[] values) {
        if (key != null && values != null && values.length > 0) {
            logger.debug("key:{} delListItems values  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    for (Serializable v : values) {
                        if (v != null) {
                            try {
                                writeRedisClient.lrem(key.getBytes(), 1, getBytesFromObject(v));
                            } catch (RuntimeException re) {
                                logger.error("lrem begin error ! key {}", key);
                                logger.error("lrem begin error !", re);
                            }
                        } else {

                        }

                    }
                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("del begin error ! key {}", key);
                    logger.error("del begin error !", re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delListItems   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delMap(String... key) {
        if (key != null) {
            logger.debug("key:{} delMap  begin !" + key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    byte[][] bytes = new byte[key.length][];

                    for (int j = 0; j < key.length; j++) {
                        byte[] b = key[j].getBytes();

                        bytes[j] = b;
                    }

                    writeRedisClient.del(bytes);
                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("delMap begin error ! key {}" + key);
                    logger.error("delMap begin error !" + re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delMap   end!" + key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delMapValue(String key, String mapkey) {
        if (key != null && mapkey != null) {
            logger.debug("key:{} delMapValue  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    writeRedisClient.hdel(key.getBytes(), mapkey.getBytes());

                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("del begin error ! key {}", key);
                    logger.error("del begin error !", re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delMapValue   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delSet(String... key) {
        if (key != null) {
            logger.debug("key:{} delSet  begin !" + key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {

                    byte[][] bytes = new byte[key.length][];

                    for (int j = 0; j < key.length; j++) {
                        byte[] b = key[j].getBytes();

                        bytes[j] = b;
                    }

                    writeRedisClient.del(bytes);
                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("del begin error ! key {}" + key);
                    logger.error("del begin error !", re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delSet   end!" + key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean delSetItems(String key, Serializable[] values) {
        if (key != null && values != null && values.length > 0) {
            logger.debug("key:{} delSetItems  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (Serializable s : values) {
                        if (s == null) {
                            logger.warn(" Serializable is null !");
                            continue;
                        }
                        try {
                            pl.srem(key.getBytes(), getBytesFromObject(s));
                        } catch (RuntimeException re) {
                            logger.error("srem begin error ! key {}", key);
                            logger.error("srem begin error !", re);
                        }
                    }
                    pl.sync();
                    tag = true;
                } else {
                    logger.error("pl couldn't be null !");
                }
                try {
                    pl = null;
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
                return tag;
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} delSetItems   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null !");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public long getListLength(String key) {
        Long size = 0L;
        if (key != null) {
            logger.debug("key:{} getList  begin !", key);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                size = readRedisClient.llen(key.getBytes());
                if (size == null || size < 0) {
                    size = 0L;
                    logger.error("key={}  list not existing !", key);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getList   end!", key);
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
        return size;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public List<Serializable> getList(String key) {
        if (key != null) {
            logger.debug("key:{} getList  begin !", key);
            Jedis readRedisClient = null;
            List<Serializable> result = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                Long size = readRedisClient.llen(key.getBytes());
                if (size != null && size > 0) {
                    List<byte[]> results = readRedisClient.lrange(
                            key.getBytes(), 0, size.intValue());
                    if (results != null && results.size() > 0) {
                        result = new LinkedList<Serializable>();
                        for (byte[] s : results) {
                            if (s != null) {
                                Serializable res = getObjectFromBytes(s);
                                result.add(res);
                            } else {
                                result.add(null);
                            }
                        }
                    }
                } else {
                    logger.error("key={}  list not existing !", key);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getList   end!", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public List<Serializable> getList(String key, int start, int end) {
        if (key != null) {
            logger.debug("key:{}, start:{}, end:{} getList  begin !", new Object[] { key, start, end });
            Jedis readRedisClient = null;
            List<Serializable> result = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !",
                        e);
            }
            if (readRedisClient != null) {
                List<byte[]> results = readRedisClient.lrange(key.getBytes(),
                        start, end);
                if (results != null && results.size() > 0) {
                    result = new LinkedList<Serializable>();
                    for (byte[] s : results) {
                        if (s != null) {
                            Serializable res = getObjectFromBytes(s);
                            result.add(res);
                        } else {
                            result.add(null);
                        }
                    }
                } else {
                    logger.error("key={},start={},end={}  list not existing !",
                            new Object[] { key, start, end });
                }

                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error(
                            "redis connection pool couldn't return  connection !",
                            e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{}, start={},end={} getList   end!",
                    new Object[] { key, start, end });
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Map<String, Serializable> getMap(String key) {
        if (key != null) {
            logger.debug("key:{} getMap  begin !", key);
            Jedis readRedisClient = null;
            Map<String, Serializable> result = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !",
                        e);
            }
            if (readRedisClient != null) {
                Map<byte[], byte[]> resultMap = readRedisClient.hgetAll(key
                        .getBytes());
                if (resultMap != null && resultMap.size() > 0) {
                    result = new HashMap<String, Serializable>();
                    Set<byte[]> keys = resultMap.keySet();
                    for (byte[] k : keys) {
                        if (k != null) {
                            byte[] value = resultMap.get(k);
                            result.put(new String(k), getObjectFromBytes(value));
                        } else {
                            logger.warn("key is null !");
                        }
                    }
                } else {
                    logger.error("key={}  map not existing !", key);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error(
                            "redis connection pool couldn't return  connection !",
                            e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getMap   end!", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Serializable[] getMapItems(String key, String[] fields) {
        if (key == null) {
            throw new IllegalArgumentException("param key is null ");
        }
        if (fields == null || fields.length < 1) {
            throw new IllegalArgumentException("param fields is empty ");
        }
        logger.debug("key:{} getMap  begin !", key);
        Jedis readRedisClient = null;
        try {
            readRedisClient = (Jedis) readRedisPool.getResource();
        } catch (JedisConnectionException e) {
            logger.error("redis connection pool havn't idle contention !", e);
        }

        Serializable[] result = null;
        if (readRedisClient != null) {
            Map<byte[], byte[]> resultMap = readRedisClient.hgetAll(key
                    .getBytes());
            if (resultMap != null && !resultMap.isEmpty()) {
                result = new Serializable[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    byte[] v = resultMap.get(fields[i].getBytes());
                    if (v == null || "".equals(v)) {
                        continue;
                    }
                    result[i] = getObjectFromBytes(v);
                }
            }
        }

        try {
            readRedisClient.close();
        } catch (RuntimeException e) {
            logger.error("redis connection pool couldn't return  connection !", e);
        }

        return result;
    }

    @Override
    public Serializable getMapItem(String key, String field) {
        if (key == null) {
            throw new IllegalArgumentException("param key is null ");
        }
        if (field == null) {
            throw new IllegalArgumentException("param field is null ");
        }

        logger.debug("key:{} getMap  begin !", key);
        Jedis readRedisClient = null;
        try {
            readRedisClient = (Jedis) readRedisPool.getResource();
        } catch (JedisConnectionException e) {
            logger.error("redis connection pool havn't idle contention !", e);
        }

        Serializable result = null;

        if (readRedisClient != null) {
            byte[] value = readRedisClient.hget(key.getBytes(), field.getBytes());
            if (value != null && value.length != 0) {
                result = getObjectFromBytes(value);
            }
        }

        try {
            readRedisClient.close();
        } catch (RuntimeException e) {
            logger.error("redis connection pool couldn't return  connection !", e);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Set<Serializable> getSet(String key) {
        if (key != null) {
            logger.debug("key:{} getSet  begin !", key);
            Jedis readRedisClient = null;
            Set<Serializable> result = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                Set<byte[]> resultSet = readRedisClient
                        .smembers(key.getBytes());
                if (resultSet != null && resultSet.size() > 0) {
                    result = new HashSet<Serializable>();
                    for (byte[] s : resultSet) {
                        if (s != null && s.length != 0) {
                            Serializable res = getObjectFromBytes(s);
                            result.add(res);
                        } else {
                            result.add(null);
                        }
                    }
                } else {
                    logger.error("key={}  set not existing !", key);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getSet   end!", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public Boolean setListItems(String key, int[] indexes, Serializable[] values) {
        if (key != null && values != null && values.length > 0
                && indexes != null && indexes.length > 0
                && indexes.length == values.length) {
            logger.debug("key:{} setListItems  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                Pipeline pl = writeRedisClient.pipelined();
                if (pl != null) {
                    for (int i = 0; i < indexes.length; i++) {
                        Integer index = indexes[i];
                        Serializable value = values[i];
                        if (index != null && value != null) {
                            try {
                                pl.lset(key.getBytes(), index, getBytesFromObject(value));
                            } catch (RuntimeException re) {
                                logger.error("lpush begin error ! key {}" + key);
                                logger.error("lpush begin error !", re);
                            }
                        }
                    }
                    pl.sync();
                    try {
                        pl = null;
                        writeRedisPool.close();
                    } catch (RuntimeException e) {
                        logger.error("redis connection pool couldn't return  connection !", e);
                    }
                } else {
                    logger.error("pl  couldn't be  null !");
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} setListItems  end !", key);
            return tag;
        } else {
            throw new IllegalArgumentException(
                    "param key is null or  list is empty");
        }
    }

    @Override
    public Set<String> getKeysSet(String pattern) {
        if (pattern == null || pattern.length() < 1) {
            throw new IllegalArgumentException("param pattern is null or pattern is empty");
        }
        logger.debug("fetch keys by pattern {} !", pattern);
        Jedis readRedisClient = null;
        Set<byte[]> result = null;
        Set<String> rresult = null;
        try {
            readRedisClient = (Jedis) readRedisPool.getResource();
        } catch (JedisConnectionException e) {
            logger.error("redis connection pool havn't idle contention !", e);
        }
        if (readRedisClient != null) {
            try {
                result = readRedisClient.keys(pattern.getBytes());
                if (result != null && !result.isEmpty()) {
                    rresult = new HashSet<String>();
                    for (byte[] v : result) {
                        rresult.add((String) getObjectFromBytes(v));
                    }
                }

            } catch (RuntimeException re) {
                logger.error("keys  begin error ! pattern {}", pattern);
                logger.error("keys begin error !", re);
            }
            try {
                readRedisClient.close();
            } catch (RuntimeException e) {
                logger.error("redis connection pool couldn't return  connection !", e);
            }
        } else {
            logger.error("redis connection pool couldn't return  connection !");
        }
        logger.debug("fetch keys by pattern {} end !", pattern);
        return rresult;
    }

    // 1 成功 0 失败
    @Override
    public Long expire(String key, int seconds) {
        if (key == null || key.length() < 1) {
            throw new IllegalArgumentException("param key is null or key is empty");
        }
        logger.debug("set key: {} timeout: {}s  begin !", new Object[] { key, seconds });
        Jedis writeRedisClient = null;
        Long result = 0L;
        try {
            writeRedisClient = (Jedis) writeRedisPool.getResource();
        } catch (JedisConnectionException e) {
            logger.error("redis connection pool havn't idle contention !", e);
        }
        if (writeRedisClient != null) {
            try {
                result = writeRedisClient.expire(key.getBytes(), seconds);
                logger.debug("set key: {} timeout  end!", key);
            } catch (RuntimeException re) {
                logger.error("set key: {} timeout error !", key);
            }
            try {
                writeRedisPool.close();
            } catch (RuntimeException e) {
                logger.error("redis connection pool couldn't return  connection !", e);
            }
        } else {
            logger.error("redis connection pool couldn't return  connection !");
        }
        return result;
    }

    @Override
    public Long getNumber(String key) {
        if (key != null) {
            logger.debug("key:{} getCount  begin !", key);
            Jedis readRedisClient = null;
            Long result = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                try {
                    byte[] bresult = readRedisClient.get(key.getBytes());
                    if (bresult != null && bresult.length > 0) {
                        result = (Long) getObjectFromBytes(bresult);
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("get key: {} begin error !", key);
                    logger.error("error  info {} !", e);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getCount   end!", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Long setNumber(String key, Long number) {
        if (key != null) {
            logger.debug("key:{} setCount[{}]  begin !", key, number);
            Jedis writeRedisClient = null;
            Long result = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !",
                        e);
            }
            if (writeRedisClient != null) {
                try {

                    String r = writeRedisClient.set(key.getBytes(), getBytesFromObject(number));
                    String resultStr = number.toString();
                    if (resultStr != null && resultStr.length() > 0) {
                        result = Long.parseLong(resultStr);
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("get key: {} begin error !", key);
                    logger.error("error  info {} !", e);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} setCount[{}]   end!", key, number);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Long getZrank(String key, String member, Integer dbIndex) {
        Long rankNum = null;
        if (key != null) {
            logger.debug("key:{} getZrank  begin !", key);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
                if (dbIndex != null) {
                    readRedisClient.getDB();
                    readRedisClient.select(dbIndex);
                }
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                rankNum = readRedisClient.zrank(key, member);
                if (rankNum == null || rankNum < 0) {
                    rankNum = null;
                    logger.error("key={} list not existing !", key);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:{} getZrank   end!", key);
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
        return rankNum;
    }

    @Override
    public Long incrby(String key, long incr) {
        if (key != null) {
            logger.debug("key:[{}]incrby incr:[{}] begin !", key, incr);
            Jedis writeRedisClient = null;
            Long result = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    result = writeRedisClient.incrBy(key.getBytes(), incr);
                    if (result != null) {
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("incrBy key:[{}] incrby incr:[{}] begin error !", key, incr);
                    logger.error("error  info {} !", e);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:[{}]incrby incr:[{}] end !", key, incr);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Long decrby(String key, long decr) {
        if (key != null) {
            logger.debug("key:[{}]decrby incr:[{}] begin !", key, decr);
            Jedis writeRedisClient = null;
            Long result = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    result = writeRedisClient.decrBy(key.getBytes(), decr);
                    if (result != null) {
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("decrby key:[{}] decr :[{}] begin error !", key, decr);
                    logger.error("error  info {} !", e);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:[{}]decrby incr:[{}] end !", key, decr);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Long persist(String key) {
        if (key != null) {
            logger.debug("key:[{}] persist begin !", key);
            Jedis writeRedisClient = null;
            Long result = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    result = writeRedisClient.persist(key.getBytes());
                    if (result != null) {
                        return result;
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("persist key:[{}]  begin error !", key);
                    logger.error("error  info {} !", e);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:[{}] persist end !", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public Long ttl(String key) {
        if (key != null) {
            logger.debug("key:[{}] ttl begin !", key);
            Jedis writeRedisClient = null;
            Long result = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    result = writeRedisClient.ttl(key.getBytes());
                    if (result != null) {
                        return result;
                    } else {
                        logger.error("key={}  set not existing !", key);
                    }
                } catch (RuntimeException e) {
                    logger.error("ttl key:[{}]  begin error !", key);
                    logger.error("error  info {} !", e);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
            logger.debug("key:[{}] ttl end !", key);
            return result;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    @Override
    public boolean exists(String key) {
        boolean flag = false;
        if (key != null && !"".equals(key)) {
            logger.debug("exists key {} !", key);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                try {
                    flag = readRedisClient.exists(key.getBytes());
                } catch (RuntimeException re) {
                    logger.error("exists key error ! pattern {}", key);
                    logger.error("exists key error !", re);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
        }
        logger.debug("exists key {} end !", key);

        return flag;
    }

    @Override
    public boolean hexists(String key, String field) {
        boolean flag = false;
        if (key != null && !"".equals(key)) {
            logger.debug("exists key {}, filed {}  !", key, field);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                try {
                    flag = readRedisClient.hexists(key.getBytes(), field.getBytes());
                } catch (RuntimeException re) {
                    logger.error("exists key {}, filed {}", key, field);
                    logger.error("exists key error !", re);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
        }
        logger.debug("exists key {}, filed {} end !", key, field);

        return flag;
    }

    @Override
    public boolean set(String key, Serializable value) {
        boolean flag = false;
        if (key != null && !"".equals(key)) {
            logger.debug("set key {}, value {}  !", key, value);
            Jedis writeRedisClient = null;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    writeRedisClient.mset(key.getBytes(),
                            getBytesFromObject(value));
                    flag = true;
                } catch (RuntimeException re) {
                    logger.error("set key {}, value {}  !", key, value);
                    logger.error("set key error !", re);
                }
                try {
                    writeRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
        }
        logger.debug("set key {}, value {} end !", key, value);

        return flag;
    }

    @Override
    public Serializable get(String key) {
        Serializable value = null;
        if (key != null && !"".equals(key)) {
            logger.debug("get key {} !", key);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                try {
                    byte[] b = readRedisClient.get(key.getBytes());
                    if (b != null && b.length > 0) {
                        value = getObjectFromBytes(b);
                    }
                } catch (RuntimeException re) {
                    logger.error("get key {} !", key);
                    logger.error("get key error !", re);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
        }
        logger.debug("get key {} end !", key);

        return value;
    }
    @Override
    public Serializable get(String key, Integer dbIndex) {
        Serializable value = null;
        if (key != null && !"".equals(key)) {
            logger.debug("get key {} !", key);
            Jedis readRedisClient = null;
            try {
                readRedisClient = (Jedis) readRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (readRedisClient != null) {
                try {
                    readRedisClient.select(dbIndex);
                    byte[] b = readRedisClient.get(key.getBytes());
                    if (b != null && b.length > 0) {
                        value = getObjectFromBytes(b);
                    }
                } catch (RuntimeException re) {
                    logger.error("get key {} !", key);
                    logger.error("get key error !", re);
                }
                try {
                    readRedisClient.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool couldn't return  connection !");
            }
        }
        logger.debug("get key {} end !", key);

        return value;
    }


    @Override
    public boolean del(String key) {
        if (key != null) {
            logger.debug("key:{} del  begin !", key);
            Jedis writeRedisClient = null;
            boolean tag = false;
            try {
                writeRedisClient = (Jedis) writeRedisPool.getResource();
            } catch (JedisConnectionException e) {
                logger.error("redis connection pool havn't idle contention !", e);
            }
            if (writeRedisClient != null) {
                try {
                    writeRedisClient.del(key.getBytes());
                    tag = true;
                } catch (RuntimeException re) {
                    logger.error("del begin error ! key {}", key);
                    logger.error("del begin error !", re);
                }
                try {
                    writeRedisPool.close();
                } catch (RuntimeException e) {
                    logger.error("redis connection pool couldn't return  connection !", e);
                }
            } else {
                logger.error("redis connection pool havn't idle contention !");
            }
            logger.debug("key:{} del   end!", key);
            return tag;
        } else {
            throw new IllegalArgumentException("param key is null ");
        }
    }

    public JedisPool getReadRedisPool() {
        return readRedisPool;
    }

    public void setReadRedisPool(JedisPool readRedisPool) {
        this.readRedisPool = readRedisPool;
    }

    public JedisPool getWriteRedisPool() {
        return writeRedisPool;
    }

    public void setWriteRedisPool(JedisPool writeRedisPool) {
        this.writeRedisPool = writeRedisPool;
    }
}
