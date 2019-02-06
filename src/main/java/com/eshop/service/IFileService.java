package com.eshop.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Description: The interface of file upload and download Service
 * Created by Jingtao Liu on 1/02/2019.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
