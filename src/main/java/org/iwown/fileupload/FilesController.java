package org.iwown.fileupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: aidem
 * @Describtion: 文件上传工具类
 */
@RestController
public class FilesController {

    @Value("${uploadDir}")
    private String uploadDir;
    @Value("${downloadPre}")
    private String downloadPre;

    /**
     * 功能描述:单文件上传工具类
     */
    @RequestMapping(value = "/uploadImage/{type}", method = RequestMethod.POST)
    public String uploadImage(@PathVariable("type") String type, @RequestParam(value = "file") MultipartFile file) throws RuntimeException {
        if (file.isEmpty()) {
            return "fileCannotBeEmpty";
        }
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 文件上传后的路径
        String filePath = uploadDir + type + "/";
        // 加上时间戳
        String lastPath = filePath + new Date().getTime() + fileName;
        File dest = new File(lastPath);
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            System.out.println("file_path_after_successful_upload：" + lastPath);
            return downloadPre + type + "/" + lastPath.replace(uploadDir + type + "/", "");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "fail";
    }

    /**
     * 功能描述:文件下载
     */
    //文件下载相关代码
    @RequestMapping(value = "/downloadImage/{type}/{imageName}", method = RequestMethod.GET)
    public String downloadImage(@PathVariable("type") String type, @PathVariable("imageName") String imageName, HttpServletRequest request, HttpServletResponse response) {

        String lastName = type + "/" + imageName;
        String fileUrl = uploadDir + lastName;
        System.out.println("filePathToBeDownloaded is :" + fileUrl);
        if (fileUrl != null) {
            //当前是从该工程的WEB-INF//File//下获取文件(该目录可以在下面一行代码配置)然后下载到C:\\users\\downloads即本机的默认下载的目录
//            String realPath = request.getServletContext().getRealPath(
//                    "//WEB-INF//");
//            File file = new File(realPath, fileName);
            File file = new File(fileUrl);
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition",
                        "attachment;fileName=" + imageName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println("success downloadImage ：" + fileUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 功能描述:多文件上传
     */
    public String uploadFiles(@RequestParam(value = "file") MultipartFile[] files) {
        StringBuffer result = new StringBuffer();
        try {
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                    //调用上传方法
                    String fileName = executeUpload(files[i]);
                    result.append(fileName + ";");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "文件上传失败";
        }
        return result.toString();
    }

    /**
     * 功能描述: 多文件上传，抽取单文件代码
     */
    private String executeUpload(MultipartFile file) throws Exception {

        //文件后缀名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //上传文件名
        String fileName = UUID.randomUUID() + suffix;
        //服务端保存的文件对象
        File serverFile = new File(uploadDir + fileName);
        // 检测是否存在目录
        if (!serverFile.getParentFile().exists()) {
            serverFile.getParentFile().mkdirs();
        }
        //将上传的文件写入到服务器端文件内
        file.transferTo(serverFile);
        return fileName;
    }
}
