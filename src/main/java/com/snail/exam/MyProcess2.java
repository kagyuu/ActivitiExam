package com.snail.exam;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProcess2 {

    private static final Logger log = LoggerFactory.getLogger(MyProcess2.class);

    public static void main(String[] args) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            // ----- 4.3.1. Deploying the process
            log.info("--- #1. Deploying the process");
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/test/VacationRequest.bpmn20.xml")
                    .deploy();
            
            // ----- 4.3.4. Suspending and activating a process
            log.info("--- #2. Suspend Process");
            repositoryService.suspendProcessDefinitionByKey("vacationRequest");

            // ----- 4.3.2. Starting a process instance
            log.info("--- #3. Starting a process instance (will fail)");

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("employeeName", "Kermit");
            variables.put("numberOfDays", new Integer(4));
            variables.put("startDate", DateUtils.parseDate("1999-12-31", "yyyy-MM-dd"));
            variables.put("vacationMotivation", "I'm really tired!");

            // the process to run
            // id        : <process id="vacationRequest">
            // arguments : <activiti:formProperty>
            RuntimeService runtimeService = processEngine.getRuntimeService();
            try {
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);
            } catch (ActivitiException e) {
                log.error("ERROR", e);
            }
            
            // ----- 4.3.4. Suspending and activating a process
            log.info("--- #5. Activate Process");
            repositoryService.activateProcessDefinitionByKey("vacationRequest");
            
            log.info("--- #6. Starting a process instance (will success)");            
            try {
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);
                log.info("{} was started", processInstance.getProcessDefinitionId());
            } catch (ActivitiException e) {
                log.error("ERROR", e);
            }
 
        } catch (Throwable th) {
            log.error("ERROR", th);
        }
    }
}
