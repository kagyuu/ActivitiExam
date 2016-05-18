package com.snail.exam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormProcess {

    private static final Logger log = LoggerFactory.getLogger(FormProcess.class);

    public static void main(String[] args) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            // ----- 4.3.1. Deploying the process
            log.info("--- #1. Deploying the process");
            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/test/VacationRequest.bpmn20.xml")
                    .deploy();
            
            // ----- 9. Forms (Read Start Form)
            ProcessDefinition processDef = processEngine.getRepositoryService().createProcessDefinitionQuery()
            		.processDefinitionKey("vacationRequest").singleResult();
            
            log.info("Vaction Request PID={}", processDef.getId());
            
            StartFormData startForm = processEngine.getFormService().getStartFormData(processDef.getId());
            for (FormProperty prop : startForm.getFormProperties()) {
            	log.info("id={}, name={}, type={}, value={}, readable={}, required={}, writable={}, "
            			, prop.getId(), prop.getName(), prop.getType(), prop.getValue()
            			, prop.isReadable(), prop.isRequired(), prop.isWritable());
            }
            
            // ----- 4.3.2. Starting a process instance
            log.info("--- #3. Starting a process instance");

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
            
            // Fetch all tasks for the management group
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().processDefinitionKeyLike("vacationRequest%").list();
            for (Task task : tasks) {
            	TaskFormData taskForm = processEngine.getFormService().getTaskFormData(task.getId());
                for (FormProperty prop : taskForm.getFormProperties()) {
                	log.info("id={}, name={}, type={}, value={}, readable={}, required={}, writable={}, "
                			, prop.getId(), prop.getName(), prop.getType(), prop.getValue()
                			, prop.isReadable(), prop.isRequired(), prop.isWritable());
                }

                 // Do task (reject application)
				 Map<String, Object> taskVariables = new HashMap<String, Object>();
				 taskVariables.put("vacationApproved", "false");
				 taskVariables.put("managerMotivation", "We have a tight deadline!");
				 taskService.complete(task.getId(), taskVariables);
            }            
            
            
             
        } catch (Throwable th) {
            log.error("ERROR", th);
        }
    }
}
