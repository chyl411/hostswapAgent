package com.ztessc.core.service;

import com.ztessc.core.exception.BizException;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author chyl411
 * @create 2018-10-13 14:47
 * @desc 异常诊断工具
 **/
@Service
public class DiagnosticService {
    @Value("${file.upload.root}")
    private String fileRoot;

    public String saveAsLocalFile(InputStream in, String relativeFilePath) throws Exception {
        OutputStream out = null;
        try {
            String filePath = fileRoot + File.separator + relativeFilePath;

            out = new FileOutputStream(filePath);
            IOUtils.copy(in, out);
            out.flush();
            return filePath;
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * @param path      jar包存储路径
     * @param classPath jar中要替换的class的文件名
     * @param args      要替换的类的全名如com.ztessc.xxx
     * @return ${return_type}
     * @throws
     * @Description: 使用jvm attach加载jar
     * @author chyl411
     * @date 2018/10/13 14:55
     */
    public void loadAgent(String path, String classPath, String args) throws Exception {
        if (!new File(path).exists()) {
            throw new BizException("jar包不存在");
        }

//        VirtualMachine virtualmachine = VirtualMachine.attach(getCurrentPid());
//        // 让JVM加载 Agent
//        virtualmachine.loadAgent(path, path + "|" + classPath + "|" + args);
//        virtualmachine.detach();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Class<?> vmdClass = loader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
        final Class<?> vmClass = loader.loadClass("com.sun.tools.attach.VirtualMachine");

        Object attachVmdObj = null;
        for (Object obj : (List<?>) vmClass.getMethod("list", (Class<?>[]) null).invoke(null, (Object[]) null)) {
            if ((vmdClass.getMethod("id", (Class<?>[]) null).invoke(obj, (Object[]) null))
                    .equals(getCurrentPid())) {
                attachVmdObj = obj;
            }
        }

        Object vmObj = null;
        try {
            if (null == attachVmdObj) {
                // 使用 attach(String pid) 这种方式
                vmObj = vmClass.getMethod("attach", String.class).invoke(null, "" + getCurrentPid());
            } else {
                vmObj = vmClass.getMethod("attach", vmdClass).invoke(null, attachVmdObj);
            }
            vmClass.getMethod("loadAgent", String.class, String.class).invoke(vmObj, path, path + "|" + classPath + "|" + args);
        } finally {
            if (null != vmObj) {
                vmClass.getMethod("detach", (Class<?>[]) null).invoke(vmObj, (Object[]) null);
            }
        }
    }

    private String getCurrentPid() {
        // get name representing the running Java virtual machine.
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
        // get pid
        String pid = name.split("@")[0];
        return pid;
    }
}
