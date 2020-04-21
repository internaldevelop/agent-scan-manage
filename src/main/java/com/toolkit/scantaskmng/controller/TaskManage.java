package com.toolkit.scantaskmng.controller;

import com.toolkit.scantaskmng.service.NodesManageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@RequestMapping(value = "/nodes/manage")
public class TaskManage {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private final NodesManageService nodesManageService;

    public TaskManage(NodesManageService nodesManageService) {
        this.nodesManageService = nodesManageService;
    }

    // 测试命令：  http://localhost:8191/nodes/manage/run-task?uuid=0022f9d3-a2b6-4e73-b128-8b883c76bdf3
//    @RequestMapping(value = "/run-task", method = RequestMethod.GET)
//    @ResponseBody
//    public Object runTask(@RequestParam("uuid") String taskUuid) {
//        return nodesManageService.runTask(taskUuid);
//    }

    // 测试命令：  http://localhost:8191/nodes/manage/run-project-task?project_uuid={project_uuid}&task_uuid={task_uuid}
    @RequestMapping(value = "/run-project-task", method = RequestMethod.GET)
    @ResponseBody
    public Object runProjectTask(@RequestParam("project_uuid") String projectUuid,
                                 @RequestParam("task_uuid") String taskUuid,
                                 @RequestParam("user_uuid") String userUuid) {
        return nodesManageService.runTask(projectUuid, taskUuid, userUuid);
    }
}
