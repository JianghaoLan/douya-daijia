package org.lanjianghao.daijia.driver.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.auditing.*;
import org.apache.commons.codec.binary.Base64;
import org.lanjianghao.daijia.driver.config.TencentCloudConfigProperties;
import org.lanjianghao.daijia.driver.service.CiService;
import org.lanjianghao.daijia.model.vo.order.TextAuditingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CiServiceImpl implements CiService {

    @Autowired
    private TencentCloudConfigProperties props;

    @Autowired
    private COSClient cosClient;

    @Override
    public Boolean imageAuditing(String path) {
        //1.创建任务请求对象
        ImageAuditingRequest request = new ImageAuditingRequest();
        //2.添加请求参数 参数详情请见 API 接口文档
        //2.1设置请求 bucket
        request.setBucketName(props.getBucketPrivate());
        //2.2设置审核策略 不传则为默认策略（预设）
        //request.setBizType("");
        //2.3设置 bucket 中的图片位置
        request.setObjectKey(path);
        //3.调用接口,获取任务响应对象
        ImageAuditingResponse response = cosClient.imageAuditing(request);

        return response.getPornInfo().getHitFlag().equals("0")
                && response.getAdsInfo().getHitFlag().equals("0")
                && response.getTerroristInfo().getHitFlag().equals("0")
                && response.getPoliticsInfo().getHitFlag().equals("0");
    }

    @Override
    public TextAuditingVo textAuditing(String content) {
        if (!StringUtils.hasText(content)) {
            TextAuditingVo vo = new TextAuditingVo();
            vo.setResult("0");
            return vo;
        }

        //1.创建任务请求对象
        TextAuditingRequest request = new TextAuditingRequest();
        //2.添加请求参数 参数详情请见 API 接口文档
        request.setBucketName(props.getBucketPrivate());
        //2.1.1设置请求内容,文本内容的Base64编码
        request.getInput().setContent(Base64.encodeBase64String(content.getBytes()));
        //或是cos中的设置对象地址 不可同时设置
        //request.getInput().setObject("1.txt");
        //2.2设置审核模板（可选）
        //request.getConf().setBizType("aa3e9d84a6a079556b0109a935c*****");
        //3.调用接口,获取任务响应对象
        TextAuditingResponse response = cosClient.createAuditingTextJobs(request);

        AuditingJobsDetail detail = response.getJobsDetail();
        TextAuditingVo textAuditingVo = new TextAuditingVo();
        if ("Success".equals(detail.getState())) {
            //检测结果: 0（审核正常），1 （判定为违规敏感文件），2（疑似敏感，建议人工复核）。
            String result = detail.getResult();

            //违规关键词
            StringBuilder keywords = new StringBuilder();
            List<SectionInfo> sectionInfoList = detail.getSectionList();
            for (SectionInfo info : sectionInfoList) {
                String pornInfoKeyword = info.getPornInfo().getKeywords();
                String illegalInfoKeyword = info.getIllegalInfo().getKeywords();
                String abuseInfoKeyword = info.getAbuseInfo().getKeywords();
                if (pornInfoKeyword.length() > 0) {
                    keywords.append(pornInfoKeyword).append(",");
                }
                if (illegalInfoKeyword.length() > 0) {
                    keywords.append(illegalInfoKeyword).append(",");
                }
                if (abuseInfoKeyword.length() > 0) {
                    keywords.append(abuseInfoKeyword).append(",");
                }
            }
            textAuditingVo.setResult(result);
            textAuditingVo.setKeywords(keywords.toString());
        }
        return textAuditingVo;
    }
}
