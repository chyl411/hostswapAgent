package com.ztessc.core.controller;

import ch.qos.logback.classic.LoggerContext;
import com.ztessc.core.exception.BizException;
import com.ztessc.core.service.DiagnosticService;
import com.ztessc.core.util.FileDownload;
import com.ztessc.core.util.MessageUtil;
import com.ztessc.core.util.PageData;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author chyl411
 * @create 2018-10-12 15:42
 * @desc 诊断工具包
 **/
@Controller
@RequestMapping(value = "/diagnostic")
public class DiagnosticController extends BaseController {
    @Resource
    private DiagnosticService diagnosticService;

    @GetMapping(value = "/exportLog")
    public void exportExcel(HttpServletResponse response) throws Exception {
        PageData pd = this.getPageData();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        String logPath = context.getProperty("LOG_ROOT");

        String fileName = pd.getString("fileName");
        if (fileName.contains("/") || fileName.contains("..")) {
            return;
        } else {
            String path = logPath + File.separator + fileName;
            if (!new File(path).exists()) {
                throw new BizException("文件未找到");
            }
            FileDownload.fileDownload(response, path);
        }
    }

    /**
    * @Description: agentFile 是个jar里面包含manifest文件 classFile是要替换的class文件
    * @param ${tags} 
    * @return ${return_type} 
    * @throws
    * @author chyl411
    * @date 2018/10/15 10:46
    */
    @PostMapping(value = "/postAgent")
    public PageData postAgent(MultipartFile agentFile, String classFile, String args) throws Exception {
        if (agentFile != null) {
            String originalFilename = agentFile.getOriginalFilename();

            String agentPath = diagnosticService.saveAsLocalFile(agentFile.getInputStream(), originalFilename);
            diagnosticService.loadAgent(agentPath, classFile, args);
        }

        return MessageUtil.getSuccessMessage();
    }
}
