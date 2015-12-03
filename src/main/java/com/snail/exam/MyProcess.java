package com.snail.exam;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProcess {

	private static Logger log = LoggerFactory.getLogger(MyProcess.class);
	public static void main(String[] args) {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		
		// 4.3.1. Deploying the process
		RepositoryService repositoryService = processEngine.getRepositoryService();
		repositoryService.createDeployment()
		  .addClasspathResource("org/activiti/test/VacationRequest.bpmn20.xml")
		  .deploy();

		log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());


	}

}
