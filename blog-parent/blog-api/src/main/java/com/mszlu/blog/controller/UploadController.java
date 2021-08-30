package com.mszlu.blog.controller;

import com.mszlu.blog.utils.QiniuUtils;
import com.mszlu.blog.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private QiniuUtils qiniuUtils;

    @PostMapping
    public Result upload(@RequestParam("image")MultipartFile file){
        //原始文件名称
        String originalFilename = file.getOriginalFilename();
        //唯一的文件名称 防止重名
        String fileName=UUID.randomUUID().toString()+"."+
                StringUtils.substringAfterLast(originalFilename, ".");
        //👆👆substringAfterLast(String1,String2) :取String1内分隔符sting2后的字符串。👆👆
        //上传文件 这里上传到 七牛云。 云服务器按量付费，速度快，可以把图片发放到离用户最近的服务器上
        //降低我们自身应用服务器的带宽消耗
        boolean upload = qiniuUtils.upload(file, fileName);
        if(upload){
            return Result.success(QiniuUtils.url+fileName);
        }
        return Result.fail(2001,"上传失败");
    }
}
