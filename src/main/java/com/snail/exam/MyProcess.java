package com.snail.exam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProcess {

    private static final Logger log = LoggerFactory.getLogger(MyProcess.class);

    public static void main(String[] args) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            // ----- 4.3.1. Deploying the process
            log.info("--- #1. Deploying the process");
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/test/VacationRequest.bpmn20.xml")
                    .deploy();

            log.info("Number of process definitions {}", repositoryService.createProcessDefinitionQuery().count());
            for (ProcessDefinition p : repositoryService.createProcessDefinitionQuery().list()) {
                log.info("PROCESS DEF [id={},name={},key={}]", p.getId(), p.getName(), p.getKey());
            }

            // ----- 4.3.2. Starting a process instance
            log.info("--- #2. Starting a process instance");

            //  VacationRequest.bpmn20.xml L3-10
            //  --------------------------------------------------------------------------
            //  <process id="vacationRequest" name="Vacation request" isExecutable="true">
            //               ^^^^^^^^^^^^^^^
            //    <startEvent id="request" activiti:initiator="employeeName">
            //                    ^^^^^^^*1                    ^^^^^^^^^^^^ $employeeName = the applicant user name
            //      <extensionElements>
            //        <activiti:formProperty id="numberOfDays" name="Number of days" type="long" required="true"></activiti:formProperty>
            //                                   ^^^^^^^^^^^^                              ^^^^
            //        <activiti:formProperty id="startDate" name="First day of holiday (dd-MM-yyy)" type="date" datePattern="dd-MM-yyyy hh:mm" required="true"></activiti:formProperty>
            //                                   ^^^^^^^^^                                                ^^^^
            //        <activiti:formProperty id="vacationMotivation" name="Motivation" type="string"></activiti:formProperty>
            //                                   ^^^^^^^^^^^^^^^^^^                          ^^^^^^
            //      </extensionElements>
            //    </startEvent>
            // <activiti:formProperty>
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("employeeName", "Kermit");
            variables.put("numberOfDays", new Integer(4));
            variables.put("startDate", DateUtils.parseDate("1999-12-31", "yyyy-MM-dd"));
            variables.put("vacationMotivation", "I'm really tired!");

            // the process to run
            // id        : <process id="vacationRequest">
            // arguments : <activiti:formProperty>
            RuntimeService runtimeService = processEngine.getRuntimeService();
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);

            log.info("Number of process instances: " + runtimeService.createProcessInstanceQuery().count());
            for (ProcessInstance p : runtimeService.createProcessInstanceQuery().list()) {
                log.info("PROCESS INSTANCE [id={},pid={},pname={},pkey={}]"
                        , p.getId()
                        , p.getProcessDefinitionId()
                        , p.getProcessDefinitionName()
                        , p.getProcessDefinitionKey());
            }
            
            // ----- 4.3.3. Completing tasks (Reject or Accept)
            //  VacationRequest.bpmn20.xml L11-21
            //  --------------------------------------------------------------------------
            //    <sequenceFlow id="flow1" sourceRef="request" targetRef="handleRequest"></sequenceFlow>
            //                                        ^^^^^^^*1           ^^^^^^^^^^^^^*2
            //    <userTask id="handleRequest" name="Handle vacation request" activiti:candidateGroups="management">
            //                  ^^^^^^^^^^^^^*2                                                         ^^^^^^^^^^
            //      <documentation>${employeeName} would like to take ${numberOfDays} day(s) of vacation (Motivation: ${vacationMotivation}).</documentation>
            //      <extensionElements>
            //        <activiti:formProperty id="vacationApproved" name="Do you approve this vacation" type="enum" required="true">
            //                                   ^^^^^^^^^^^^^^^^
            //          <activiti:value id="true" name="Approve"></activiti:value>
            //          <activiti:value id="false" name="Reject"></activiti:value>
            //        </activiti:formProperty>
            //        <activiti:formProperty id="managerMotivation" name="Motivation" type="string"></activiti:formProperty>
            //                                   ^^^^^^^^^^^^^^^^^
            //      </extensionElements>
            //    </userTask>
            log.info("--- #3. Completing tasks (Reject or Accept)");
            
            // Fetch all tasks for the management group
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
            for (Task task : tasks) {
                if (task.getProcessDefinitionId().startsWith("vacationRequest")){
                    if (task.getTaskDefinitionKey().equals("handleRequest")) {
                        // Description is <documentation>.
                        log.info("DO TASK [{}]", task.getDescription());
                        
                        // Do task (reject application)
                         Map<String, Object> taskVariables = new HashMap<String, Object>();
                         taskVariables.put("vacationApproved", "false");
                         taskVariables.put("managerMotivation", "We have a tight deadline!");
                         taskService.complete(task.getId(), taskVariables);
                         
                        log.info(ReflectionToStringBuilder.reflectionToString(task));
                    }
                }
            }
        } catch (Throwable th) {
            log.error("ERROR", th);
        }
    }
}
