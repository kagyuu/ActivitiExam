package com.snail.exam;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class MultiInstanceProcess {
 
    private static final Logger log = LoggerFactory.getLogger(MyProcess.class);
 
    public static void main(String[] args) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
 
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/test/MultiInstance.bpmn")
                    .deploy();
             
            log.info("Deployed");
             
            RuntimeService runtimeService = processEngine.getRuntimeService();
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("MultiExam");
             
            log.info("Complete");
 
        } catch (RuntimeException th) {
            log.error("ERROR", th);
        }
    }
}