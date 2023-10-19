# powerjob-worker

[![Docker Image CI](https://github.com/tomoncle/powerjob-worker/actions/workflows/docker-image.yml/badge.svg?branch=master)](https://github.com/tomoncle/powerjob-worker/actions/workflows/docker-image.yml)

## 处理器

### `com.tomoncle.powerjob.processor.SimpleProcessor`

`context`描述将返回值中某些值加入到工作流上下文中，`args`中使用`@`符号引用工作流上下文中保存的参数，加入到当前任务的参数中。

* 添加第一个工作流任务`job1`, 参数如下：
```json
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

```

* 添加第二个工作流任务`job2`, 参数如下：
```json
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
```


* 添加第三个工作流任务`job2`, 参数如下：
```json
{
	"processor":{
	  "name":"HTTP",
	  "args":{"method":"GET","url":"https://api.tomoncle.com/opration?@serviceId&@serviceName&action=start"}
	}
}
```