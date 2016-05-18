package com.snail.exam;
 
import java.util.Map;
 
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class MyJavaTask implements JavaDelegate {
 
    private static final Logger log = LoggerFactory.getLogger(MyJavaTask.class);
     
    public void execute(DelegateExecution execution) throws Exception {
        log.info("This is MyJavaTask");
        for(Map.Entry entry : execution.getVariables().entrySet()) {
            log.info("{}={}", entry.getKey(), entry.getValue());
        }
    }
}
