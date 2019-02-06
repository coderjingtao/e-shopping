package com.eshop.service.impl;

import com.eshop.service.IFileService;
import com.eshop.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Description: The implementation of file upload and download Service
 * Created by Jingtao Liu on 1/02/2019.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path) {
        //1.Prepare a new file name
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("Start Uploading File: file name:{}, file path:{}, new file name:{}",fileName,path,uploadFileName);

        //2. Create File Directory or Path
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        //3. Create a empty File with the new name and the path created above.
        File targetFile = new File(path,uploadFileName);

        try {
            //4. Put the uploading file content to the target file
            file.transferTo(targetFile);

            //5. After transferring, the file is Uploaded to the tomcat temporary directory "webapp/upload"
            // and then, it needs to be uploaded to FTP server.
            if(FTPUtil.uploadFile(Lists.newArrayList(targetFile))){
                //6. After uploading to FTP server, delete it from the tomcat temporary directory
                targetFile.delete();
            }else{
                return null;
            }
        } catch (IOException e) {
            logger.error("Uploading File Exception",e);
            return null;
        }
        return targetFile.getName();
    }

}
