package com.dahai.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class FileController {

    private static final String file_token = "dahai_upload";

    @Value("${file.uploadFolder}")
    private String uploadFolder;


    @PostMapping("upload")
    public BaseResponse upload(@RequestParam("file") MultipartFile file,HttpServletRequest request) {
        BaseResponse response = new BaseResponse();

        String token = request.getHeader("file_token");
        if (token==null || token.length()==0 || !file_token.equals(token)) {
            response.error_code = "1001";
            response.reason = "授权失败";
            return response;
        }

        if (file.isEmpty()) {
            response.error_code = "1001";
            response.reason = "文件为空";
            return response;
        }

        String fileName = file.getOriginalFilename();

        String suffixName = fileName.substring(fileName.lastIndexOf("."));

        fileName = UUID.randomUUID() + suffixName;

        String fileDir = request.getHeader("file_dir");

        if (fileDir!=null && fileDir.length()!=0) {
            fileDir = fileDir + "/";
        } else {
            fileDir = "common/";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        fileDir = fileDir + format.format(new Date())+"/";

        File dest = new File(uploadFolder + fileDir + fileName);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            // api/file/test/2019-05/
            response.result = "/api/file/"+fileDir;
            return response;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.reason = "上传失败";
        response.error_code = "1001";
        return response;
    }


    @PostMapping("multiUpload")
    public BaseResponse handleFileUpload(HttpServletRequest request){
        BaseResponse response = new BaseResponse();

        String token = request.getHeader("file_token");
        if (token==null || token.length()==0 || !file_token.equals(token)) {
            response.error_code = "1001";
            response.reason = "授权失败";
            return response;
        }

        File dest = new File(uploadFolder);

        if (!dest.exists()) {
            dest.mkdirs();
        }

        List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");
        List<String> paths = new ArrayList<String>();
        MultipartFile file = null;
        BufferedOutputStream stream = null;
        for (int i =0;i<files.size();++i){
            file = files.get(i);
            if(!file.isEmpty()){
                try {
                    byte[] bytes = file.getBytes();

                    String fileName = file.getOriginalFilename();

                    String suffixName = fileName.substring(fileName.lastIndexOf("."));
                    fileName = UUID.randomUUID() + suffixName;
                    paths.add("/api/file/"+fileName);
                    stream = new BufferedOutputStream(new FileOutputStream(new File(uploadFolder + fileName)));
                    stream.write(bytes);
                    stream.close();
                } catch (Exception e) {
                    response.error_code = "1001";
                    response.reason = "上传失败";
                    return response;
                }
            }else{
                response.error_code = "1001";
                response.reason = "没有文件";
                return response;
            }
        }
        response.result = paths;
        return response;
    }
}
