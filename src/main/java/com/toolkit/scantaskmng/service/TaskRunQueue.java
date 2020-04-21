package com.toolkit.scantaskmng.service;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskRunQueue {
    static private List<JSONObject> queue = new ArrayList<>();
    synchronized static public void add(JSONObject taskInfo) {
        queue.add(taskInfo);
    }
    synchronized static public JSONObject fetch() {
        if (queue.size() > 0) {
            JSONObject taskInfo = queue.get(0);
            queue.remove(0);
            return taskInfo;
        } else {
            return null;
        }
    }
}
