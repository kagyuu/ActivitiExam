package com.snail.exam;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableProcess {

    private static final Logger log = LoggerFactory.getLogger(VariableProcess.class);

    public static void main(String[] args) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            // ----- 4.3.1. Deploying the process
            log.info("--- #1. Deploying the process");
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/test/VariableProcess.bpmn")
                    .deploy();

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("start", "Start");
            
            RuntimeService runtimeService = processEngine.getRuntimeService();
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableProcess", variables);            

        } catch (Throwable th) {
            log.error("ERROR", th);
        }
    }
}
