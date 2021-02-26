package com.community.event;

import com.alibaba.fastjson.JSONObject;
import com.community.Util.CommunityConstant;
import com.community.Util.CommunityUtil;
import com.community.entity.DiscussPost;
import com.community.entity.Event;
import com.community.entity.Message;
import com.community.service.DiscussPostService;
import com.community.service.ElasticsearchService;
import com.community.service.MessageService;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant{
    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Value("${wk.image.command}")
    private String wkImageCommand;
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @KafkaListener(topics={TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        //判断内容是否有问题
        if (record==null||record.value()==null){
            logger.error("消息内容为空！");
            return;
        }
        //判断格式是否有问题
        Event event= JSONObject.parseObject(record.value().toString(),Event.class);
        if ((event==null)){
            logger.error("消息格式错误！");
            return;
        }
        //发送站内通知
        Message message=new Message();
        message.setFromId(SYSTEN_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        if (!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

    }
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        //判断内容是否有问题
        if (record == null || record.value() == null) {
            logger.error("消息内容为空！");
            return;
        }
        //判断格式是否有问题
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if ((event == null)) {
            logger.error("消息格式错误！");
            return;
        }
        DiscussPost discussPost=discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }
    //删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        //判断内容是否有问题
        if (record == null || record.value() == null) {
            logger.error("消息内容为空！");
            return;
        }
        //判断格式是否有问题
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if ((event == null)) {
            logger.error("消息格式错误！");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }
    //消费分享事件（这是单线程的，只会有一台服务器消费一个数据）
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        //判断内容是否有问题
        if (record == null || record.value() == null) {
            logger.error("消息内容为空！");
            return;
        }
        //判断格式是否有问题
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if ((event == null)) {
            logger.error("消息格式错误！");
            return;
        }

        String htmlUrl=(String)event.getData().get("htmlUrl");
        String fileName=(String)event.getData().get("fileName");
        String suffix=(String)event.getData().get("suffix");
        /*--quality 75 */
        String cmd=wkImageCommand+" "+htmlUrl+" "+wkImageStorage+"/"+fileName+"."+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功："+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败："+e.getMessage());
        }
        //启用定时器，监视该图片，一旦生成了，则上传至七牛云
        UploadTask uploadTask=new UploadTask(fileName,suffix);
        Future future=threadPoolTaskScheduler.scheduleAtFixedRate(uploadTask,500);
        uploadTask.setFuture(future);
    }

    class UploadTask implements Runnable{

        //文件名称
        private String fileName;
        //文件后缀
        private String suffix;
        //启动任务的返回值,可用于停止定时器
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime=System.currentTimeMillis();
            this.uploadTimes=0;
        }

        @Override
        public void run() {
            //生成图片失败
            if (System.currentTimeMillis()-startTime>30000){
                logger.error("生成图片执行时间过长，终止任务："+fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if (uploadTimes>=5){
                logger.error("上传次数过多，终止任务："+fileName);
                future.cancel(true);
                return;
            }
            String path=wkImageStorage+"/"+fileName+"."+suffix;
            File file=new File(path);
            if (file.exists()){
                logger.info("开始第"+(++uploadTimes)+"次上传"+fileName);
                //设置响应信息
                StringMap policy=new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //生成上传凭证
                Auth auth=Auth.create(accessKey,secretKey);
                String uploadToken=auth.uploadToken(shareBucketName,fileName,3600,policy);
                //指定上传的机房
                UploadManager uploadManager=new UploadManager(new Configuration(Zone.zone2()));
                try{
                    //开始上传图片
                    Response response=uploadManager.put(path,fileName,uploadToken,null,"image/"+suffix,false);
                    //处理响应结果
                    JSONObject jsonObject=JSONObject.parseObject(response.bodyString());
                    if (jsonObject==null||jsonObject.get("code")==null||!jsonObject.get("code").toString().equals("0")){
                        logger.info("开始第"+uploadTimes+"次上传失败"+fileName);
                    }else{
                        logger.info("开始第"+uploadTimes+"次上传成功"+fileName);
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info("开始第"+uploadTimes+"次上传失败"+fileName);
                }
            }else {
                logger.info("等待图片生成"+fileName);
            }
        }
    }

}























