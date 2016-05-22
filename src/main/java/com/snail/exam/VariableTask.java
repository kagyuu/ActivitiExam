package com.snail.exam;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableTask implements JavaDelegate {
	private static final Logger log = LoggerFactory.getLogger(VariableTask.class);
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String id = execution.getCurrentActivityName();
		log.info(id);
		
		// Dump variables
		List<String> keys = new ArrayList<String>(execution.getVariableNames());
		keys.sort((o1,o2)->{return o1.compareTo(o2);});
		keys.forEach((key)->{
			log.info("@{} EXEC  SCOPE {} = {}", id, key, execution.getVariable(key));
		});
		
		List<String> keysLocal = new ArrayList<String>(execution.getVariableNamesLocal());
		keysLocal.sort((o1,o2)->{return o1.compareTo(o2);});
		keysLocal.forEach((key)->{
			log.info("@{} LOCAL SCOPE {} = {}", id, key, execution.getVariableLocal(key));
		});
		
		// Update variables
		execution.getVariableNames().forEach((name)->{
			execution.setVariable(name, id);
		});
		execution.getVariableNamesLocal().forEach((name)->{
			execution.setVariableLocal(name, id);
		});
		execution.setVariable(id + "_EXEC", id);
		execution.setVariable(id + "_LOCAL", id);
	}
}
