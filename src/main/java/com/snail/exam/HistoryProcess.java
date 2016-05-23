package com.snail.exam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryProcess {

	private static final Logger log = LoggerFactory.getLogger(HistoryProcess.class);

	public static void main(String[] args) {
		try {
			ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

			// ----- 4.3.1. Deploying the process
			log.info("--- #1. Deploying the process");
			RepositoryService repositoryService = processEngine.getRepositoryService();
			repositoryService.createDeployment().addClasspathResource("org/activiti/test/VacationRequest.bpmn20.xml")
					.deploy();

			log.info("Number of process definitions {}", repositoryService.createProcessDefinitionQuery().count());
			for (ProcessDefinition p : repositoryService.createProcessDefinitionQuery().list()) {
				log.info("PROCESS DEF [id={},name={},key={}]", p.getId(), p.getName(), p.getKey());
			}

			// ----- 4.3.2. Starting a process instance
			log.info("--- #2. Starting a process instance");

			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("employeeName", "Kermit");
			variables.put("numberOfDays", new Integer(4));
			variables.put("startDate", DateUtils.parseDate("1999-12-31", "yyyy-MM-dd"));
			variables.put("vacationMotivation", "I'm really tired!");

			RuntimeService runtimeService = processEngine.getRuntimeService();
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);

			log.info("Number of process instances: " + runtimeService.createProcessInstanceQuery().count());
			for (ProcessInstance p : runtimeService.createProcessInstanceQuery().list()) {
				log.info("PROCESS INSTANCE [id={},pid={},pname={},pkey={}]", p.getId(), p.getProcessDefinitionId(),
						p.getProcessDefinitionName(), p.getProcessDefinitionKey());
			}

			log.info("--- #3. Completing tasks (Accept Request)");

			// Fetch all tasks for the management group
			TaskService taskService = processEngine.getTaskService();
			List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("management")
					.processDefinitionKey("vacationRequest").list();
			for (Task task : tasks) {
				if (task.getTaskDefinitionKey().equals("handleRequest")) {
					// Description is <documentation>.
					log.info("TASK ACCEPT REQ [{}]", task.getDescription());

					// Do task (reject application)
					Map<String, Object> taskVariables = new HashMap<String, Object>();
					taskVariables.put("vacationApproved", "true");
					taskVariables.put("managerMotivation", "Okay, refresh yourself!");
					taskService.complete(task.getId(), taskVariables);
				}
			}

			// ----- 11. History
			log.info("--- #4. History");
			HistoryService historyService = processEngine.getHistoryService();

			// 11.1.1. HistoricProcessInstanceQuery
			List<HistoricProcessInstance> historicPorcessInstances = historyService.createHistoricProcessInstanceQuery()
					.finished().orderByProcessInstanceDuration().desc().listPage(0, 10);
			log.info("TERMINATED PROCESS = {}", historicPorcessInstances.size());
			historicPorcessInstances.forEach(p -> {
				log.info("PROCESS INSTANCE [id={},pid={},name={},variables_size={}]"
						, p.getId()
						, p.getProcessDefinitionId()
						, p.getName()
						, p.getProcessVariables().size());
				p.getProcessVariables().forEach((k,v) -> {
					log.info("{} = {}", k, v);
				});
			});
			
			// 11.1.2. HistoricVariableInstanceQuery
			List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
			  .orderByVariableName().asc().list();
			log.info("VARIABLE SIZE = {}", historicVariableInstances.size());
			historicVariableInstances.forEach(p -> {
				log.info("VARIABLE [id={},pid={},task={},type={},name={},value={}]"
						, p.getId()
						, p.getProcessInstanceId()
						, p.getTaskId()
						, p.getVariableTypeName()
						, p.getVariableName()
						, p.getValue());
			});
			
			// 11.1.3. HistoricActivityInstanceQuery
			List<HistoricActivityInstance> historicActivityInstances =historyService.createHistoricActivityInstanceQuery()
			  .finished()
			  .orderByHistoricActivityInstanceEndTime().desc().list();
			log.info("ACTIVITY SIZE = {}", historicActivityInstances.size());
			historicActivityInstances.forEach(p -> {
				log.info("ACTIVITY [id={},name={},task={},asignee={}]"
						, p.getActivityId()
						, p.getActivityName()
						, p.getTaskId()
						, p.getAssignee()
						);
			});
			
			// 11.1.4. HistoricDetailQuery
			List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().list();
			log.info("DETAIL SIZE = {}", historicDetails.size());
			historicDetails.forEach(p -> {
				if (p instanceof HistoricFormProperty) {
					log.info("FORM [pid={},taskid={},property_id={},value={}]"
							, p.getProcessInstanceId()
							, p.getTaskId()
							, ((HistoricFormProperty)p).getPropertyId()
							, ((HistoricFormProperty)p).getPropertyValue()
							);					
				} else if (p instanceof HistoricVariableUpdate) {
					log.info("VARIABLE [pid={},taskid={},revision={},name={},value={}]"
							, p.getProcessInstanceId()
							, p.getTaskId()
							, ((HistoricVariableUpdate)p).getRevision()
							, ((HistoricVariableUpdate)p).getVariableName()
							, ((HistoricVariableUpdate)p).getValue()
							);					
				}
			});
			
			// 11.1.5. HistoricTaskInstanceQuery
			List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
			  .finished()
			  .orderByHistoricTaskInstanceDuration().desc()
			  .listPage(0, 10);
			log.info("TASK SIZE = {}", historicTaskInstances.size());
			historicTaskInstances.forEach(p -> {
				log.info("TASK [id={},name={},time={},asignee={}]"
					, p.getId()
					, p.getName()
					, p.getCreateTime()
					, p.getAssignee()
					);
			});
		} catch (Throwable th) {
			log.error("ERROR", th);
		}
	}
}