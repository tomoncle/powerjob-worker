package com.tomoncle.powerjob.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.tomoncle.powerjob.common.OkHttpRequest;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.WorkflowContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author tomoncle
 */
@Slf4j
public class SimpleProcessor implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws IOException {
        // 在线日志
        OmsLogger logger = context.getOmsLogger();
        String jobParams = Optional.ofNullable(context.getJobParams()).orElse("S");
        logger.info("Current context info : {}", JSON.toJSONString(context));
        logger.info("Current jobParameters: {}", jobParams);
        // 本地日志
        log.debug("Current context info : {}", JSON.toJSONString(context));
        log.debug("Current jobParameters: {}", jobParams);

        String processor = (String) JSONPath.extract(jobParams, "$.processor.name");
        logger.info("Current processor : {}", processor);
        log.debug("Current processor : {}", processor);
        if (!Objects.equals("HTTP", processor)) {
            return new ProcessResult(false, "processor仅支持HTTP参数.");
        }
        JSONObject jsonObject = http(logger, jobParams, context.getWorkflowContext());
        // 类型为工作流的job，加入工作流后，拥有此值
        if (Objects.nonNull(context.getWorkflowContext().getWfInstanceId())) {
            log.warn("工作流ID：{}, ", context.getWorkflowContext().getWfInstanceId());
            return new ProcessResult(true, jsonObject.toJSONString());
        }
        return new ProcessResult(true, jsonObject.toJSONString());
    }

    /*
    step1. fake create vm
    {
        "processor":{
          "name":"HTTP",
          "args":{"method":"GET","url":"https://api.tomoncle.com/create?serverId=001&serverName=test"}
        },
        "context":{
            "serverId":"$.args.serverId",
            "serverName":"$.args.serverName"
        }
    }

    step2. fake create nginx
    {
        "processor":{
          "name":"HTTP",
          "args":{"method":"GET","url":"https://api.tomoncle.com/deploy?serviceId=svc-001&serviceName=nginx&@serverId&@serverName"}
        },
        "context":{
            "serviceId":"$.args.serviceId",
            "serviceName":"$.args.serviceName"
        }
    }

    step3. fake start nginx
    {
        "processor":{
          "name":"HTTP",
          "args":{"method":"GET","url":"https://api.tomoncle.com/opration?@serviceId&@serviceName&action=start"}
        }
    }
    */
    private JSONObject http(OmsLogger logger, String jobParams, WorkflowContext workflowContext) throws IOException {
        String method = (String) JSONPath.extract(jobParams, "$.processor.args.method");
        logger.info("Current method : {}", method);
        log.debug("Current method : {}", method);

        String url = (String) JSONPath.extract(jobParams, "$.processor.args.url");
        logger.info("Current url : {}", url);
        log.debug("Current url : {}", url);

        URL urlObj = new URL(url);
        StringBuilder params = new StringBuilder();
        Arrays.asList(urlObj.getQuery().split("&")).forEach(param -> {
            if (param.length() > 0) {
                params.append("&");
            }
            if (param.startsWith("@")) {
                param = param.replace("@", "");
                params.append(param).append("=").append(workflowContext.fetchWorkflowContext().get(param));
            } else {
                params.append(param);
            }
        });
        if (Objects.nonNull(urlObj.getRef())) {
            params.append("#").append(urlObj.getRef());
        }
        url = url.split("\\?")[0] + "?" + params.toString();
        String response;
        switch (method) {
            case "POST":
                response = OkHttpRequest.POST.request(url);
                break;
            case "GET":
            default:
                response = OkHttpRequest.GET.request(url);
        }
        logger.info("Current response : {}", response);
        log.debug("Current response : {}", response);

        JSONObject appendContext = Optional.ofNullable((JSONObject)JSONPath.extract(jobParams, "$.context")).orElse(new JSONObject());
        logger.info("Current appendContext : {}", appendContext);
        log.debug("Current appendContext : {}", appendContext);

        for (String key : appendContext.keySet()) {
            logger.info("Current append WorkflowContext {} : {}", key, JSONPath.extract(response, appendContext.getString(key)));
            log.debug("Current append WorkflowContext {} : {}", key, JSONPath.extract(response, appendContext.getString(key)));
            workflowContext.appendData2WfContext(key, JSONPath.extract(response, appendContext.getString(key)));
        }
        return JSON.parseObject(response);
    }


}