package com.tomoncle.powerjob.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.tomoncle.powerjob.common.OkHttpRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Objects;
import java.util.Optional;

/**
 * @author tomoncle
 */
@Slf4j
public class SimpleProcessor implements BasicProcessor {

    @SneakyThrows
    @Override
    public ProcessResult process(TaskContext context){
        // 在线日志
        OmsLogger logger = context.getOmsLogger();
        String jobParams = Optional.ofNullable(context.getJobParams()).orElse("S");
        logger.info("Current context info : {}", JSON.toJSONString(context));
        logger.info("Current jobParameters: {}", jobParams);
        // 本地日志
        log.debug("Current context info : {}", JSON.toJSONString(context));
        log.debug("Current jobParameters: {}", jobParams);

        String processor = (String) JSONPath.extract(jobParams,"$.processor.name");
        logger.info("Current processor : {}", processor);
        log.debug("Current processor : {}", processor);
        if(!Objects.equals("HTTP",processor)){
            return new ProcessResult(false, "job cannot support processor : "+ processor);
        }

        String method = (String) JSONPath.extract(jobParams,"$.processor.args.method");
        logger.info("Current method : {}", method);
        log.debug("Current method : {}", method);

        String url = (String) JSONPath.extract(jobParams,"$.processor.args.url");
        logger.info("Current url : {}", url);
        log.debug("Current url : {}", url);
        String response;
        switch (method){
            case "POST":
                response = OkHttpRequest.POST.request(url);
                break;
            case "GET":
            default:
                response = OkHttpRequest.GET.request(url);
        }
        logger.info("Current response : {}", response);
        log.debug("Current response : {}", response);

        JSONObject appendContext = (JSONObject) JSONPath.extract(jobParams,"$.context.append");
        logger.info("Current appendContext : {}", appendContext);
        log.debug("Current appendContext : {}", appendContext);

        for (String key : appendContext.keySet()){
            logger.info("Current append WorkflowContext {} : {}", key, JSONPath.extract(response,appendContext.getString(key)));
            log.debug("Current append WorkflowContext {} : {}", key, JSONPath.extract(response,appendContext.getString(key)) );
        }

        // 类型为工作流的job，加入工作流后，拥有此值
        if(Objects.nonNull(context.getWorkflowContext().getWfInstanceId())){
            log.warn("工作流ID：{}, ",context.getWorkflowContext().getWfInstanceId());
            context.getWorkflowContext().appendData2WfContext(jobParams,jobParams);
            return new ProcessResult(true, JSON.toJSONString(context.getWorkflowContext().getAppendedContextData()));
        }
        return new ProcessResult(true, "job is ok! ");
    }




}