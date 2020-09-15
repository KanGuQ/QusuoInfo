package com.forcpacebj.api.utils;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.forcpacebj.api.config.StaticParam;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by pc on 2019/8/20.
 */
@Slf4j
public class PhoneCode {
    private static String code;


    /**
     * 阿里云短信服务配置
     */
    public static String getPhonemsg(String mobile, final String signName, final String templateCode, JSONObject json) {

        System.out.println(mobile);
        if (StrUtil.isBlank(mobile)) {
            System.out.println("手机号为空");
            return "";
        }

        // 设置超时时间-可自行调整
        System.setProperty(StaticParam.defaultConnectTimeout, StaticParam.Timeout);
        System.setProperty(StaticParam.defaultReadTimeout, StaticParam.Timeout);
        // 初始化ascClient需要的几个参数
        final String product = StaticParam.product;// 短信API产品名称（短信产品名固定，无需修改）
        final String domain = StaticParam.domain;// 短信API产品域名（接口地址固定，无需修改）
        // AK
        final String accessKeyId = StaticParam.accessKeyId;// accessKeyId
        final String accessKeySecret = StaticParam.accessKeySecret;// accessKeySecret
        // 初始化ascClient,暂时不支持多region
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        // 若报Can not find endpoint to access异常，请添加以下此行代码
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        } catch (ClientException e1) {
            e1.printStackTrace();
        }

        //获取验证码
//        code = json.get("code")==null?null:json.get("code").toString();

        IAcsClient acsClient = new DefaultAcsClient(profile);
        // 组装请求对象
        SendSmsRequest request = new SendSmsRequest();
        // 使用post提交
        request.setMethod(MethodType.POST);
        // 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
        request.setPhoneNumbers(mobile);
        // 必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        // 必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        // 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        // 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
        request.setTemplateParam(json.toString());
        // 可选-上行短信扩展码(无特殊需求用户请忽略此字段)
        // request.setSmsUpExtendCode("90997");
        // 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
//        request.setOutId("yourOutId");
        // 请求失败这里会抛ClientException异常
        SendSmsResponse sendSmsResponse;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
            log.info("短信接口返回的数据----------------");
            log.info("{Code:" + sendSmsResponse.getCode() +
                    ",Message:" + sendSmsResponse.getMessage() +
                    ",RequestId:" + sendSmsResponse.getRequestId() +
                    ",BizId:" + sendSmsResponse.getBizId() + "}");
            if ("OK".equals(sendSmsResponse.getCode())) {
                // 请求成功
                System.out.println("发送成功！！！");
            } else {
                //如果验证码出错，会输出错误码告诉你具体原因
                System.out.println(sendSmsResponse.getCode());
                System.out.println("发送失败...");
            }
        } catch (ClientException e) {
            e.printStackTrace();
            return "短信系统维护中...";
        }
        return sendSmsResponse.getCode();
    }

    /**
     * 生成6位随机数验证码
     *
     * @return
     */
    public static String vcode() {
        String vcode = "";
        for (int i = 0; i < 6; i++) {
            vcode = vcode + (int) (Math.random() * 9);
        }
        return vcode;
    }
}
